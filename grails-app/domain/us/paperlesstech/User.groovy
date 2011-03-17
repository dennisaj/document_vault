package us.paperlesstech

class User {

	String username
	String userPassword
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static constraints = {
		username blank: false, unique: true
		userPassword blank: false
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

	String toString() {
		"User(${id}) - ${username}"
	}
}
