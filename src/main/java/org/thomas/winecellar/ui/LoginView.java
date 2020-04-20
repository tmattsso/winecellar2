package org.thomas.winecellar.ui;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.WildcardParameter;

@Route("login")
@RouteAlias("loginSuccess")
public class LoginView extends VerticalLayout implements HasUrlParameter<String> {

	private static final long serialVersionUID = -1724824498184786980L;

	private final Logger LOG = LoggerFactory.getLogger(LoginView.class);

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@Override
	public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {

		removeAll();

		add(new H2("Welcome to Winecellar!"));
		add(new Text("We don't seem to be able to identify you; please login."));
		add(new Anchor(getLoginLink(), "Login with Google"));

		if (parameter != null && parameter.contains("error")) {
			add(new Text("There was an error with our login, please try again"));
		}

	}

	@SuppressWarnings("unchecked")
	public String getLoginLink() {
		Iterable<ClientRegistration> clientRegistrations = null;
		final ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
		if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
			clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
		}

		final Map<String, String> oauth2AuthenticationUrls = new HashMap<>();
		final String authorizationRequestBaseUri = "oauth2/authorization";

		clientRegistrations.forEach(registration -> oauth2AuthenticationUrls.put(registration.getClientName(),
				authorizationRequestBaseUri + "/" + registration.getRegistrationId()));

		return oauth2AuthenticationUrls.entrySet().iterator().next().getValue();
	}

}
