package it.unibo.arces.wot.sepa.tools;

import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;

public interface LoginListener {
	void onLogin();
	void onRegister();
	void onError(ErrorResponse err);
	void onLoginClose();
}
