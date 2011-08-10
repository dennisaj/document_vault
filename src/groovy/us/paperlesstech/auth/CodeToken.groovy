package us.paperlesstech.auth

import org.apache.shiro.authc.AuthenticationToken

class CodeToken implements AuthenticationToken {
	String code

	@Override
	public Object getPrincipal() {
		code
	}

	@Override
	public Object getCredentials() {
		code
	}
}
