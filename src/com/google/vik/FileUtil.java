package com.google.vik;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.gson.Gson;

public class FileUtil {

	private static final Gson GSON = new Gson();

	public static Reader getResourceAsStream(String name)
			throws FileNotFoundException {

		InputStream inputStream = new FileInputStream(name);
		return new InputStreamReader(inputStream);
	}

	public static Credential getCredentialFromJsonFile(String file) {
		try {
			Reader reader = getResourceAsStream(file);
			return GSON.fromJson(reader, Credential.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}