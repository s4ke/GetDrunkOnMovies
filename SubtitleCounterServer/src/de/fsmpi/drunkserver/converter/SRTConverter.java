package de.fsmpi.drunkserver.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SRTConverter {

	private SRTConverter() {
		throw new AssertionError("can't touch this");
	}

	private static final String nl = "\\r?\\n";
	private static final String sp = "[ \\t]*";
	private static final String TIME_FORMAT = "(\\d{1,2}):(\\d\\d):(\\d\\d),(\\d\\d\\d)";
	private static final Pattern FILE_PATTERN = Pattern.compile("\\s*(\\d+)"
			+ sp + nl + TIME_FORMAT + sp + "-->" + sp + TIME_FORMAT + sp
			+ "(X1:\\d.*?)??" + nl + "(.*?)" + nl + nl, Pattern.DOTALL);
	private static final Pattern WORD_PATTERN = Pattern.compile(sp
			+ "\\W*([\\w']{4,})\\W*" + sp);
	private static final Pattern PATTERN = Pattern.compile("(.*).srt",
			Pattern.CASE_INSENSITIVE);

	public static void convertAllSRTs(String inputPath, String outputPath,
			String commonWords) throws FileNotFoundException, IOException {
		File mainPath = new File(inputPath);
		File[] files = mainPath.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return PATTERN.matcher(name).matches();
			}

		});
		for(File file : files) {
			Matcher matcher = PATTERN.matcher(file.getAbsolutePath());
			if (!matcher.matches()) {
				throw new AssertionError("should match");
			}
			String movieFileAbsolutPath = matcher.group(1) + ".properties";
			convertSRT(file.getAbsolutePath(), movieFileAbsolutPath , commonWords);
		}
	}

	public static void convertSRT(String inputPath, String outputPath,
			String commonWords) throws FileNotFoundException, IOException {

		Map<String, Integer> countMap = new HashMap<>();
		Properties properties = new Properties();
		String completeFile = readFileAsString(inputPath);

		{
			Matcher completeFileMatcher = FILE_PATTERN.matcher(completeFile);
			while (completeFileMatcher.find()) {
				// String hoursStart = completeFileMatcher.group(2);
				// String minsStart = completeFileMatcher.group(3);
				// String secsStart = completeFileMatcher.group(4);
				// String milliStart = completeFileMatcher.group(5);
				// String hoursEnd = completeFileMatcher.group(6);
				// String minsEnd = completeFileMatcher.group(7);
				// String secsEnd = completeFileMatcher.group(8);
				// String milliEnd = completeFileMatcher.group(9);
				String subtitleText = completeFileMatcher.group(11);
				Matcher subtitleTextMatcher = WORD_PATTERN
						.matcher(subtitleText);
				while (subtitleTextMatcher.find()) {
					String word = subtitleTextMatcher.group(1);
					if(!commonWords.contains(word.toLowerCase())) {
						int count = 0;
						if (countMap.containsKey(word)) {
							count = countMap.get(word);
						}
						countMap.put(word, count + 1);
					}
				}
			}
		}
		{
			for (Entry<String, Integer> entry : countMap.entrySet()) {
				properties.put(entry.getKey(), entry.getValue().toString());
			}
			try (FileOutputStream fos = new FileOutputStream(new File(
					outputPath))) {
				properties.store(fos, "");
			}
		}
	}

	private static String readFileAsString(String inputPath)
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

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		convertAllSRTs(".", ".", readFileAsString("common_words.txt").toLowerCase());
	}

}
