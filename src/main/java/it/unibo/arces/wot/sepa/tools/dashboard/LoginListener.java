package it.unibo.arces.wot.sepa.tools.dashboard;

import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.security.ClientSecurityManager;

public interface LoginListener {
	void onLogin(String string,ClientSecurityManager sm);
	void onRegister();
	void onLoginError(ErrorResponse err);
	void onLoginClose();
}
