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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.github.hotware.hsearch.annotations.InIndex;

/**
 * @author Martin Braun
 */
@Entity
@Indexed
@InIndex
public class DBMovie {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@DocumentId
	private Integer id;

	@Column
	@Field(store = Store.YES, index = Index.YES, analyzer = @Analyzer(impl = CompleteWordAnalyzer.class))
	private String name;

	@OrderBy("count")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "dbMovie", targetEntity = DrinkOccasion.class, orphanRemoval = true)
	private List<DrinkOccasion> occasions;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the occasions
	 */
	public List<DrinkOccasion> getOccasions() {
		return occasions;
	}

	/**
	 * @param occasions
	 *            the occasions to set
	 */
	public void setOccasions(List<DrinkOccasion> occasions) {
		this.occasions = occasions;
	}

}
