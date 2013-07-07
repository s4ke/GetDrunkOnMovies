package de.fsmpi.commonwords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSortedMap;

public class CommonWordsGenerator {

	// private static final String nl = "\\r?\\n";
	private static final String sp = "[ \\t]*";
	private static final Pattern WORD_PATTERN = Pattern.compile(sp
			+ "\\W*([\\w'^[0-9]]{1,})\\W*" + sp);

	private static final double THRESHOLD = .00002;

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		Map<String, Integer> words = new HashMap<>();
		int count = 0;
		for (String arg : args) {
			String input = readFileAsString(arg);
			count += count(input, words);
		}
		final Map<String, Integer> tmp = new HashMap<>();
		for (Entry<String, Integer> entry : words.entrySet()) {
			if (((double) entry.getValue() / (double) count) > THRESHOLD) {
				tmp.put(entry.getKey(), entry.getValue());
			}
		}
		words = ImmutableSortedMap.copyOf(tmp, new Comparator<String>() {

			@Override
			public int compare(String first, String second) {
				int ret = tmp.get(second) - tmp.get(first);
				if (ret == 0) {
					ret = first.compareTo(second);
				}
				return ret;
			}

		});
		System.out.println("Total words read: " + count);
		System.out.println("Common Words are: " + words);
		try (PrintWriter writer = new PrintWriter(new File("common_words.txt"))) {
			for (Entry<String, Integer> entry : words.entrySet()) {
				writer.println(entry.getKey());
			}
		}
	}

	private static int count(String input, Map<String, Integer> countMap) {
		int total = 0;
		Matcher matcher = WORD_PATTERN.matcher(input);
		while (matcher.find()) {
			String word = matcher.group(1);
			int count = 0;
			if (countMap.containsKey(word)) {
				count = countMap.get(word);
			}
			countMap.put(word, count + 1);
			++total;
		}
		return total;
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

}
