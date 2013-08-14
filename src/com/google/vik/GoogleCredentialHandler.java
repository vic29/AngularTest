package com.google.vik;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;

public class GoogleCredentialHandler {

	private static final String CLIENT_SECRET_JSON = "client_secrets.json";
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
	private static final String REDIRECT_URI = "http://localhost:8888";

	private GoogleAuthorizationCodeFlow flow;
	private String refreshToken;
	private Credential credential;

	public GoogleCredentialHandler(HttpServletRequest req, HttpServletResponse resp){
		String authenticationCode = null;
		if (refreshToken != null)
			authenticationCode = refreshToken;
		else {
			authenticationCode = req.getParameter("code");
		}
		
		if (authenticationCode == null) {
			flow = getFlow();
			resp.getWriter().print(flow.newAuthorizationUrl()
					.setRedirectUri(REDIRECT_URI).build(););
		} 
		else {
			credential = this.exchangeCode(authorizationCode);
			refreshToken = credendial.getRefreshToken();
		}
	}

	private GoogleAuthorizationCodeFlow getFlow() throws IOException {
		if (flow == null) {
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();

			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
					jsonFactory, FileUtil.getResourceAsStream(CLIENT_SECRET_JSON));
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
					jsonFactory, clientSecrets, SCOPES)
					.setAccessType("offline").setApprovalPrompt("force")
					.build();
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

	private Credential getCredential() {
		return credential;
	}
}