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
	def handlerChain
	def highlightService
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
		party.signator = p.signator ?: getSignator(p.fullName, p.email)
		party.color = p.color ? PartyColor.valueOf(p.color) : null
		party.documentPermission = p.permission ? DocumentPermission.valueOf(p.permission) : null

		try {
			party.expiration = p.expiration ? Date.parse(dateFormat, p.expiration) : null
		} catch (ParseException e) {
			dateParseError = true
		}

		party.highlights = highlightService.fromJsonList(party, p.highlights)

		party.validate()

		// Add this error here because validate wipes existing errors
		if (dateParseError) {
			party.errors.rejectValue('expiration', 'party.expiration.invalidformat', [dateFormat] as Object[], 'Expiration date must be in the format {0}')
		}

		if (!party.signator.hasErrors() && !party.hasErrors()) {
			Permission profileEdit = new Permission(managed:true, type: Permission.defaultPerm, target:"document:${party.documentPermission.name().toLowerCase()}:*:${document.id}")
			permissionService.createPermission(profileEdit, party.signator)
			def savedParty = party.save()
			if (savedParty) {
				return sendCode(savedParty)
			}
		}

		party
	}

	/**
	 * Applies the signatures to the document for the currently logged in user.
	 *
	 * If the user is not currently a party on the document, they are added and a 0 width 
	 * highlight is added to that party. No email should be sent
	 *
	 * @pre The current user must have the {@link DocumentPermission#Sign} permission for document.
	 *
	 * @return A saved Document or a document containing errors.
	 *
	 * @throws RuntimeException If there is a error saving the Party.
	 */
	Document cursiveSign(Document document, Map signatures) {
		assert document
		assert signatures
		assert authService.canSign(document)

		def user = authService.authenticatedUser
		Party party = document.parties.find { it.signator == user }

		if (!party) {
			party = new Party(signator:user, color:PartyColor.Yellow, documentPermission:DocumentPermission.Sign, document:document)
			def highlight = new Highlight(pageNumber:1, party:party)
			party.highlights = [highlight]
			document.addToParties(party)

			def savedParty = party.save()
			if (!savedParty) {
				party.errors.each {
					log.error it
				}

				throw new RuntimeException("Unable to create party for the current user.")
			}
			party = savedParty
		}

		markAccepted(party)

		handlerChain.cursiveSign(document: document, documentData: document.files.first(), signatures:signatures)
		def savedDocument = document.save()
		if (savedDocument) {
			return savedDocument
		}

		document
	}

	/**
	 * Set all {@link Highlight#accepted} dates to now.
	 *
	 * @pre The current user must have either the {@link DocumentPermission#GetSigned} or 
	 * {@link DocumentPermission#Sign} permission for party.document.
	 *
	 * @return The updated Party.
	 *
	 * @throws RuntimeException If there is a error saving the Party.
	 */
	Party markAccepted(Party party) {
		assert party
		assert authService.canSign(party.document) || authService.canGetSigned(party.document)

		if (!party.rejected) {
			party.highlights = party.highlights.collect {highlight->
				highlightService.markAccepted(highlight)
			}
		}

		party
	}

	/**
	 * Set the {@link Party#rejected} flag to true.
	 * 
	 * @pre The current user must have either the {@link DocumentPermission#GetSigned} or 
	 * {@link DocumentPermission#Sign} permission for party.document.
	 *
	 * @return The updated Party.
	 *
	 * @throws RuntimeException If there is a error saving the Party.
	 */
	Party markRejected(Party party) {
		assert party
		assert authService.canSign(party.document) || authService.canGetSigned(party.document)

		party.rejected = true
		def savedParty = party.save()
		if (savedParty) {
			return savedParty
		}

		throw new RuntimeException("Unable to mark Party(${party.id}) rejected.")
	}

	/**
	 * Set the {@link Party#viewed} flag to true.
	 *
	 * @pre The current user must have either the {@link DocumentPermission#GetSigned} or 
	 * {@link DocumentPermission#Sign} permission for party.document.
	 *
	 * @return The updated Party.
	 *
	 * @throws RuntimeException If there is a error saving the Party.
	 */
	Party markViewed(Party party) {
		assert party
		assert authService.canSign(party.document) || authService.canGetSigned(party.document)

		party.viewed = true
		def savedParty = party.save()
		if (savedParty) {
			return savedParty
		}

		throw new RuntimeException("Unable to mark Party(${party.id}) viewed.")
	}

	/**
	 * @pre The current user must have the {@link DocumentPermission#GetSigned} permission for party.document.
	 *
	 * @throws RuntimeException If there is a error saving the document or removing the permission from signator.
	 */
	void removeParty(Party party) {
		assert party
		assert authService.canGetSigned(party.document)
		assert party.removable()

		def document = party.document
		document.removeFromParties(party)

		def savedDocument = document.save()
		if (savedDocument) {
			def signator = party.signator
			def target = "document:${party.documentPermission.name().toLowerCase()}:*:${savedDocument.id}"

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
	 * Sends the email with the party code and sets the {@link Party#sent} flag to true.
	 *
	 * @return The updated Party.
	 *
	 * @throws RuntimeException If there is a error saving the Party.
	 * @throws org.springframework.mail.MailException If the Spring mail library encounters an error.
	 * @throws grails.plugin.mail.GrailsMailException If the mail plugin encounters an error.
	 * @throws java.lang.IllegalArgumentException If a bad argument is passed to the mail plugin.
	 */
	Party sendCode(Party party) {
		assert party
		assert authService.canGetSigned(party.document)

		sendMail {
			to party.signator.profile.email
			subject "Document Vault Signature Code" // TODO: i18n
			body (view: "/email", model:[party:party])
		}

		if (!party.sent) {
			party.sent = true
			def savedParty = party.save()
			if (savedParty) {
				return savedParty
			}
		} else {
			return party
		}

		throw new RuntimeException("Unable to mark Party(${party.id}) sent.")
	}

	/**
	 * Attempts to update the highlights on the given party. If their party is null, an Exception is raised.
	 *
	 * @pre The current user must have the {@link DocumentPermission#GetSigned} permission for party.document.
	 *
	 */
	Party updateHighlights(Party party, List jsonHighlights) {
		assert party
		assert authService.canGetSigned(party.document)

		highlightService.fromJsonList(party, jsonHighlights)?.each {highlight->
			party.addToHighlights(highlight)
		}

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

		def password = []

		while (password.size() < grailsApplication.config.nimble.passwords.minlength) {
			validChars.each {str->
				password += str.charAt(Math.abs(rnd.nextInt() % str.length()))
			}
		}

		Collections.shuffle(password, rnd)

		return password.join()
	}
}
