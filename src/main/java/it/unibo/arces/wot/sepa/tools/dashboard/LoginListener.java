package it.unibo.arces.wot.sepa.tools.dashboard;

import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;

public interface LoginListener {
	void onLogin(String string);
	void onRegister();
	void onLoginError(ErrorResponse err);
	void onLoginClose();
}
