package us.paperlesstech

import us.paperlesstech.nimble.User

class PreferenceService {
	static final String DEFAULT_UPLOAD_GROUP = "us.paperlesstech.defaultUploadGroup"
	static final String DEFAULT_PRINTER = "us.paperlesstech.defaultPrinter"

	static transactional = true

	def authService

	/**
	 * If a preference with the given key is found, that value is updated.
	 * If no existing preference matches the passed in key, a new preference is added.
	 */
	boolean setPreference(User user, String key, String value) {
		assert authService.authenticatedSubject.isPermitted("profile:edit:$user.id")

		def preference = user.preferences.find { it.key == key }

		if (preference) {
			preference.value = value
		} else {
			preference = new Preference(user:user, key:key, value:value)
			user.addToPreferences(preference)
		}

		if (!user.save()) {
			log.error "Error setting a user's preference: $user, Key($key), Value($value)"
			user.errors.each {
				log.error it
			}
			return false
		}

		true
	}

	/**
	 * @param key They key to look up
	 * @return The value or null if the key does not exist
	 */
	def getPreference(User user, String key) {
		assert authService.authenticatedSubject.isPermitted("profile:edit:$user.id")

		user.preferences.find { it.key == key }?.value
	}
}
