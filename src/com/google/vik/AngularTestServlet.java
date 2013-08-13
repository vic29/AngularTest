package com.google.vik;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

@SuppressWarnings("serial")
public class AngularTestServlet extends HttpServlet {

	private static final String CLIENT_SECRET_JSON = "client_secrets.json";
	private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
	private static final String REDIRECT_URI = "http://localhost:8888";

	private GoogleAuthorizationCodeFlow flow;
	private Credential storedCredential;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String code = null;
		if (storedCredential != null)
			code = storedCredential.getRefreshToken();
		else
			code = req.getParameter("code");

		if (code == null) {
			flow = getFlow();

			String url = flow.newAuthorizationUrl()
					.setRedirectUri(REDIRECT_URI).build();
			System.out
					.println("Please open the following URL in your browser then type the authorization code:");
			System.out.println("  " + url);
			resp.getWriter().print(url);
		} else {
			HttpTransport httpTransport = new NetHttpTransport();
			JsonFactory jsonFactory = new JacksonFactory();
			// Create a new authorized API client
			Drive service = new Drive.Builder(httpTransport, jsonFactory,
					getCredential(code)).build();

			File file = service.files()
					.get("1uuNXbO2s-YCGUtiIpeIkdENOMFKMfi8knBaHkVF1ypM")
					.execute();

			// // Insert a file
			// File body = new File();
			// body.setTitle("My document");
			// body.setDescription("A test document");
			// body.setMimeType("text/plain");
			//
			// java.io.File fileContent = new java.io.File("document.txt");
			// FileContent mediaContent = new FileContent("text/plain",
			// fileContent);
			//
			// File file = service.files().insert(body, mediaContent).execute();
			//

			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();

			String line;
			try {

				br = new BufferedReader(new InputStreamReader(
						this.downloadFile(service, file)));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			resp.getWriter().print(sb.toString());

		}
	}

	/**
	 * Print a file's metadata.
	 * 
	 * @param service
	 *            Drive API service instance.
	 * @param fileId
	 *            ID of the file to print metadata for.
	 */
	private static void printFile(Drive service, String fileId) {

		try {
			File file = service.files().get(fileId).execute();

			System.out.println("Title: " + file.getTitle());
			System.out.println("Description: " + file.getDescription());
			System.out.println("MIME type: " + file.getMimeType());
		} catch (IOException e) {
			System.out.println("An error occured: " + e);
		}
	}

	/**
	 * Download a file's content.
	 * 
	 * @param service
	 *            Drive API service instance.
	 * @param file
	 *            Drive File instance.
	 * @return InputStream containing the file's content if successful,
	 *         {@code null} otherwise.
	 */
	private static InputStream downloadFile(Drive service, File file) {

		try {
			HttpResponse resp = service
					.getRequestFactory()
					.buildGetRequest(
							new GenericUrl(
									"https://docs.google.com/feeds/download/documents/export/Export?id="
											+ file.getId()
											+ "&exportFormat=html")).execute();
			return resp.getContent();
		} catch (IOException e) {
			// An error occurred.
			e.printStackTrace();
			return null;
		}

	}

	private GoogleAuthorizationCodeFlow getFlow() throws IOException {
		if (flow == null) {
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();

			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
					jsonFactory, getResourceAsStream(CLIENT_SECRET_JSON));
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
					jsonFactory, clientSecrets, SCOPES)
					.setAccessType("offline").setApprovalPrompt("force")
					.build();
		}
		return flow;
	}

	private Reader getResourceAsStream(String name)
			throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(name);
		return new InputStreamReader(inputStream);
	}

	/**
	 * Exchange an authorization code for OAuth 2.0 credentials.
	 * 
	 * @param authorizationCode
	 *            Authorization code to exchange for OAuth 2.0 credentials.
	 * @return OAuth 2.0 credentials.
	 * @throws CodeExchangeException
	 *             An error occurred.
	 */
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

	private Credential getCredential(String authorizationCode) {
		if (storedCredential == null)
			storedCredential = this.exchangeCode(authorizationCode);
		return storedCredential;
	}
}
