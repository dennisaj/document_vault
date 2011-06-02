package us.paperlesstech

import grails.plugins.nimble.auth.WildcardPermission
import grails.plugins.nimble.core.AuthenticatedService
import grails.plugins.nimble.core.Group

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
		isPermissionImplied("document:sign")
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
		isPermissionImplied("document:upload:${group?.id}")
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

		if (!subject.authenticated) {
			return false
		}

		String pString = permission.name().toLowerCase()

		subject.isPermitted("document:$pString:${d.group.id}:${d.id}")
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
		if (!subject.authenticated) {
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
