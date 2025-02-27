package com.vaimee.sepa.tools.dashboard.utils;

import com.vaimee.sepa.commons.response.ErrorResponse;

public interface LoginListener {
	void onLogin(String id);//, String secret,boolean remember);
	void onLoginError(ErrorResponse err);
	void onLoginClose();
}
