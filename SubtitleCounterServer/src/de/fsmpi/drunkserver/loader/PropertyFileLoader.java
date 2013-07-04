package de.fsmpi.drunkserver.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Ordering;

import de.fsmpi.drunkserver.model.Movie;
import de.fsmpi.drunkserver.util.ValueComparableMap;

public final class PropertyFileLoader {
	
	public static final String MOVIE_NAME_KEY = "MOVIE_NAME_TO_DISPLAY_IN_DRUNK_SERVER";

	private static final Logger LOGGER = Logger
			.getLogger(PropertyFileLoader.class.getName());
	private static final Pattern PATTERN = Pattern.compile("(.*).properties",
			Pattern.CASE_INSENSITIVE);

	private PropertyFileLoader() {
		throw new AssertionError("can't touch this.");
	}

	public static Map<String, List<Movie>> loadPropertyFiles(String path)
			throws IOException {
		Map<String, List<Movie>> ret = new HashMap<>();
		File mainPath = new File(path);
		File[] files = mainPath.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return PATTERN.matcher(name).matches();
			}

		});
		for (File file : files) {
			try (FileInputStream fs = new FileInputStream(file)) {
				Properties props = new Properties();
				props.load(new FileInputStream(file));
				Matcher matcher = PATTERN.matcher(file.getName());
				if (!matcher.matches()) {
					throw new AssertionError("should match");
				}
				String movieFileName = matcher.group(1);
				Map<String, Integer> drinkAction = new ValueComparableMap<>(
						Ordering.natural().reverse());
				for (Entry<Object, Object> entry : props.entrySet()) {
					if (!entry.getKey().equals(MOVIE_NAME_KEY)) {
						try {
							drinkAction
									.put((String) entry.getKey(),
											Integer.parseInt((String) entry
													.getValue()));
						} catch (NumberFormatException e) {
							LOGGER.log(Level.WARNING,
									"malformed properties file", e);
						}
					}
				}
				Movie mov = new Movie();
				String movieName = props.getProperty(MOVIE_NAME_KEY,
						movieFileName);
				mov.setName(movieName);
				mov.setDrink(drinkAction);
				List<Movie> list = ret.get(movieName);
				if (list == null) {
					list = new ArrayList<>();
					ret.put(movieName, list);
				}
				list.add(mov);
			}
		}
		return ret;
	}

}
