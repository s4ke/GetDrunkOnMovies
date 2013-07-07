package de.fsmpi.drunkserver.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import de.fsmpi.drunkserver.util.FileUtils;

import static de.fsmpi.drunkserver.util.Constants.*;

public class SRTConverter {
	
	private static final int OCCASION_COUNT = 20;

	private SRTConverter() {
		throw new AssertionError("can't touch this");
	}
	
	private static final String TIME_FORMAT = "(\\d{1,2}):(\\d\\d):(\\d\\d),(\\d\\d\\d)";
	private static final Pattern FILE_PATTERN = Pattern.compile("\\s*(\\d+)"
			+ sp + nl + TIME_FORMAT + sp + "-->" + sp + TIME_FORMAT + sp
			+ "(X1:\\d.*?)??" + nl + "(.*?)" + nl + nl, Pattern.DOTALL);
	private static final Pattern WORD_PATTERN = Pattern.compile(sp
			+ "\\W*([\\w']{4,})\\W*" + sp);
	private static final Pattern PATTERN = Pattern.compile("(.*).srt",
			Pattern.CASE_INSENSITIVE);

	public static void convertAllSRTs(String inputPath, String outputPath,
			ImmutableSet<String> commonWords) throws FileNotFoundException, IOException {
		File mainPath = new File(inputPath);
		File[] files = mainPath.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return PATTERN.matcher(name).matches();
			}

		});
		for (File file : files) {
			Matcher matcher = PATTERN.matcher(file.getAbsolutePath());
			if (!matcher.matches()) {
				throw new AssertionError("should match");
			}
			String movieFileAbsolutPath = matcher.group(1) + ".properties";
			convertSRT(file.getAbsolutePath(), movieFileAbsolutPath,
					commonWords);
		}
	}

	public static void convertSRT(String inputPath, String outputPath,
			ImmutableSet<String> commonWords) throws FileNotFoundException, IOException {

		final Map<String, Integer> countMap = new HashMap<>();
		Properties properties = new Properties();
		String completeFile = FileUtils.readFileAsString(inputPath);

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
					String word = subtitleTextMatcher.group(1).toLowerCase();
					if (!commonWords.contains(word.toLowerCase())) {
						int count;
						// the version (Uppercase or Lowercase) of the word that
						// was added gets used for the counting
						if (countMap.containsKey(word)) {
							count = countMap.get(word);
						} 
//						else if (countMap.containsKey(word.toLowerCase())) {
//							word = word.toLowerCase();
//							count = countMap.get(word);
//						} 
						else {
							count = 0;
						}
						countMap.put(word, count + 1);
					}
				}
			}
		}
		{
			PriorityQueue<String> occasionOrdering = new PriorityQueue<>(8, new Comparator<String>() {

				@Override
				public int compare(String first, String second) {
					int ret = countMap.get(second)
							- countMap.get(first);
					if (ret == 0) {
						ret = first.compareTo(second);
					}
					return ret;
				}
				
			});
			occasionOrdering.addAll(countMap.keySet());
			for(int i = 0; i < OCCASION_COUNT && occasionOrdering.size() > 0; ++i) {
				String key = occasionOrdering.poll();
				properties.put(key, countMap.get(key).toString());
			}
			try (FileOutputStream fos = new FileOutputStream(new File(
					outputPath))) {
				properties.store(fos, "");
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		System.out.println("converting...");
		long pre = System.currentTimeMillis();
		convertAllSRTs(".", ".", ImmutableSet.copyOf(FileUtils.readFileAsString("common_words.txt")
				.toLowerCase().split(nl)));
		long after = System.currentTimeMillis();
		System.out.println("done. took " + (after - pre) + "ms.");
	}

}
