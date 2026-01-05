/*
 * Copyright 2011-2026 the original author or authors.
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

import java.util.Optional;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author Michael J. Simons
 */
interface PeopleRepository extends Neo4jRepository<Person, Long> {

	@Query("""
		MATCH (person:Person {name: $name})
		OPTIONAL MATCH (person)-[:DIRECTED]->(d:Movie)
		OPTIONAL MATCH (person)<-[r:ACTED_IN]->(a:Movie)
		OPTIONAL MATCH (person)-->(movies)<-[relatedRole:ACTED_IN]-(relatedPerson)		
		RETURN DISTINCT person.name AS name, person.born AS born,
		collect(DISTINCT d) AS directed,
		collect(DISTINCT a) AS actedIn,
		collect(DISTINCT relatedPerson) AS related
		""")
	Optional<PersonDetails> getDetailsByName(String name);
}
