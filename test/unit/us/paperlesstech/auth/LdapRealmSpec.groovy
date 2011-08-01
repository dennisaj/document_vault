package us.paperlesstech.auth

import grails.plugin.spock.UnitSpec
import grails.plugins.nimble.core.Group
import grails.plugins.nimble.core.GroupService
import javax.naming.directory.BasicAttributes
import javax.naming.directory.SearchResult
import us.paperlesstech.MetaClassMixin

@Mixin(MetaClassMixin)
class LdapRealmSpec extends UnitSpec {
	LdapRealm realm
	GroupService groupService = Mock()

	def setup() {
		mockLogging(LdapRealm)
		realm = new LdapRealm()
		realm.ldapEnabled = true
		realm.groupService = groupService
	}

	def "getGroupDesc should return the group description"() {
		given:
		SearchResult searchResult = new SearchResult(name, new Object(), attributes)

		expect:
		realm.getGroupDescription(searchResult) == result

		where:
		name   | attributes                                                    | result
		"name" | new BasicAttributes()                                         | "name"
		"name" | new BasicAttributes("distinguishedName", "distinguishedName") | "distinguishedName"
	}

	def "getGroupName should return the group name"() {
		given:
		SearchResult searchResult = new SearchResult("group name", new Object(), attributes)

		expect:
		realm.getGroupName(searchResult) == result

		where:
		attributes                                        | result
		new BasicAttributes("name", "name")               | "name"
		new BasicAttributes("displayName", "displayName") | "displayName"
	}

	def "getGroupExternalId should return the group id"() {
		given:
		SearchResult searchResult = new SearchResult("group external id", new Object(), attributes)

		expect:
		realm.getGroupExternalId(searchResult) == result

		where:
		attributes                                      | result
		new BasicAttributes("objectGUID", "objectGUID") | "objectGUID"
		new BasicAttributes("gidNumber", "gidNumber")   | "gidNumber"
	}

	def "getDefaultGroup should return the default group if it exists"() {
		given:
		realm.ldapAllUsersGroup = groupName
		def group = new Group(name: groupName)
		mockDomain(Group, [group])

		expect:
		realm.getDefaultGroup() == group

		where:
		groupName = "All Users"
	}

	def "getDefaultGroup should create the default group if it does not exist"() {
		given:
		realm.ldapAllUsersGroup = groupName
		def group = new Group(name: groupName)
		mockDomain(Group)
		1 * groupService.createGroup(groupName, groupName, true) >> group

		expect:
		realm.getDefaultGroup() == group

		where:
		groupName = "All Users"
	}

	def "getUserName should return the user name"() {
		given:
		SearchResult searchResult = new SearchResult("user name", new Object(), attributes)

		expect:
		realm.getUserName(searchResult) == result

		where:
		attributes                                        | result
		new BasicAttributes("name", "name")               | "name"
		new BasicAttributes("displayName", "displayName") | "displayName"
	}

	def "getUserEmail should return the user email"() {
		given:
		SearchResult searchResult = new SearchResult("user", new Object(), attributes)

		expect:
		realm.getUserEmail(searchResult) == "email"

		where:
		attributes = new BasicAttributes("mail", "email")
	}

	def "getUserExternalId should return the user id"() {
		given:
		SearchResult searchResult = new SearchResult("user external id", new Object(), attributes)

		expect:
		realm.getUserExternalId(searchResult) == result

		where:
		attributes                                      | result
		new BasicAttributes("objectGUID", "objectGUID") | "objectGUID"
		new BasicAttributes("uidNumber", "uidNumber")   | "uidNumber"
	}

	def "getLdapUserName should return ldap user name"() {
		given:
		SearchResult searchResult = new SearchResult("user", new Object(), attributes)
		searchResult.nameInNamespace = nameInNamespace

		expect:
		realm.getLdapUserName(searchResult) == result

		where:
		nameInNamespace   | attributes                                                    | result
		"nameInNamespace" | new BasicAttributes()                                         | "nameInNamespace"
		"nameInNamespace" | new BasicAttributes("userPrincipalName", "userPrincipalName") | "userPrincipalName"
	}

	def "loadLdapGroup should create groups that do not exist"() {
		// Given some dummy values to return
		realm.searchAllGroups = searchAllGroups
		realm.metaClass.getGroupExternalId = { SearchResult searchResult -> groupExternalId }
		realm.metaClass.getGroupName = { SearchResult searchResult -> groupName }
		realm.metaClass.getGroupDescription = { SearchResult searchResult -> groupDescription }

		// Given that we are only testing the closure not doWithEachSearchResult
		realm.metaClass.doWithEachSearchResult = { String searchString, Closure closure ->
			assert searchString == searchAllGroups
			// Execute with a dummy search result
			closure.call(new SearchResult("searchResult", new Object(), null), [:])
		}

		// Given that no groups exist
		mockDomain(Group)

		when: "When called with a group that does not exist"
		realm.loadLdapGroups()

		then: "A new group should be created"
		1 * groupService.createGroup(groupName, groupDescription, true, groupExternalId)

		where:
		searchAllGroups = "searchAllGroups"
		groupExternalId = "groupExternalId"
		groupName = "groupName"
		groupDescription = "groupDescription"
	}

	def "loadLdapGroup should update groups whose info has changed"() {
		// Given some dummy values to return
		realm.searchAllGroups = searchAllGroups
		realm.metaClass.getGroupExternalId = { SearchResult searchResult -> groupExternalId }
		realm.metaClass.getGroupName = { SearchResult searchResult -> groupName }
		realm.metaClass.getGroupDescription = { SearchResult searchResult -> groupDescription }

		// Given that we are only testing the closure not doWithEachSearchResult
		realm.metaClass.doWithEachSearchResult = { String searchString, Closure closure ->
			assert searchString == searchAllGroups
			// Execute with a dummy search result
			closure.call(new SearchResult("searchResult", new Object(), null), [:])
		}

		// Given that the group exists with different values
		boolean saveCalled = false
		def group = new Group(name: "old" + groupName, description: "old" + groupDescription, externalId: groupExternalId)
		mockDomain(Group, [group])
		metaClassFor(Group).save = { Map m -> saveCalled = true; group }
		metaClassFor(Group).isDirty = { -> true }

		when: "When called with a group that exists"
		realm.loadLdapGroups()

		then: "The group should be updated and saved"
		group.name == groupName
		group.description == groupDescription
		saveCalled

		where:
		searchAllGroups = "searchAllGroups"
		groupExternalId = "groupExternalId"
		groupName = "groupName"
		groupDescription = "groupDescription"
	}

	def "loadLdapGroup should not save groups whose info has not changed"() {
		// Given some dummy values to return
		realm.searchAllGroups = searchAllGroups
		realm.metaClass.getGroupExternalId = { SearchResult searchResult -> groupExternalId }
		realm.metaClass.getGroupName = { SearchResult searchResult -> groupName }
		realm.metaClass.getGroupDescription = { SearchResult searchResult -> groupDescription }

		// Given that we are only testing the closure not doWithEachSearchResult
		realm.metaClass.doWithEachSearchResult = { String searchString, Closure closure ->
			assert searchString == searchAllGroups
			// Execute with a dummy search result
			closure.call(new SearchResult("searchResult", new Object(), null), [:])
		}

		// Given that the group exists with the same values
		boolean saveCalled = false
		def group = new Group(name: groupName, description: groupDescription, externalId: groupExternalId)
		mockDomain(Group, [group])
		metaClassFor(Group).save = { Map m -> saveCalled = true; group }
		metaClassFor(Group).isDirty = { -> false }

		when: "When called with a group that exists"
		realm.loadLdapGroups()

		then: "The group values should not have changed so nothing should be saved"
		group.name == groupName
		group.description == groupDescription
		!saveCalled

		where:
		searchAllGroups = "searchAllGroups"
		groupExternalId = "groupExternalId"
		groupName = "groupName"
		groupDescription = "groupDescription"
	}
}
