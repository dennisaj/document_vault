package us.paperlesstech

import grails.plugins.nimble.auth.WildcardPermission
import grails.plugins.nimble.core.AdminsService
import grails.plugins.nimble.core.AuthenticatedService
import grails.plugins.nimble.core.Group

import java.util.concurrent.ConcurrentHashMap

class AuthService extends AuthenticatedService {
	static scope = "request"
	static transactional = false

	Map permissionsCache = [implied:[:], permitted: [:]] as ConcurrentHashMap
	def testSubject

	boolean canDelete(Document d) {
		checkPermission(DocumentPermission.Delete, d)
	}

	boolean canGetSigned(Document d) {
		grailsApplication.config.document_vault.remoteSigning.enabled && checkPermission(DocumentPermission.GetSigned, d)
	}

	boolean canNotes(Document d) {
		checkPermission(DocumentPermission.Notes, d)
	}

	boolean canNotesAny() {
		isPermissionImplied("document:notes")
	}

	boolean canPrint(Document d) {
		checkPermission(DocumentPermission.Notes, d)
	}

	boolean canPrintAny() {
		isPermissionImplied("document:print")
	}

	boolean canSign(Document d) {
		checkPermission(DocumentPermission.Sign, d)
	}

	boolean canSignAny() {
		isPermissionImplied("document:sign")
	}

	boolean canTag(Document d) {
		checkPermission(DocumentPermission.Tag, d)
	}

	boolean canTagAny() {
		isPermissionImplied("document:tag")
	}

	boolean canUpload(Group group) {
		checkPermission(DocumentPermission.Upload, group)
	}

	boolean canUploadAny() {
		isPermissionImplied("document:upload")
	}

	boolean canView(Document d) {
		checkPermission(DocumentPermission.View, d)
	}

	boolean canViewAny() {
		isPermissionImplied("document:view")
	}

	boolean checkPermission(DocumentPermission permission, Document d) {
		assert d
		def subject = testSubject ?: getAuthenticatedSubject()

		if (!isLoggedIn()) {
			return false
		}

		String pString = "document:${permission.name().toLowerCase()}:${d.group.id}:${d.id}"

		checkPermission(subject, pString)
	}

	boolean checkPermission(DocumentPermission permission, Group g) {
		assert g
		def subject = testSubject ?: getAuthenticatedSubject()

		if (!isLoggedIn()) {
			return false
		}

		String pString = "document:${permission.name().toLowerCase()}:${g.id}"

		checkPermission(subject, pString)
	}

	boolean checkPermission(def subject, String permission) {
		def permitted = permissionsCache.permitted[permission]
		if (permitted == null) {
			permitted = subject.isPermitted(permission)

			// The parentheses around permission are required so that the value of the string is used as the key
			permissionsCache.permitted << [(permission):permitted]
		}

		permitted
	}

	/**
	 * Returns all groups the user can perform the given permission on.
	 *
	 * @param permission The permission to test
	 * @return The set of all groups where the user can perform the indicated permission
	 */
	Set getGroupsWithPermission(List<DocumentPermission> permissions) {
		def user = authenticatedUser
		def subject = authenticatedSubject

		if (!isLoggedIn()) {
			return [] as Set
		}

		def groups = Group.list()

		subject.hasRole(AdminsService.ADMIN_ROLE) ? groups : groups?.findAll {group->
			permissions.any {permission->
				checkPermission(permission, group)
			}
		}
	}

	/**
	 * This is for getting the documents the user has one off permissions for.  For example this isn't for seeing if
	 * the user has document:view:1:* it is for collecting the 5 in document:view:1:5 or document:view:*:5
	 *
	 * @param permission The permission to test
	 * @return The set of all document ids where the user has specific permission to perform the indicated permission
	 */
	Set getIndividualDocumentsWithPermission(List<DocumentPermission> permissions) {
		def subject = authenticatedSubject

		if (!isLoggedIn()) {
			return [] as Set
		}

		if (testSubject) {
			// TODO add better testing for roles
			return true
		}

		String pString = permissions*.name().join("|").toLowerCase()
		def matcher = ~/(?i)document:(?:$pString):(?:\d+|\*):(\d+)/
		def match
		def matches = [] as Set

		def user = authenticatedUser
		for (p in user.permissions) {
			if((match = (p.target =~ matcher))) {
				matches << match[0][1]
			}
		}

		for (role in user.roles) {
			for (p in role.permissions) {
				if((match = (p.target =~ matcher))) {
					matches << match[0][1]
				}
			}
		}

		for (group in user.groups) {
			for (role in group.roles) {
				for (p in role.permissions) {
					if((match = (p.target =~ matcher))) {
						matches << match[0][1]
					}
				}
			}

			for (p in group.permissions) {
				if ((match = (p.target =~ matcher))) {
					matches << match[0][1]
				}
			}
		}

		matches ? matches.collect { it.toLong() } : [] as Set
	}

	/**
	 * Checks to see if this permission implies any of the users permissions.  This is mainly used for checking to
	 * see if a user has a permission on any document.
	 *
	 * @param permission The permission to imply against the users permissions
	 * @return true if the permission was implied
	 */
	boolean isPermissionImplied(String permission) {
		def subject = testSubject ?: getAuthenticatedSubject()
		if (!isLoggedIn()) {
			return false
		}

		def permitted = permissionsCache.implied[permission]
		if (permitted != null) {
			return permitted
		}

		if (testSubject) {
			// TODO add better testing for roles
			return true
		}

		def wp = new WildcardPermission(permission)

		def user = authenticatedUser
		for (p in user.permissions) {
			if (wp.implies(new WildcardPermission(p.target))) {
				permissionsCache.implied << [(permission):true]
				return true
			}
		}

		for (role in user.roles) {
			for (p in role.permissions) {
				if (wp.implies(new WildcardPermission(p.target))) {
					permissionsCache.implied << [(permission):true]
					return true
				}
			}
		}

		for (group in user.groups) {
			for (role in group.roles) {
				for (p in role.permissions) {
					if (wp.implies(new WildcardPermission(p.target))) {
						permissionsCache.implied << [(permission):true]
						return true
					}
				}
			}

			for (p in group.permissions) {
				if (wp.implies(new WildcardPermission(p.target))) {
					permissionsCache.implied << [(permission):true]
					return true
				}
			}
		}

		return checkPermission(subject, permission)
	}
}
