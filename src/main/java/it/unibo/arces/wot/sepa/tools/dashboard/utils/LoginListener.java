package it.unibo.arces.wot.sepa.tools.dashboard.utils;

import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;

public interface LoginListener {
	void onLogin(String id);//, String secret,boolean remember);
	void onLoginError(ErrorResponse err);
	void onLoginClose();
}
