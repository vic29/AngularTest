package com.google.vik;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

public class GoogleCredentialHandler {

	private static final String CLIENT_SECRET_JSON = "client_secrets.json";
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
	private static final String REDIRECT_URI = "http://localhost:8888";

	private GoogleAuthorizationCodeFlow flow;
	private String refreshToken;
	private Credential credential;

	public GoogleCredentialHandler(HttpServletRequest req,
			HttpServletResponse resp) {

		String authenticationCode = null;

		if (refreshToken != null)
			authenticationCode = refreshToken;
		else {
			authenticationCode = req.getParameter("code");
		}

		if (authenticationCode == null) {
			flow = getFlow();
			try {
				resp.getWriter().print(
						flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI)
								.build());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			credential = this.exchangeCode(authenticationCode);
			refreshToken = credential.getRefreshToken();
		}
	}

	private GoogleAuthorizationCodeFlow getFlow() {
		if (flow == null) {
			try {
				HttpTransport httpTransport = new NetHttpTransport();
				JacksonFactory jsonFactory = new JacksonFactory();

				GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
						jsonFactory,
						FileUtil.getResourceAsStream(CLIENT_SECRET_JSON));

				flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
						jsonFactory, clientSecrets, SCOPES)
						.setAccessType("offline").setApprovalPrompt("force")
						.build();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return flow;
	}

	private Credential exchangeCode(String authorizationCode) {
		try {
			GoogleAuthorizationCodeFlow flow = getFlow();
			GoogleTokenResponse response = flow
					.newTokenRequest(authorizationCode)
					.setRedirectUri(REDIRECT_URI).execute();
			return flow.createAndStoreCredential(response, null);
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
			return null;
		}
	}

	public Credential getCredential() {
		return credential;
	}
}