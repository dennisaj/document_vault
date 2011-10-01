package us.paperlesstech

import us.paperlesstech.nimble.Group

class BucketService {
	static transactional = true

	def authService

	/**
	 * Creates and returns bucket with the given name.
	 * If the bucket already exists, it is returned.
	 *
	 * @pre The current user must have the {@link BucketPermission#Create} permission for document.
	 * @throws RuntimeException if there is a problem saving
	 *
	 * @return The saved bucket if there were no problems or a bucket with errors if validation fails
	 */
	Bucket createBucket(Group group, String name) {
		assert group
		assert authService.canCreateBucket(group)

		def bucket = new Bucket(group:group, name:name)

		if (!bucket.validate()) {
			log.debug "Bucket failed validation"
			return bucket
		}

		bucket.save(failOnError:true)
	}

	/**
	 * Removes the folder from the given bucket then deletes it.
	 *
	 * @pre The current user must have the {@link BucketPermission#Delete} permission for document.
	 * @throws RuntimeException if there is a problem saving
	 */
	def deleteBucket(Bucket bucket) {
		assert bucket
		assert authService.canDelete(bucket)

		if (bucket.folders) {
			def folders = []
			folders.addAll(bucket.folders)
			folders.each {
				bucket.removeFromFolders(it)
			}
		}

		bucket.delete(flush:true)
	}

	/**
	 * Moves the given folder out of its current bucket (if applicable)
	 * and then moves it into the given bucket.
	 *
	 * @pre The current user must have the {@link BucketPermission#MoveInTo} permission for destination.
	 * If folder.bucket is set, the user must also have {@link BucketPermission#MoveOutOf} permission for the current bucket.
	 * @pre folder.group must equal destination.group
	 * @throws RuntimeException if there is a problem saving
	 */
	Folder addFolderToBucket(Bucket destination, Folder folder) {
		assert destination
		assert folder

		if (folder.bucket == destination) {
			return folder
		}

		assert folder.group == destination.group
		assert authService.canMoveInTo(destination)
		assert !folder.bucket || authService.canMoveOutOf(folder.bucket)

		folder.bucket?.removeFromFolders(folder)
		destination.addToFolders(folder)
		destination.save(failOnError:true)
		folder
	}

	/**
	 * Removes of the folder from its current bucket and sets its bucket to null.
	 *
	 * If folder.bucket is null, nothing happens.
	 *
	 * @pre The current user must have the {@link BucketPermission#MoveOut} permission for document.
	 * @throws RuntimeException if there is a problem saving
	 */
	Folder removeFolderFromBucket(Folder folder) {
		assert folder
		assert !folder.bucket || authService.canMoveOutOf(folder.bucket)

		def bucket = folder.bucket
		bucket?.removeFromFolders(folder)
		bucket?.save(failOnError:true)
		folder
	}
}
