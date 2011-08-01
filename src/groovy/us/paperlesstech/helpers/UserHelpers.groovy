package us.paperlesstech.helpers

class UserHelpers {
	static final List validChars = ["abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "123457890", '!@#$%&+=-']

	static String generatePassword(int minLength) {
		Random rnd = new Random()

		def password = []

		while (password.size() < minLength) {
			validChars.each {str ->
				password << str.charAt(rnd.nextInt(str.size()))
			}
		}

		Collections.shuffle(password, rnd)

		return password.join()
	}
}
