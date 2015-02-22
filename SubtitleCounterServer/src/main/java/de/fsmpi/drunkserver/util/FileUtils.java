package de.fsmpi.drunkserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public final class FileUtils {

	private FileUtils() {
		throw new AssertionError("can't touch this!");
	}
	
	public static String readFileAsString(String inputPath)
			throws FileNotFoundException, IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(inputPath))))) {
			StringBuffer buffer = new StringBuffer();
			{
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line).append("\n");
				}
			}
			return buffer.toString();
		}
	}

}
