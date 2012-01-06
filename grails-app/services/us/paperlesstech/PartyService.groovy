package us.paperlesstech

import java.text.ParseException

import org.codehaus.groovy.grails.web.json.JSONObject

import us.paperlesstech.nimble.Permission
import us.paperlesstech.nimble.Profile
import us.paperlesstech.nimble.User

class PartyService {
	static final String dateFormat = 'MM/dd/yyyy'
	static transactional = true

	def authService
	def grailsApplication
	def handlerChain
	def highlightService
	def permissionService
	def requestService
	def userService

	/**
	 * Attempts to create a party for the given document.
	 *
	 * @pre The current user must have the {@link DocumentPermission#GetSigned} permission for document.
	 *
	 * @param p a Map of Party values (eg: [highlights:[list of highlights], color:..., permission:..., fullName:..., email:..., expiration:...])
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
			Permission documentPermission = new Permission(managed:true, type: Permission.defaultPerm, target:"document:${party.documentPermission.name().toLowerCase()}:*:${document.id}")
			permissionService.createPermission(documentPermission, party.signator)
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
			party = new Party(signator:user, documentPermission:DocumentPermission.Sign, document:document)
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
		} else if (party.color) {
			signatures.each { p, s ->
				s*.color = party.color.name()
			}
		}

		markAccepted(party)

		handlerChain.cursiveSign(document: document, documentData: document.files.first(), signatures:signatures)
		def savedDocument = document.save()
		if (savedDocument) {
			return savedDocument
		}

		document.errors.each {
			log.error "[Document(${document.id})] - " + it
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
	 * @throws org.grails.mail.GrailsMailException If the mail plugin encounters an error.
	 * @throws java.lang.IllegalArgumentException If a bad argument is passed to the mail plugin.
	 */
	Party sendCode(Party party) {
		assert party
		assert authService.canGetSigned(party.document)

		sendMail {
			to party.signator.profile.email
			subject "Document Vault Signature Code" // TODO: i18n
			body(view: "/email", model: [party: party, baseAddr: requestService.baseAddr])
		}

		if (!party.sent) {
			party.sent = true
			def savedParty = party.save()
			if (!savedParty) {
				throw new RuntimeException("Unable to mark Party(${party.id}) sent.")
			}

			party = savedParty
		}

		return party
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
	 * Iterates over the inParties list and either updates the highlights if the party already exists
	 * or creates a new party and adds it to the document
	 *
	 * @param inParties a List of Maps (see createParty)
	 * @return a List of Party objects that each may have error information associated with them
	 *
	 * @see #createParty(Document, Map)
	 * @see #updateHighlights(Party, List)
	 */
	List submitParties(Document document, List inParties) {
		assert document
		assert authService.canGetSigned(document)

		inParties.collect { inParty->
			// Remove JSONObject.Null entries
			inParty = inParty.collectEntries { k, v->
				if (v == JSONObject.NULL) {
					v = null
				}

				[(k): v]
			}

			def outParty = null
			if (inParty.id) {
				def party = Party.get(inParty.id as long)

				if (party) {
					outParty = updateHighlights(party, inParty.highlights)
				}
			} else {
				outParty = createParty(document, inParty)
				if (outParty.hasErrors() || outParty.signator.hasErrors()) {
					// If there was an error, use the existing code.
					// This ensures that the unsaved clientside highlights won't disappear.
					outParty.code = inParty.code
				}
			}

			outParty
		}
	}

	/**
	 * @return An existing User with the given email or a new User with the given name and email address.
	 */
	private User getSignator(String fullName, String email) {
		def profile = Profile.findByEmail(email)

		profile ? profile.owner : userService.createUser(username: email, fullName: fullName, email: email, addSignatorRole: true)
	}
}
