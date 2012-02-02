package us.paperlesstech.helpers

import java.util.concurrent.Callable

import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject

import us.paperlesstech.nimble.User

class ShiroHelpers {

	static void runas(def principle, String realm, Closure c) {
		PrincipalCollection principals = new SimplePrincipalCollection(principle, realm)
		Subject subject = new Subject.Builder().principals(principals).buildSubject()
		subject.execute(c as Callable)
	}

	static void runas(User user, String realm="localized", Closure c) {
		assert user
		assert c

		runas(user.id, realm, c)
	}
}
