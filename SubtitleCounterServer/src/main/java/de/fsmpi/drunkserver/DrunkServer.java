package de.fsmpi.drunkserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

import de.fsmpi.drunkserver.db.DBMovie;
import de.fsmpi.drunkserver.db.Database;
import de.fsmpi.drunkserver.db.DrinkOccasion;
import de.fsmpi.drunkserver.loader.PropertyFileLoader;
import de.fsmpi.drunkserver.model.Movie;
import de.fsmpi.drunkserver.util.FileUtils;
import static de.fsmpi.drunkserver.util.Constants.*;
import flexjson.JSONSerializer;

public class DrunkServer extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(DrunkServer.class
			.getName());

	private final Lock lock;
	private String path;
	// FIXME: converting has to be starteable from here
	@SuppressWarnings("unused")
	private ImmutableSet<String> commonWords;

	public DrunkServer(String path, ImmutableSet<String> commonWords)
			throws IOException {
		super();
		this.lock = new ReentrantLock();
		this.path = path;
		this.commonWords = commonWords;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		{
			// allow complete access from everywhere
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
		}
		{
			JSONSerializer serializer = new JSONSerializer().exclude("*.class");
			PrintWriter writer = response.getWriter();
			String remoteAddr = request.getRemoteAddr();
			if (request.getParameter("allMovies") != null) {
				LOGGER.log(Level.INFO, "all movies requested from "
						+ remoteAddr);
				serializer.deepSerialize(putIntoBatches(Database.getAll()), writer);
			} else if (request.getParameter("movie") != null) {
				Integer id= Integer.parseInt(request.getParameter("movie"));
				LOGGER.log(Level.INFO, "movie \"" + id
						+ "\" requested from " + remoteAddr);
				DBMovie dbMovie = Database.findOne(id);
				if (dbMovie == null) {
					serializer.serialize(new DBMovie(), writer);
				} else {
					serializer.deepSerialize(dbMovie, writer);
				}
			} else if (request.getParameter("rebuild") != null
					&& (remoteAddr.equals("0:0:0:0:0:0:0:1") || remoteAddr
							.equals("127.0.0.1"))) {
				try {
					this.init();
					writer.println("rebuilt movie database");
				} catch (IOException e) {
					LOGGER.log(Level.INFO, "IOException in init", e);
				}
			} else if (request.getParameter("search") != null) {
				// TODO: LUCENIZE this
				String searchString = request.getParameter("search");
				serializer.deepSerialize(putIntoBatches(Database.findAll(searchString)), writer);
			} else {
				writer.println("nothing to do");
			}
		}
	}
	
	private static ImmutableSortedMap<String, List<DBMovie>> putIntoBatches(List<DBMovie> dbMovies) {
		//TODO: do this with faceting!
		Map<String, List<DBMovie>> dbMoviesPerStartingLetter = new HashMap<>();
		for(DBMovie dbMovie : dbMovies) {
			dbMoviesPerStartingLetter.computeIfAbsent(dbMovie.getName().substring(0, 1).toUpperCase(), (key) -> {
				return new ArrayList<>();
			}).add(dbMovie);
		}
		for(Map.Entry<String, List<DBMovie>> entry : dbMoviesPerStartingLetter.entrySet()) {
			entry.getValue().sort(new Comparator<DBMovie>() {

				@Override
				public int compare(DBMovie first, DBMovie second) {
					return first.getName().compareTo(second.getName());
				}
				
			});
		}
		return ImmutableSortedMap.copyOf(dbMoviesPerStartingLetter);
	}
	
//	private static Movie toMovie(DBMovie dbMovie) {
//		Movie movie = new Movie();
//		movie.setName(dbMovie.getName());
//		final Map<String, Integer> map = new HashMap<>();
//		for (DrinkOccasion dbOccasion : dbMovie.getOccasions()) {
//			map.put(dbOccasion.getText(), dbOccasion.getCount());
//		}
//		movie.setDrink(ImmutableSortedMap.copyOf(map,
//				new Comparator<String>() {
//
//					@Override
//					public int compare(String first,
//							String second) {
//						int ret = map.get(second)
//								- map.get(first);
//						if (ret == 0) {
//							ret = first.compareTo(second);
//						}
//						return ret;
//					}
//
//				}));
//		return movie;
//	}

	private void init() throws IOException {
		this.lock.lock();
		try {
			LOGGER.log(Level.INFO, "(re)building movie database");
			Database.clear();
			Map<String, List<Movie>> map = PropertyFileLoader
					.loadPropertyFiles(this.path);
			for (Map.Entry<String, List<Movie>> entry : map.entrySet()) {
				for (Movie movie : entry.getValue()) {
					DBMovie dbMovie = new DBMovie();
					dbMovie.setName(movie.getName());
					List<DrinkOccasion> drinkOccasions = new ArrayList<>();
					for (Map.Entry<String, Integer> occasion : movie.getDrink()
							.entrySet()) {
						DrinkOccasion dbOccasion = new DrinkOccasion();
						dbOccasion.setDbMovie(dbMovie);
						dbOccasion.setText(occasion.getKey());
						dbOccasion.setCount(occasion.getValue());
						drinkOccasions.add(dbOccasion);
					}
					dbMovie.setOccasions(drinkOccasions);
					Database.store(dbMovie);
				}
			}
			LOGGER.log(Level.INFO, "(re)built movie database");
		} finally {
			this.lock.unlock();
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("parameters: <port> <path_to_movie_properties>"
					+ " <path_to_common_words_file>");
			return;
		}
		System.out.println(Database.findAll("a"));
		Server server = new Server(Integer.parseInt(args[0]));
		server.setHandler(new DrunkServer(args[1], ImmutableSet
				.copyOf(FileUtils.readFileAsString(args[2]).toLowerCase()
						.split(nl))));
		server.start();
		server.join();
	}
}
