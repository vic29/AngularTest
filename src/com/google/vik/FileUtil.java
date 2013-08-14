package com.google.vik;

public class FileUtil {

	public static Reader getResourceAsStream(String name)
			throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(name);
		return new InputStreamReader(inputStream);
	}
}