package us.paperlesstech

import java.util.concurrent.Callable

import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject

class CodeController extends DocumentController {
	def beforeInterceptor = [action:this.&wrapper]

	def wrapper() {
		def party = Party.findByCode(params.documentId)
		assert party
		params.documentId = party.document.id

		PrincipalCollection principals = new SimplePrincipalCollection(party.signator.id, "localized")
		Subject subject = new Subject.Builder().principals(principals).buildSubject()
		subject.execute({
			this[params.action]()
		} as Callable)

		return false
	}
}
