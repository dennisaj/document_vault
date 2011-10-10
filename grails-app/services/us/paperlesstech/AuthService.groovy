package us.paperlesstech

import java.util.concurrent.ConcurrentHashMap

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.subject.Subject

import us.paperlesstech.nimble.AdminsService
import us.paperlesstech.nimble.Group
import us.paperlesstech.nimble.Role
import us.paperlesstech.nimble.User

class AuthService {
	static transactional = false

	def grailsApplication
	protected Map permissionsCache = [:] as ConcurrentHashMap
	def testSubject

	boolean canCreateBucket(Group group) {
		assert group
		def subject = testSubject ?: authenticatedSubject

		if (!isLoggedIn()) {
			return false
		}

		String pString = "bucket:${BucketPermission.Create.name().toLowerCase()}:${group.id}"

		checkPermission(subject, pString)
	}

	boolean canDelete(Bucket b) {
		checkPermission(BucketPermission.Delete, b)
	}

	boolean canDelete(Document d) {
		checkPermission(DocumentPermission.Delete, d)
	}

	boolean canDeleteAnyBucket() {
		isPermissionImplied("bucket:delete")
	}

	boolean canDeleteAnyDocument() {
		isPermissionImplied("document:delete")
	}

	boolean canGetSigned(Document d) {
		grailsApplication.config.document_vault.remoteSigning.enabled && checkPermission(DocumentPermission.GetSigned, d)
	}

	boolean canMoveInTo(Bucket b) {
		checkPermission(BucketPermission.MoveInTo, b)
	}

	boolean canMoveInToAnyBucket() {
		isPermissionImplied("bucket:moveinto")
	}

	boolean canMoveOutOf(Bucket b) {
		checkPermission(BucketPermission.MoveOutOf, b)
	}

	boolean canMoveOutOfAnyBucket() {
		isPermissionImplied("bucket:moveoutof")
	}

	boolean canNotes(Document d) {
		checkPermission(DocumentPermission.Notes, d)
	}

	boolean canNotesAnyDocument() {
		isPermissionImplied("document:notes")
	}

	boolean canPrint(Document d) {
		// We cannot currently print non-pdf documents.
		d.files.first().mimeType == MimeType.PDF && checkPermission(DocumentPermission.Print, d)
	}

	boolean canPrintAnyDocument() {
		isPermissionImplied("document:print")
	}

	boolean canSign(Document d) {
		checkPermission(DocumentPermission.Sign, d)
	}

	boolean canSignAnyDocument() {
		isPermissionImplied("document:sign")
	}

	/**
	 * Returns true if the user can create folders in the given group.
	 */
	boolean canFolderCreate(Group group) {
		checkGroupPermission(DocumentPermission.FolderCreate, group)
	}

	/**
	 * Returns true if the user can delete folders in the given group.
	 */
	boolean canFolderDelete(Group group) {
		checkGroupPermission(DocumentPermission.FolderDelete, group)
	}

	/**
	 * Returns true if the user can move a document in to any folder in the given group.
	 */
	boolean canFolderMoveInTo(Group group) {
		checkGroupPermission(DocumentPermission.FolderMoveInTo, group)
	}

	/**
	 * Returns true if the user can move a document out of any folder in the given group.
	 */
	boolean canFolderMoveOutOf(Group group) {
		checkGroupPermission(DocumentPermission.FolderMoveOutOf, group)
	}

	boolean canUpload(Group group) {
		checkGroupPermission(DocumentPermission.Upload, group)
	}

	boolean canUploadAnyGroup() {
		isPermissionImplied("document:upload")
	}

	boolean canView(Document d) {
		checkPermission(DocumentPermission.View, d)
	}

	boolean canViewAnyDocument() {
		isPermissionImplied("document:view")
	}

	boolean canView(Bucket b) {
		checkPermission(BucketPermission.View, b)
	}

	boolean canViewAnyBucket() {
		isPermissionImplied("bucket:view")
	}

	boolean canRunAs(User u) {
		!authenticatedSubject?.isRunAs() && u in authenticatedUser?.delegators
	}

	boolean canRunAsAny() {
		!authenticatedSubject?.isRunAs() && !authenticatedUser?.delegators?.isEmpty()
	}

	private boolean checkPermission(DocumentPermission permission, Document d) {
		assert d
		def subject = testSubject ?: authenticatedSubject

		if (!isLoggedIn()) {
			return false
		}

		String pString = "document:${permission.name().toLowerCase()}:${d.group.id}:${d.id}"

		checkPermission(subject, pString)
	}

	private boolean checkPermission(BucketPermission permission, Bucket b) {
		assert b
		def subject = testSubject ?: authenticatedSubject

		if (!isLoggedIn()) {
			return false
		}

		String pString = "bucket:${permission.name().toLowerCase()}:${b.group.id}:${b.id}"

		checkPermission(subject, pString)
	}

	public boolean checkGroupPermission(DocumentPermission permission, Group g) {
		assert g
		def subject = testSubject ?: authenticatedSubject

		if (!isLoggedIn()) {
			return false
		}

		String pString = "document:${permission.name().toLowerCase()}:${g.id}"

		checkPermission(subject, pString)
	}

	public boolean checkGroupPermission(BucketPermission permission, Group g) {
		assert g
		def subject = testSubject ?: authenticatedSubject

		if (!isLoggedIn()) {
			return false
		}

		String pString = "bucket:${permission.name().toLowerCase()}:${g.id}"

		checkPermission(subject, pString)
	}

	private boolean checkPermission(def subject, String permission) {
		def user = authenticatedUser
		def cacheEntry = permissionsCache[authenticatedUser.id] ?: (permissionsCache[authenticatedUser.id] = new PermissionCacheEntry())
		def permitted = cacheEntry.permitted[permission]
		if (permitted == null) {
			permitted = subject.isPermitted(permission)

			// The parentheses around permission are required so that the value of the string is used as the key
			cacheEntry.permitted << [(permission): permitted]
		}

		permitted
	}

	/**
	 * Returns all groups the user can perform the given permissions on.
	 *
	 * @param permissions A list of permissions to test
	 * @return The SortedSet of all groups where the user can perform the any of the given permissions
	 */
	SortedSet getGroupsWithPermission(List<Enum> permissions) {
		if (!isLoggedIn()) {
			return [] as SortedSet
		}

		def groups = Group.list()

		(isAdmin() ? groups : groups?.findAll { Group group ->
			permissions.any { permission ->
				checkGroupPermission(permission, group)
			}
		}) as SortedSet
	}

	/**
	 * This is for getting the documents the user has one off permissions for.  For example this isn't for seeing if
	 * the user has document:view:1:* it is for collecting the 5 in document:view:1:5 or document:view:*:5
	 *
	 * @param permissions A list of permissions to test
	 * @return The set of all document ids where the user has specific permission to perform any of the indicated permissions
	 */
	Set getIndividualDocumentsWithPermission(List<DocumentPermission> permissions, Group searchGroup=null) {
		getIndividualItemsWithPermission(permissions, 'document', searchGroup)
	}

	/**
	 * This is for getting the buckets the user has one off permissions for. For example this isn't for seeing if
	 * the user has bucket:view:1:* it is for collecting the 5 in bucket:view:1:5 or bucket:view:*:5
	 *
	 * @param permissions A list of permissions to test
	 * @return The set of all bucket ids where the user has specific permission to perform any of the indicated permissions.
	 */
	Set getIndividualBucketsWithPermission(List<BucketPermission> permissions) {
		getIndividualItemsWithPermission(permissions, 'bucket')
	}

	private Set getIndividualItemsWithPermission(List<Enum> permissions, type, Group searchGroup=null) {
		if (!isLoggedIn()) {
			return [] as Set
		}

		def subject = authenticatedSubject

		if (testSubject) {
			// TODO add better testing for roles
			return true
		}

		def groupMatcher = searchGroup?.id ?: '\\d+|\\*'
		String pString = permissions*.name().join("|").toLowerCase()

		def matcher = ~/(?i)$type:(?:$pString):(?:$groupMatcher):(\d+)/
		def match
		def matches = [] as Set

		def user = authenticatedUser
		for (p in user.permissions) {
			if ((match = (p.target =~ matcher))) {
				matches << match[0][1]
			}
		}

		for (role in user.roles) {
			for (p in role.permissions) {
				if ((match = (p.target =~ matcher))) {
					matches << match[0][1]
				}
			}
		}

		for (group in user.groups) {
			for (role in group.roles) {
				for (p in role.permissions) {
					if ((match = (p.target =~ matcher))) {
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
	private boolean isPermissionImplied(String permission) {
		if (!isLoggedIn()) {
			return false
		}

		def subject = testSubject ?: authenticatedSubject
		def user = authenticatedUser
		def cacheEntry = permissionsCache[authenticatedUser.id] ?: (permissionsCache[authenticatedUser.id] = new PermissionCacheEntry())

		def permitted = cacheEntry.implied[permission]
		if (permitted != null) {
			return permitted
		}

		if (testSubject) {
			return true
		}

		def wp = new WildcardPermission(permission)

		for (p in user.permissions) {
			if (wp.implies(new WildcardPermission(p.target))) {
				cacheEntry.implied << [(permission): true]
				return true
			}
		}

		for (role in user.roles) {
			for (p in role.permissions) {
				if (wp.implies(new WildcardPermission(p.target))) {
					cacheEntry.implied << [(permission): true]
					return true
				}
			}
		}

		for (group in user.groups) {
			for (role in group.roles) {
				for (p in role.permissions) {
					if (wp.implies(new WildcardPermission(p.target))) {
						cacheEntry.implied << [(permission): true]
						return true
					}
				}
			}

			for (p in group.permissions) {
				if (wp.implies(new WildcardPermission(p.target))) {
					cacheEntry.implied << [(permission): true]
					return true
				}
			}
		}

		return checkPermission(subject, permission)
	}

	Subject getAuthenticatedSubject() {
		SecurityUtils.subject
	}

	User getAuthenticatedUser() {
		def principal = authenticatedSubject?.principal
		def authUser = User.get(principal)

		if (!authUser) {
			log.info "Authenticated user was not able to be obtained from metaclass"
			return null
		}

		authUser
	}

	boolean isLoggedIn() {
		def subject = testSubject ?: authenticatedSubject

		subject?.authenticated || subject?.remembered
	}

	boolean isAdmin() {
		def subject = testSubject ?: authenticatedSubject

		subject?.hasRole(AdminsService.ADMIN_ROLE)
	}

	/**
	 *
	 * @return The base user if the running as a delegate.
	 */
	User getDelegateUser() {
		def p = authenticatedSubject.previousPrincipals?.iterator()?.next()
		if (p) {
			return User.get(p)
		}

		return null
	}

	void login(token) {
		SecurityUtils.subject.login(token)
		// reset permissions on login
		resetCache(authenticatedUser)
	}

	void logout() {
		// reset permissions on logout
		resetCache(authenticatedUser)
		SecurityUtils.subject?.logout()
	}

	/**
	 * Reset the permission cache entry for given user.
	 */
	void resetCache(User user) {
		permissionsCache.remove(user?.id)
	}

	/**
	 * Reset the permission cache entry for each of the users in the given role.
	 */
	void resetCache(Role role) {
		role?.users?.each { user->
			resetCache(user)
		}

		role?.groups?.each { group->
			group.users?.each { user->
				resetCache(user)
			}
		}
	}

	/**
	 * Reset the permission cache entry for each of the users in the given group.
	 */
	void resetCache(Group group) {
		group?.users?.each { user->
			resetCache(user)
		}

		group?.roles?.each { role->
			role.users?.each { user->
				resetCache(user)
			}
		}
	}
}

class PermissionCacheEntry {
	Map implied = [:] as ConcurrentHashMap
	Map permitted = [:] as ConcurrentHashMap
}
