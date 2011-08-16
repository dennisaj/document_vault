package us.paperlesstech

import us.paperlesstech.nimble.User

class Preference {
	String key
	String value

	static belongsTo = [user:User]

	static constraints = {
		key nullable: false, blank: false, unique: "user"
		value nullable: true, blank: true, maxSize: 4096
	}

	static mapping = {
		key column:"_key"
		value column:"_value"
	}

	@Override
	String toString() {
		"Preference($user, $key, $value)"
	}
}
