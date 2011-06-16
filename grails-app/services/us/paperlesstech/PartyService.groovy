package us.paperlesstech

import grails.plugins.nimble.InstanceGenerator
import grails.plugins.nimble.core.Permission
import grails.plugins.nimble.core.Role

import java.text.ParseException

class PartyService {
	static final String dateFormat = 'MM/dd/yyyy'
	static final List validChars = ["abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "123457890", '!@#$%&+=-']
	static transactional = true

	def authService
	def grailsApplication
	def permissionService
	def roleService
	def userService

	/**
	 * Attempts to create a party for the given document. 
	 * 
	 * @pre The current user must have the {@link DocumentPermission#GetSigned} permission for document.
	 * 
	 * @return The Party object, with errors populated if any occur.
	 */
	Party createParty(Document document, Map p) {
		assert authService.canGetSigned(document)

		def dateParseError = false
		def party = new Party()

		party.document = document
		party.signator = getSignator(p.fullName, p.email)
		party.color = p.color ? PartyColor.valueOf(p.color) : null
		party.documentPermission = p.permission ? DocumentPermission.valueOf(p.permission) : null

		try {
			party.expiration = p.expiration ? Date.parse(dateFormat, p.expiration) : null
		} catch (ParseException e) {
			dateParseError = true
		}

		party.highlights = Highlight.fromJsonList(party, p.highlights)

		party.validate()

		// Add this error here because validate wipes existing errors
		if (dateParseError) {
			party.errors.rejectValue('expiration', 'party.expiration.invalidformat', [dateFormat] as Object[], 'Expiration date must be in the format {0}')
		}

		if (!party.signator.hasErrors() && !party.hasErrors()) {
			Permission profileEdit = new Permission(managed:true, type: Permission.defaultPerm, target:"document:${party.documentPermission}:*:${document.id}")
			permissionService.createPermission(profileEdit, party.signator)
			return party.save()
		}

		party
	}

	/**
	 * @pre The current user must have the {@link DocumentPermission#GetSigned} permission for party.document.
	 * 
	 * @throws RuntimeException is thrown if there is a failure saving the document or removing the permission from signator
	 */
	void removeParty(Party party) {
		assert party
		assert authService.canGetSigned(party.document)

		def document = party.document
		document.removeFromParties(party)

		def savedDocument = document.save()
		if (savedDocument) {
			def signator = party.signator
			def target = "document:${party.documentPermission}:*:${savedDocument.id}"

			party.delete(flush:true)

			Permission perm = Permission.findWhere(user:signator, managed:true, target:target)

			if (perm) {
				permissionService.deletePermission(perm)
			}

			return
		}

		throw new RuntimeException("Unable to remove Party(${party.id}) from Document(${document.id})")
	}

	/**
	 * Attempts to update the highlights on the given party. If there party is null, an Exception is raised.
	 * 
	 * @pre The current user must have the {@link DocumentPermission#GetSigned} permission for party.document.
	 *
	 */
	Party updateHighlights(Party party, List jsonHighlights) {
		assert party
		assert authService.canGetSigned(party.document)

		party.resetHighlights()
		party.highlights = Highlight.fromJsonList(party, jsonHighlights)
		def savedParty = party.save(flush:true)

		if (!savedParty) {
			log.error("Unable to update highlights for Party(${party.id})")
			party.errors.each {
				log.error it
			}

			return party
		}

		savedParty
	}

	/**
	 * @return An existing User with the given email or a new User with the given name and email address.
	 */
	private User getSignator(String fullName, String email) {
		def profile = Profile.findByEmail(email)

		profile ? profile.owner : createUser(fullName, email)
	}

	private User createUser(String fullName, String email) {
		def password = generatePassword()
		def user = InstanceGenerator.user()
		user.profile = InstanceGenerator.profile()

		user.username = email
		user.enabled = true
		user.external = false
		user.pass = password
		user.passConfirm = password

		user.profile.fullName = fullName
		user.profile.email = email
		user.profile.owner = user

		def savedUser = userService.createUser(user)
		if (savedUser.hasErrors()) {
			return user
		}

		roleService.addMember(savedUser, Role.findByName(User.SIGNATOR_USER_ROLE))
		savedUser
	}

	def generatePassword() {
		Random rnd = new Random()

		String password = ""

		while (password.length() < grailsApplication.config.nimble.passwords.minlength) {
			validChars.each {str->
				password += str.charAt(Math.abs(rnd.nextInt() % str.length()))
			}
		}

		def passwordList = password as List
		Collections.shuffle(passwordList, rnd)

		return passwordList.join()
	}
}
