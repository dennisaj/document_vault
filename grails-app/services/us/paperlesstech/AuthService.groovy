package us.paperlesstech

import grails.plugins.nimble.auth.WildcardPermission
import grails.plugins.nimble.core.AuthenticatedService
import grails.plugins.nimble.core.Group
import grails.plugins.nimble.core.AdminsService

class AuthService extends AuthenticatedService {
	static transactional = false
	def testSubject

	boolean canDelete(Document d) {
		checkPermission(DocumentPermission.Delete, d)
	}

	boolean canGetSigned(Document d) {
		checkPermission(DocumentPermission.GetSigned, d)
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

		String pString = permission.name().toLowerCase()

		subject.isPermitted("document:$pString:${d.group.id}:${d.id}")
	}

	boolean checkPermission(DocumentPermission permission, Group g) {
		assert g
		def subject = testSubject ?: getAuthenticatedSubject()

		if (!isLoggedIn()) {
			return false
		}

		String pString = permission.name().toLowerCase()

		subject.isPermitted("document:$pString:${g.id}")
	}

	/**
	 * Returns all groups the user can perform the given permission on.
	 *
	 * @param permission The permission to test
	 * @return The set of all groups where the user can perform the indicated permission
	 */
	Set getGroupsWithPermission(DocumentPermission permission) {
		def user = authenticatedUser
		def subject = authenticatedSubject

		if (!isLoggedIn()) {
			return [] as Set
		}

		def groups = Group.list()

		subject.hasRole(AdminsService.ADMIN_ROLE) ? groups : groups?.findAll {
			checkPermission(permission, it)
		}
	}

	/**
	 * This is for getting the documents the user has one off permissions for.  For example this isn't for seeing if
	 * the user has document:view:1:* it is for collecting the 5 in document:view:1:5
	 *
	 * @param permission The permission to test
	 * @return The set of all document ids where the user has specific permission to perform the indicated permission
	 */
	Set getIndividualDocumentsWithPermission(DocumentPermission permission) {
		def subject = authenticatedSubject

		if (!isLoggedIn()) {
			return [] as Set
		}

		if (testSubject) {
			// TODO add better testing for roles
			return true
		}

		String pString = permission.name().toLowerCase()
		def matcher = ~/document:$pString:\d+:(\d+)/
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

		if (testSubject) {
			// TODO add better testing for roles
			return true
		}

		def wp = new WildcardPermission(permission)

		def user = authenticatedUser
		for (p in user.permissions) {
			if (wp.implies(new WildcardPermission(p.target))) {
				return true
			}
		}

		for (role in user.roles) {
			for (p in role.permissions) {
				if (wp.implies(new WildcardPermission(p.target))) {
					return true
				}
			}
		}

		for (group in user.groups) {
			for (role in group.roles) {
				for (p in role.permissions) {
					if (wp.implies(new WildcardPermission(p.target))) {
						return true
					}
				}
			}

			for (p in group.permissions) {
				if (wp.implies(new WildcardPermission(p.target))) {
					return true
				}
			}
		}

		return subject.isPermitted(wp)
	}
}
