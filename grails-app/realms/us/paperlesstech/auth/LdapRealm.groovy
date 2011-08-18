package us.paperlesstech.auth

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingException
import javax.naming.PartialResultException
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.CredentialsException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.UnknownAccountException
import org.hibernate.Session
import org.springframework.beans.factory.InitializingBean

import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.User

/**
 * A realm that authenticates users against an LDAP server.
 */
class LdapRealm implements InitializingBean {
	static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken
	static final realmToken = "LdapRealmInstance"

	// Services
	def grailsApplication
	def groupService
	def sessionFactory
	def userService

	// instance variables
	def ldapEnabled = false
	def ldapUrls
	def ldapAllUsersGroup
	def searchBase
	def searchUser
	def searchPass
	def searchAllGroups
	def searchAllUsers
	def searchSpecificUser

	void afterPropertiesSet() {
		def appConfig = grailsApplication.config
		assert appConfig

		ldapUrls = appConfig.ldap.server.url

		// Only enable ldap if the ldap url is set in the config file
		if (ldapUrls) {
			ldapEnabled = true
		} else {
			return
		}

		ldapAllUsersGroup = appConfig.ldap.group.all_users
		assert ldapAllUsersGroup
		searchBase = appConfig.ldap.search.base
		assert searchBase
		searchUser = appConfig.ldap.search.user
		searchPass = appConfig.ldap.search.pass
		searchAllUsers = appConfig.ldap.search.all_users.attribute
		assert searchAllUsers
		searchAllGroups = appConfig.ldap.search.all_groups.attribute
		assert searchAllGroups
		searchSpecificUser = appConfig.ldap.search.specific_user.attribute
		assert searchSpecificUser
	}

	def doWithEachSearchResult(String searchString, Closure closure) {
		// Accept strings and GStrings for convenience, but convert to a list.
		if (ldapUrls && !(ldapUrls instanceof Collection)) {
			ldapUrls = [ldapUrls]
		}

		// Set up the configuration for the LDAP search we are about to do.
		def env = new Hashtable()
		env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
		if (searchUser) {
			// Non-anonymous access for the search.
			env[Context.SECURITY_AUTHENTICATION] = "simple"
			env[Context.SECURITY_PRINCIPAL] = searchUser
			env[Context.SECURITY_CREDENTIALS] = searchPass
		}

		// Find an LDAP server that we can connect to.
		def ctx
		def urlUsed = ldapUrls.find { url ->
			log.info "Trying LDAP server ${url} ..."
			env[Context.PROVIDER_URL] = url

			// If an exception occurs, log it.
			try {
				ctx = new InitialDirContext(env)
				return true
			} catch (NamingException e) {
				log.error "Could not connect to ${url}: ${e}"
				return false
			}
		}

		if (!urlUsed) {
			def msg = 'No LDAP server available.'
			log.error msg
			throw new org.apache.shiro.authc.AuthenticationException(msg)
		}

		def searchControls = new SearchControls()
		searchControls.searchScope = SearchControls.SUBTREE_SCOPE
		log.info "LDAP searching: $searchString"
		def result = ctx.search(searchBase.toString(), searchString, searchControls)
		while (true) {
			def searchResult
			try {
				searchResult = result.next()
			} catch (PartialResultException e) {
				log.info("Skipping Partial Result Expression")
				searchResult = null
			}
			if (!searchResult) {
				break
			}

			closure.call(searchResult, env)
		}
	}

	def authenticate(authToken) {
		if (!ldapEnabled) {
			return
		}

		log.info "Attempting to authenticate ${authToken.username} in LDAP realm..."
		def username = authToken.username
		def password = new String(authToken.password)

		// Null username is invalid
		if (username == null) {
			throw new AccountException("Null usernames are not allowed by this realm.")
		}

		// Empty username is invalid
		if (username == "") {
			throw new AccountException("Empty usernames are not allowed by this realm.")
		}

		// Null password is invalid
		if (password == null) {
			throw new CredentialsException("Null password are not allowed by this realm.")
		}

		// empty password is invalid
		if (password == "") {
			throw new CredentialsException("Empty passwords are not allowed by this realm.")
		}

		def searchString = searchSpecificUser.replaceFirst("XXXXXXXX", username)

		def user
		def account
		doWithEachSearchResult(searchString) { searchResult, env ->
			env[Context.SECURITY_AUTHENTICATION] = "simple"
			env[Context.SECURITY_PRINCIPAL] = getLdapUserName(searchResult)
			env[Context.SECURITY_CREDENTIALS] = password

			try {
				new InitialDirContext(env)
			} catch (AuthenticationException ex) {
				log.info "Invalid password"
				throw new IncorrectCredentialsException("Invalid password for user '${username}'")
			}

			user = loadAndUpdateUser(searchResult)
			account = new SimpleAccount(user.id, user.passwordHash, "us.paperlesstech.auth.LocalizedRealm")
		}

		if (!user) {
			throw new UnknownAccountException("No account found for user [${username}]")
		}

		log.info("Successfully logged in user [$user.id]$user.username using local repository")
		return account
	}

	User loadAndUpdateUser(SearchResult searchResult) {
		def username = getLdapUserName(searchResult)
		assert username
		def externalId = getUserExternalId(searchResult)
		def email = getUserEmail(searchResult)
		def fullName = getUserName(searchResult)

		def user = User.findByExternalId(externalId)
		if (!user) {
			user = userService.createUser(username: username, fullName: fullName, email: email,
					externalId: externalId, realm: realmToken)
		}

		if (user.realm != realmToken) {
			throw new IncorrectCredentialsException("User '${user}' is not an ldap user")
		}

		if (user.username != username) {
			user.username = username
		}

		if (user.profile.email != email) {
			user.profile.email = email
		}

		if (user.profile.fullName != fullName) {
			user.profile.fullName = fullName
		}

		if (user.isDirty() || user.profile.isDirty()) {
			user.save(failOnError: true)
		}

		// Setup groups
		// First remove the user from all groups
		if (user.groups) {
			def currentGroups = new HashSet(user.groups)
			currentGroups.each { group ->
				groupService.deleteMember(user, group)
			}
		}
		log.info("Cleared all of user '$user' groups")

		// Now add the user to the default group
		groupService.addMember(user, defaultGroup)

		def groups = searchResult.attrs.memberOf
		if (groups) {
			groups.getAll().each { groupDN ->
				def group = Group.findByDescription(groupDN)
				if (group) {
					groupService.addMember(user, group)
					log.info("Adding group '$groupDN' to user '$user'")
				} else {
					log.info("Not adding group '$groupDN' to user '$user'")
				}
			}
		}

		user
	}

	def loadLdapUsers() {
		if (!ldapEnabled) {
			return
		}

		// Query for the most recently created ldap user
		def newestUser = User.createCriteria().get {
			eq("realm", realmToken)
			maxResults(1)
			order("id", "desc")
		}

		def lastUpdated
		if (newestUser) {
			// Query for any users added after this user was created (ones that are missed can be added individually)
			lastUpdated = (newestUser.dateCreated - 2).format("yyyyMMdd")
		} else {
			lastUpdated = "19700101"
		}

		def searchString = searchAllUsers.replaceFirst("XXXXXXXX", lastUpdated)

		doWithEachSearchResult(searchString) { searchResult, env ->
			loadAndUpdateUser(searchResult)

			Session hsession = sessionFactory.currentSession
			assert hsession != null
			hsession.flush()
			hsession.clear()
		}
	}

	String getLdapUserName(SearchResult searchResult) {
		searchResult.attrs.userPrincipalName?.get() ?: searchResult.nameInNamespace
	}

	String getUserExternalId(SearchResult searchResult) {
		def attr = searchResult.attrs.objectGUID ?: searchResult.attrs.uidNumber
		def externalId = attr?.get()
		assert externalId

		externalId
	}

	String getUserEmail(SearchResult searchResult) {
		searchResult.attrs.mail?.get()
	}

	String getUserName(SearchResult searchResult) {
		def attr = searchResult.attrs.name ?: searchResult.attrs.displayName
		def fullName = attr?.get()
		assert fullName

		fullName
	}

	Group getDefaultGroup() {
		def group = Group.findByName(ldapAllUsersGroup)
		if (!group) {
			group = groupService.createGroup(ldapAllUsersGroup, ldapAllUsersGroup, true)
		}

		group
	}

	def loadLdapGroups() {
		if (!ldapEnabled) {
			return
		}

		log.info("Updating groups")

		doWithEachSearchResult(searchAllGroups) { searchResult, env ->
			def externalId = getGroupExternalId(searchResult)
			def name = getGroupName(searchResult)
			def desc = getGroupDescription(searchResult)

			def group = Group.findByExternalId(externalId)
			if (!group) {
				group = groupService.createGroup(name, desc, true, externalId)
				log.info("Added group $group")
			} else {
				group.name = name
				group.description = desc

				if (group.isDirty()) {
					group.save(failOnError: true)
					log.info("Updated group $group")
				} else {
					log.debug("Did not update group $group because nothing changed")
				}
			}
		}

		log.info("Finished updating groups")
	}

	String getGroupExternalId(SearchResult searchResult) {
		def attr = searchResult.attrs.objectGUID ?: searchResult.attrs.gidNumber
		def externalId = attr?.get()
		assert externalId

		externalId
	}

	String getGroupName(SearchResult searchResult) {
		def attr = searchResult.attrs.name ?: searchResult.attrs.displayName
		def name = attr?.get()
		assert name

		name
	}

	String getGroupDescription(SearchResult searchResult) {
		def desc = searchResult.attrs.distinguishedName?.get() ?: searchResult.name
		assert desc

		desc
	}
}
