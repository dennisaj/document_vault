package us.paperlesstech

import org.apache.shiro.realm.Realm
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.support.DelegatingSubject
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import spock.lang.Unroll
import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.Permission
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.Role
import us.paperlesstech.nimble.User

class AuthServiceIntegrationSpec extends AbstractShiroIntegrationSpec {
	AuthService service
	def localizedRealmInstance
	def grailsApplication
	def groupService
	def roleService

	@Override
	def cleanup() {
		DelegatingSubject.metaClass = null
	}

	def setup() {
		service = new AuthService()
		service.grailsApplication = grailsApplication

		// Run checks against the default realm
		def beans = grailsApplication.mainContext.getBeansOfType(Realm.class)
		beans.each { key, value ->
			if (key.toLowerCase().contains("localizedrealm")) {
				localizedRealmInstance = value
			}
		}
		assert localizedRealmInstance

		setSecurityManager(new DefaultWebSecurityManager(localizedRealmInstance))

		setSubject(new Subject.Builder(getSecurityManager()).buildSubject())
	}

	User newUser(List<String> permissionStrings = null) {
		def rand = new Random()
		def profile = new Profile()
		def name = rand.nextInt().toString()
		def user = new User(username: name, profile: profile)

		permissionStrings?.each { permissionString ->
			def permission = new Permission(managed: true, type: Permission.defaultPerm, target: permissionString)
			permission.save(flush: true)
			user.addToPermissions(permission)
			user.save(flush: true)
		}

		user.save(flush: true)

		user
	}

	Document newDocument(Map input) {
		def group = new Group()
		if (input) {
			group.id = input.groupId
		}
		group.save(flush: true)
		def document = new Document()
		if (input) {
			document.id = input.documentId
		}
		document.group = group
		document.save(flush: true)

		document
	}

	def "authenticatedSubject should always return a subject"() {
		when:
		def subject = service.authenticatedSubject

		then:
		subject
	}

	def "authenticatedUser should not return a user if one is not logged in"() {
		when:
		def user = service.authenticatedUser

		then:
		user == null
	}

	def "authenticatedUser should return a user if one is logged in"() {
		given:
		def user = newUser()
		subject.metaClass.getPrincipal = { user.id }

		when:
		def authenticatedUser = service.authenticatedUser

		then:
		!user.errors.allErrors
		authenticatedUser
	}

	def "isLoggedIn only returns true if the user is logged in or remembered"() {
		given:
		subject.metaClass.isAuthenticated = { authenticated }
		subject.metaClass.isRemembered = { remembered }

		expect:
		service.isLoggedIn() == result

		where:
		authenticated | remembered | result
		true          | true       | true
		true          | false      | true
		false         | true       | true
		false         | false      | false
	}

	def "isAdmin makes sure the user has the admin role"() {
		given:
		// I don't know why this has to be on the class and the instance, but it works
		DelegatingSubject.metaClass.hasRole = { String role ->
			role == AdminsService.ADMIN_ROLE && hasRole
		}
		subject.metaClass.hasRole = { String role ->
			role == AdminsService.ADMIN_ROLE && hasRole
		}

		expect:
		service.isAdmin() == result

		where:
		hasRole | result
		true    | true
		false   | false
	}

	@Unroll("Testing if user with permission #permission can delete document:#groupId:#documentId")
	def "canDelete tests"() {
		def user = newUser([permission])
		def document = newDocument(documentId: documentId, groupId: groupId)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canDelete(document) == result

		where:
		permission            | groupId | documentId | result
		"*"                   | 1L      | 1L         | true
		"*"                   | 2L      | 1L         | true
		"document:delete:*"   | 1L      | 1L         | true
		"document:delete:*"   | 2L      | 1L         | true
		"document:delete:1"   | 1L      | 1L         | true
		"document:delete:1"   | 2L      | 1L         | false
		"document:delete:*:1" | 1L      | 1L         | true
		"document:delete:*:1" | 2L      | 1L         | true
		"document:delete:1:*" | 1L      | 1L         | true
		"document:delete:1:*" | 2L      | 1L         | false
		"document:delete:1:1" | 1L      | 1L         | true
		"document:delete:1:1" | 2L      | 1L         | false
		// Revisit sometime
		// "document:*:1:1"      | 1L      | 1L         | true
	}

	@Unroll("Testing if user with permissions #permissions can delete any document")
	def "canDeleteAnyDocument tests"() {
		def user = newUser(permissions)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canDeleteAnyDocument() == result

		where:
		permissions                                  | result
		["*"]                                        | true
		["document:view:1:1", "*"]                   | true
		["document:view:1:1", "document:delete:*:1"] | true
		["document:view:1:1", "document:delete:1:*"] | true
		["document:view:1:1", "document:delete:1:1"] | true
		["document:view:*"]                          | false
		["document:view:1:*"]                        | false
		["document:view:*:1"]                        | false
		["document:view:1:1"]                        | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
	}

	@Unroll("Testing if user with group permissions #permissions can delete any document")
	def "canDeleteAnyDocument group tests (for isPermissionImplied)"() {
		def user = newUser()
		def group = new Group(name: "group").save()
		permissions.each {
			def permission = new Permission(managed: true, type: Permission.defaultPerm, target: it)
			permission.save(flush: true)
			group.addToPermissions(permission)
			group.save(flush: true)
		}
		groupService.addMember(user, group)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canDeleteAnyDocument() == result

		where:
		permissions                                  | result
		["document:view:1:1", "document:delete:*:1"] | true
		["document:view:1:1", "document:delete:1:*"] | true
		["document:view:1:1", "document:delete:1:1"] | true
		["document:view:*"]                          | false
		["document:view:1:*"]                        | false
		["document:view:*:1"]                        | false
		["document:view:1:1"]                        | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
		// ["*"]                                        | true
		// ["document:view:1:1", "*"]                   | true
	}

	@Unroll("Testing if user with role permissions #permissions can delete any document")
	def "canDeleteAnyDocument role tests (for isPermissionImplied)"() {
		def user = newUser()
		def role = new Role(name: "role").save()
		permissions.each {
			def permission = new Permission(managed: true, type: Permission.defaultPerm, target: it)
			permission.save(flush: true)
			role.addToPermissions(permission)
			role.save(flush: true)
		}
		roleService.addMember(user, role)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canDeleteAnyDocument() == result

		where:
		permissions                                  | result
		["document:view:1:1", "document:delete:*:1"] | true
		["document:view:1:1", "document:delete:1:*"] | true
		["document:view:1:1", "document:delete:1:1"] | true
		["document:view:*"]                          | false
		["document:view:1:*"]                        | false
		["document:view:*:1"]                        | false
		["document:view:1:1"]                        | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
		// ["*"]                                        | true
		// ["document:view:1:1", "*"]                   | true
	}

	@Unroll("Testing if user with group-role permissions #permissions can delete any document")
	def "canDeleteAnyDocument group-role tests (for isPermissionImplied)"() {
		def user = newUser()
		def role = new Role(name: "role").save()
		permissions.each {
			def permission = new Permission(managed: true, type: Permission.defaultPerm, target: it)
			permission.save(flush: true)
			role.addToPermissions(permission)
			role.save(flush: true)
		}
		def group = new Group(name: "group").save()
		roleService.addGroupMember(group, role)
		groupService.addMember(user, group)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canDeleteAnyDocument() == result

		where:
		permissions                                  | result
		["document:view:1:1", "document:delete:*:1"] | true
		["document:view:1:1", "document:delete:1:*"] | true
		["document:view:1:1", "document:delete:1:1"] | true
		["document:view:*"]                          | false
		["document:view:1:*"]                        | false
		["document:view:*:1"]                        | false
		["document:view:1:1"]                        | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
		// ["*"]                                        | true
		// ["document:view:1:1", "*"]                   | true
	}

	@Unroll("Testing if user with permission #permission can sign document:#groupId:#documentId")
	def "canGetSigned tests"() {
		def user = newUser([permission])
		def document = newDocument(documentId: documentId, groupId: groupId)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }
		service.grailsApplication.config.document_vault.remoteSigning.enabled = true

		expect:
		service.canGetSigned(document) == result

		where:
		permission               | groupId | documentId | result
		"*"                      | 1L      | 1L         | true
		"*"                      | 2L      | 1L         | true
		"document:getsigned:*"   | 1L      | 1L         | true
		"document:getsigned:*"   | 2L      | 1L         | true
		"document:getsigned:1"   | 1L      | 1L         | true
		"document:getsigned:1"   | 2L      | 1L         | false
		"document:getsigned:*:1" | 1L      | 1L         | true
		"document:getsigned:*:1" | 2L      | 1L         | true
		"document:getsigned:1:*" | 1L      | 1L         | true
		"document:getsigned:1:*" | 2L      | 1L         | false
		"document:getsigned:1:1" | 1L      | 1L         | true
		"document:getsigned:1:1" | 2L      | 1L         | false
		// Revisit sometime
		// "document:*:1:1"      | 1L      | 1L         | true
	}

	@Unroll("Testing if user with permission #permission can leave notes on document:#groupId:#documentId")
	def "canNotes tests"() {
		def user = newUser([permission])
		def document = newDocument(documentId: documentId, groupId: groupId)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canNotes(document) == result

		where:
		permission           | groupId | documentId | result
		"*"                  | 1L      | 1L         | true
		"*"                  | 2L      | 1L         | true
		"document:notes:*"   | 1L      | 1L         | true
		"document:notes:*"   | 2L      | 1L         | true
		"document:notes:1"   | 1L      | 1L         | true
		"document:notes:1"   | 2L      | 1L         | false
		"document:notes:*:1" | 1L      | 1L         | true
		"document:notes:*:1" | 2L      | 1L         | true
		"document:notes:1:*" | 1L      | 1L         | true
		"document:notes:1:*" | 2L      | 1L         | false
		"document:notes:1:1" | 1L      | 1L         | true
		"document:notes:1:1" | 2L      | 1L         | false
		// Revisit sometime
		// "document:*:1:1"      | 1L      | 1L         | true
	}

	@Unroll("Testing if user with permissions #permissions can leave notes on any document")
	def "canNotesAnyDocument tests"() {
		def user = newUser(permissions)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canNotesAnyDocument() == result

		where:
		permissions                                 | result
		["*"]                                       | true
		["document:view:1:1", "*"]                  | true
		["document:view:1:1", "document:notes:*:1"] | true
		["document:view:1:1", "document:notes:1:*"] | true
		["document:view:1:1", "document:notes:1:1"] | true
		["document:view:*"]                         | false
		["document:view:1:*"]                       | false
		["document:view:*:1"]                       | false
		["document:view:1:1"]                       | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
	}

	@Unroll("Testing if user with permission #permission can print document:#groupId:#documentId")
	def "canPrint tests"() {
		def user = newUser([permission])
		def document = newDocument(documentId: documentId, groupId: groupId)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		document.addToFiles(new DocumentData(mimeType: MimeType.PDF))
		service.canPrint(document) == result
		// Currently if the file type isn't PDF we don't print the document
		document.files.clear()
		document.addToFiles(new DocumentData())
		service.canPrint(document) == false

		where:
		permission           | groupId | documentId | result
		"*"                  | 1L      | 1L         | true
		"*"                  | 2L      | 1L         | true
		"document:print:*"   | 1L      | 1L         | true
		"document:print:*"   | 2L      | 1L         | true
		"document:print:1"   | 1L      | 1L         | true
		"document:print:1"   | 2L      | 1L         | false
		"document:print:*:1" | 1L      | 1L         | true
		"document:print:*:1" | 2L      | 1L         | true
		"document:print:1:*" | 1L      | 1L         | true
		"document:print:1:*" | 2L      | 1L         | false
		"document:print:1:1" | 1L      | 1L         | true
		"document:print:1:1" | 2L      | 1L         | false
		// Revisit sometime
		// "document:*:1:1"      | 1L      | 1L         | true
	}

	@Unroll("Testing if user with permissions #permissions can print any document")
	def "canPrintAnyDocument tests"() {
		def user = newUser(permissions)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canPrintAnyDocument() == result

		where:
		permissions                                 | result
		["*"]                                       | true
		["document:view:1:1", "*"]                  | true
		["document:view:1:1", "document:print:*:1"] | true
		["document:view:1:1", "document:print:1:*"] | true
		["document:view:1:1", "document:print:1:1"] | true
		["document:view:*"]                         | false
		["document:view:1:*"]                       | false
		["document:view:*:1"]                       | false
		["document:view:1:1"]                       | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
	}

	@Unroll("Testing if user with permission #permission can sign document:#groupId:#documentId")
	def "canSign tests"() {
		def user = newUser([permission])
		def document = newDocument(documentId: documentId, groupId: groupId)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canSign(document) == result

		where:
		permission          | groupId | documentId | result
		"*"                 | 1L      | 1L         | true
		"*"                 | 2L      | 1L         | true
		"document:sign:*"   | 1L      | 1L         | true
		"document:sign:*"   | 2L      | 1L         | true
		"document:sign:1"   | 1L      | 1L         | true
		"document:sign:1"   | 2L      | 1L         | false
		"document:sign:*:1" | 1L      | 1L         | true
		"document:sign:*:1" | 2L      | 1L         | true
		"document:sign:1:*" | 1L      | 1L         | true
		"document:sign:1:*" | 2L      | 1L         | false
		"document:sign:1:1" | 1L      | 1L         | true
		"document:sign:1:1" | 2L      | 1L         | false
		// Revisit sometime
		// "document:*:1:1"      | 1L      | 1L         | true
	}

	@Unroll("Testing if user with permissions #permissions can sign any document")
	def "canSignAnyDocument tests"() {
		def user = newUser(permissions)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canSignAnyDocument() == result

		where:
		permissions                                | result
		["*"]                                      | true
		["document:view:1:1", "*"]                 | true
		["document:view:1:1", "document:sign:*:1"] | true
		["document:view:1:1", "document:sign:1:*"] | true
		["document:view:1:1", "document:sign:1:1"] | true
		["document:view:*"]                        | false
		["document:view:1:*"]                      | false
		["document:view:*:1"]                      | false
		["document:view:1:1"]                      | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
	}

	@Unroll("Testing if user with permission #permission can manager folders in group #groupId")
	def "canManageFolders tests"() {
		def user = newUser([permission])
		def group = new Group()
		group.id = groupId
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canManageFolders(group) == result

		where:
		permission                   | groupId  | result
		"*"                          | 1L       | true
		"*"                          | 2L       | true
		"document:managefolders:*"   | 1L       | true
		"document:managefolders:*"   | 2L       | true
		"document:managefolders:1"   | 1L       | true
		"document:managefolders:1"   | 2L       | false
	}

	@Unroll("Testing if user with permission #permission can upload to group #groupId")
	def "canUpload tests"() {
		def user = newUser([permission])
		def group = new Group()
		group.id = groupId
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canUpload(group) == result

		where:
		permission            | groupId | result
		"*"                   | 1L      | true
		"*"                   | 2L      | true
		"document:upload:*"   | 1L      | true
		"document:upload:*"   | 2L      | true
		"document:upload:1"   | 1L      | true
		"document:upload:1"   | 2L      | false
		"document:upload:1:*" | 1L      | true
		"document:upload:1:*" | 2L      | false
		"document:upload:1:1" | 1L      | false
		"document:upload:1:1" | 2L      | false
		// Revisit sometime
		// "document:*:1:1"      | 1L     true
	}

	@Unroll("Testing if user with permissions #permissions can upload any document")
	def "canUploadAnyGroup tests"() {
		def user = newUser(permissions)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canUploadAnyGroup() == result

		where:
		permissions                                  | result
		["*"]                                        | true
		["document:view:1:1", "*"]                   | true
		["document:view:1:1", "document:upload:*:1"] | true
		["document:view:1:1", "document:upload:1:*"] | true
		["document:view:1:1", "document:upload:1:1"] | true
		["document:view:*"]                          | false
		["document:view:1:*"]                        | false
		["document:view:*:1"]                        | false
		["document:view:1:1"]                        | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:view:1:1", "document:*:1:1"]      | true
	}

	@Unroll("Testing if user with permission #permission can view document:#groupId:#documentId")
	def "canView tests"() {
		def user = newUser([permission])
		def document = newDocument(documentId: documentId, groupId: groupId)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canView(document) == result

		where:
		permission          | groupId | documentId | result
		"*"                 | 1L      | 1L         | true
		"*"                 | 2L      | 1L         | true
		"document:view:*"   | 1L      | 1L         | true
		"document:view:*"   | 2L      | 1L         | true
		"document:view:1"   | 1L      | 1L         | true
		"document:view:1"   | 2L      | 1L         | false
		"document:view:*:1" | 1L      | 1L         | true
		"document:view:*:1" | 2L      | 1L         | true
		"document:view:1:*" | 1L      | 1L         | true
		"document:view:1:*" | 2L      | 1L         | false
		"document:view:1:1" | 1L      | 1L         | true
		"document:view:1:1" | 2L      | 1L         | false
		// Revisit sometime
		// "document:*:1:1"      | 1L      | 1L         | true
	}

	@Unroll("Testing if user with permissions #permissions can view any document")
	def "canViewAnyDocument tests"() {
		def user = newUser(permissions)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }

		expect:
		service.canViewAnyDocument() == result

		where:
		permissions                                  | result
		["*"]                                        | true
		["document:delete:1:1", "*"]                 | true
		["document:delete:1:1", "document:view:*:1"] | true
		["document:delete:1:1", "document:view:1:*"] | true
		["document:delete:1:1", "document:view:1:1"] | true
		["document:delete:*"]                        | false
		["document:delete:1:*"]                      | false
		["document:delete:*:1"]                      | false
		["document:delete:1:1"]                      | false
		// This should probably work but we don't use this form.  Need to revisit sometime
		// ["document:delete:1:1", "document:*:1:1"]      | true
	}

	def "getGroupsWithPermissions should return the empty set by default"() {
		given:
		service.metaClass.isLoggedIn = { false }

		when:
		def result = service.getGroupsWithPermission([DocumentPermission.Upload])

		then:
		result instanceof SortedSet
		result.empty
	}

	def "getGroupsWithPermissions should return all groups for admins"() {
		given:
		(1..10).each {
			new Group(name: "group$it").save()
		}
		service.metaClass.isLoggedIn = { true }
		service.metaClass.isAdmin = { true }

		expect:
		service.getGroupsWithPermission([DocumentPermission.Upload]) == Group.list() as SortedSet
	}

	def "getGroupsWithPermissions should be more selective for users"() {
		given:
		def groups = (0..4).collect {
			new Group(name: "group$it").save()
		}
		def user = newUser(["document:upload:${groups[0].id}", "document:view:${groups[1].id}:*",
				"document:sign:${groups[2].id}:*", "document:view:${groups[3].id}:1"])
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }
		service.metaClass.isLoggedIn = { true }
		service.metaClass.isAdmin = { false }
		service.metaClass.getAuthenticatedUser = { user }

		when:
		def result = service.getGroupsWithPermission([DocumentPermission.View])

		then:
		// This shouldn't return the last one from the permissions list because the user doesn't have perm to the group
		result == [groups[1]] as SortedSet
	}

	def "getIndividualDocumentsWithPermission should return the empty set by default"() {
		given:
		service.metaClass.isLoggedIn = { false }

		when:
		def result = service.getIndividualDocumentsWithPermission([DocumentPermission.Upload])

		then:
		result instanceof Set
		result.empty
	}

	def "getIndividualDocumentsWithPermission should return the individual documents for a user"() {
		def groups = (0..4).collect {
			new Group(name: "group$it").save()
		}
		def user = newUser(["document:upload:${groups[0].id}", "document:view:${groups[1].id}:*",
				"document:sign:${groups[2].id}:*", "document:view:${groups[3].id}:1"])
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }
		service.metaClass.isLoggedIn = { true }
		service.metaClass.isAdmin = { false }
		service.metaClass.getAuthenticatedUser = { user }

		when:
		def result = service.getIndividualDocumentsWithPermission([DocumentPermission.View])

		then:
		result == [1L] as Set
	}

	def "getIndividualDocumentsWithPermission should return the individual documents for a role"() {
		def role = new Role(name: "rolePerm").save()
		def user = newUser()
		def permission = new Permission(managed: true, type: Permission.defaultPerm, target: "document:view:42:42")
		role.addToPermissions(permission)
		role.save()
		roleService.addMember(user, role)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }
		service.metaClass.isLoggedIn = { true }
		service.metaClass.isAdmin = { false }
		service.metaClass.getAuthenticatedUser = { user }

		when:
		def result = service.getIndividualDocumentsWithPermission([DocumentPermission.View])

		then:
		result == [42L] as Set
	}

	def "getIndividualDocumentsWithPermission should return the individual documents for a group"() {
		def role = new Role(name: "rolePerm").save()
		def permission = new Permission(managed: true, type: Permission.defaultPerm, target: "document:view:13:13")
		role.addToPermissions(permission)
		role.save()
		def group = new Group(name: "groupPerm").save()
		def user = newUser()
		permission = new Permission(managed: true, type: Permission.defaultPerm, target: "document:view:42:42")
		group.addToPermissions(permission)
		group.save()
		groupService.addMember(user, group)
		roleService.addGroupMember(group, role)
		subject.@principals = new SimplePrincipalCollection(user.id, "localizedRealm")
		subject.metaClass.getPrincipal = { user.id }
		subject.metaClass.isAuthenticated = { true }
		service.metaClass.isLoggedIn = { true }
		service.metaClass.isAdmin = { false }
		service.metaClass.getAuthenticatedUser = { user }

		when:
		def result = service.getIndividualDocumentsWithPermission([DocumentPermission.View])

		then:
		result == [13L, 42L] as Set
	}
}
