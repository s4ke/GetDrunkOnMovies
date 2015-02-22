/*
 * Copyright 2015 Martin Braun
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fsmpi.drunkserver.db;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.internal.jpa.EntityManagerImpl;

import com.github.hotware.hsearch.entity.jpa.EntityManagerEntityProvider;
import com.github.hotware.hsearch.event.EventConsumer;
import com.github.hotware.hsearch.event.EventSource;
import com.github.hotware.hsearch.factory.SearchConfigurationImpl;
import com.github.hotware.hsearch.factory.SearchFactory;
import com.github.hotware.hsearch.factory.SearchFactoryFactory;
import com.github.hotware.hsearch.jpa.events.MetaModelParser;

/**
 * @author Martin Braun
 */
public class Database {

	private static final EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("EclipseLink");
	private static final SearchFactory searchFactory;
	static {
		searchFactory = SearchFactoryFactory.createSearchFactory(
				new EventSource() {

					@Override
					public void disable(boolean arg0) {

					}

					@Override
					public void setEventConsumer(EventConsumer arg0) {

					}

				},
				new SearchConfigurationImpl().addProperty(
						"hibernate.search.default.indexBase", "indexes")
						.addProperty(
								"hibernate.search.default.directory_provider",
								"filesystem"), Arrays.asList(DBMovie.class,
						DrinkOccasion.class));
	}
	private static final MetaModelParser metaModelParser;
	static {
		metaModelParser = new MetaModelParser();
		metaModelParser.parse(emf.getMetamodel());
	}

	public static void store(DBMovie dbMovie) {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(dbMovie);
			searchFactory.index(dbMovie);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public static void clear() {
		EntityManager em = emf.createEntityManager();
		try {
			em.getTransaction().begin();
			for (DBMovie dbMovie : (List<DBMovie>) em.createQuery(
					"SELECT a FROM DBMovie a").getResultList()) {
				em.remove(dbMovie);
			}
			searchFactory.purgeAll(DBMovie.class);
			em.getTransaction().commit();
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public static List<DBMovie> getAll() {
		EntityManager em = emf.createEntityManager();
		try {
			List<DBMovie> movies = em.createQuery(
					"SELECT a FROM DBMovie a ORDER BY a.name ASC")
					.getResultList();
			return movies;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}

	public static List<DBMovie> findAll(String name) {
		EntityManager em = emf.createEntityManager();
		try {
			EntityManagerEntityProvider entityProvider = new EntityManagerEntityProvider(
					em, metaModelParser.getIdProperties());
			List<DBMovie> movies = searchFactory.createQuery(
					searchFactory.buildQueryBuilder().forEntity(DBMovie.class)
							.get().keyword().onField("name").matching(name)
							.createQuery()).query(entityProvider);
			return movies;
		} finally {
			if (em != null) {
				em.close();
			}
		}
	}
}
