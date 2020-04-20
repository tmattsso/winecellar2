package org.thomas.winecellar;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.thomas.winecellar.service.CurrentUserProvider;
import org.thomas.winecellar.service.UserService;

@Component
public class SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	@Autowired
	private CurrentUserProvider cup;

	@Autowired
	private UserService service;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {

		final DefaultOidcUser principal = (DefaultOidcUser) authentication.getPrincipal();

		// Make sure there is a user with this email. If there isn't, create with the
		// provided info.
		ensureUser(principal.getEmail(), principal.getFullName());

		// Set up app identity handling
		cup.loginByEmail(principal.getEmail());

		super.onAuthenticationSuccess(request, response, authentication);
	}

	private void ensureUser(String email, String name) {

		if (!service.userExists(email)) {
			service.addUser(name, email);
		}
	}
}
