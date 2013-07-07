package de.fsmpi.drunkserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
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
import com.google.common.collect.ImmutableSortedSet;

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
	private Map<String, List<Movie>> movieMap;
	private SortedSet<String> moviesAvailable;

	public DrunkServer(String path, ImmutableSet<String> commonWords)
			throws IOException {
		super();
		this.lock = new ReentrantLock();
		this.path = path;
		this.commonWords = commonWords;
		this.init();
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
				serializer.deepSerialize(this.moviesAvailable, writer);
			} else if (request.getParameter("movie") != null) {
				String movieName = request.getParameter("movie");
				LOGGER.log(Level.INFO, "movie \"" + movieName
						+ "\" requested from " + remoteAddr);
				if (!this.movieMap.containsKey(movieName)) {
					serializer.serialize(new Movie(), writer);
				} else {
					serializer.serialize(this.movieMap.get(movieName), writer);
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
			} else {
				writer.println("nothing to do");
			}
		}
	}

	private void init() throws IOException {
		this.lock.lock();
		try {
			LOGGER.log(Level.INFO, "(re)building movie database");
			Map<String, List<Movie>> map = PropertyFileLoader
					.loadPropertyFiles(this.path);
			this.moviesAvailable = ImmutableSortedSet.copyOf(map.keySet());
			this.movieMap = map;
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
		Server server = new Server(Integer.parseInt(args[0]));
		server.setHandler(new DrunkServer(args[1], ImmutableSet
				.copyOf(FileUtils.readFileAsString(args[2]).toLowerCase()
						.split(nl))));
		server.start();
		server.join();
	}
}
