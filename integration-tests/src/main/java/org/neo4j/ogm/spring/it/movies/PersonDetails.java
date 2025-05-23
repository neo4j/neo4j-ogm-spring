/*
 * Copyright 2011-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.spring.it.movies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.neo4j.annotation.QueryResult;

/**
 * This is a DTO based projection, containing a couple of additional details,
 * like the list of movies a person acted in, the movies they direct and which other
 * people they acted with
 *
 * @author Michael J. Simons
 */
@QueryResult
public final class PersonDetails {

	private final String name;

	private final Integer born;

	private final List<Movie> actedIn;

	private final List<Movie> directed;

	private final List<Person> related;

	public PersonDetails(String name, Long born, List<Movie> actedIn,
		List<Movie> directed, List<Person> related) {
		this.name = name;
		this.born = Math.toIntExact(born);
		this.actedIn = new ArrayList<>(actedIn);
		this.directed = new ArrayList<>(directed);
		this.related = new ArrayList<>(related);
	}

	public String getName() {
		return name;
	}

	public Integer getBorn() {
		return born;
	}

	public List<Movie> getActedIn() {
		return Collections.unmodifiableList(actedIn);
	}

	public List<Movie> getDirected() {
		return Collections.unmodifiableList(directed);
	}

	public List<Person> getRelated() {
		return Collections.unmodifiableList(related);
	}
}
