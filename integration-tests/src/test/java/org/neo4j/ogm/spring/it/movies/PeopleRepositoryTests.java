/*
 * Copyright 2011-2024 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.springframework.boot.autoconfigure.Neo4jOGMAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Michael J. Simons
 */
@Testcontainers
@DataNeo4jTest
@ImportAutoConfiguration(Neo4jOGMAutoConfiguration.class)
public class PeopleRepositoryTests {

	@Container
	@ServiceConnection
	static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

	@BeforeAll
	static void prepareDataSet(@Autowired Driver driver) {

		driver.executableQuery("""
						CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})
						CREATE (Keanu:Person {name:'Keanu Reeves', born:1964})
						CREATE (Carrie:Person {name:'Carrie-Anne Moss', born:1967})
						CREATE (Laurence:Person {name:'Laurence Fishburne', born:1961})
						CREATE (Hugo:Person {name:'Hugo Weaving', born:1960})
						CREATE (LillyW:Person {name:'Lilly Wachowski', born:1967})
						CREATE (LanaW:Person {name:'Lana Wachowski', born:1965})
						CREATE (JoelS:Person {name:'Joel Silver', born:1952})
						CREATE (KevinB:Person {name:'Kevin Bacon', born:1958})
						CREATE
						(Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrix),
						(Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrix),
						(Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrix),
						(Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrix),
						(LillyW)-[:DIRECTED]->(TheMatrix),
						(LanaW)-[:DIRECTED]->(TheMatrix),
						(JoelS)-[:PRODUCED]->(TheMatrix)
							
						CREATE (Emil:Person {name:"Emil Eifrem", born:1978})
						CREATE (Emil)-[:ACTED_IN {roles:["Emil"]}]->(TheMatrix)
							
						CREATE (TheMatrixReloaded:Movie {title:'The Matrix Reloaded', released:2003, tagline:'Free your mind'})
						CREATE
						(Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixReloaded),
						(Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixReloaded),
						(Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixReloaded),
						(Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixReloaded),
						(LillyW)-[:DIRECTED]->(TheMatrixReloaded),
						(LanaW)-[:DIRECTED]->(TheMatrixReloaded),
						(JoelS)-[:PRODUCED]->(TheMatrixReloaded)
							
						CREATE (TheMatrixRevolutions:Movie {title:'The Matrix Revolutions', released:2003, tagline:'Everything that has a beginning has an end'})
						CREATE
						(Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrixRevolutions),
						(Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrixRevolutions),
						(Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrixRevolutions),
						(KevinB)-[:ACTED_IN {roles:['Unknown']}]->(TheMatrixRevolutions),
						(Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrixRevolutions),
						(LillyW)-[:DIRECTED]->(TheMatrixRevolutions),
						(LanaW)-[:DIRECTED]->(TheMatrixRevolutions),
						(JoelS)-[:PRODUCED]->(TheMatrixRevolutions)
						"""
				)
				.execute();
	}

	@Autowired
	PeopleRepository peopleRepository;

	@Test
	public void getDetailsByNameShouldWork() {

		var optionalDetails = peopleRepository.getDetailsByName("Keanu Reeves");

		assertThat(optionalDetails).hasValueSatisfying(personDetails -> {
			assertThat(personDetails.getName()).isEqualTo("Keanu Reeves");
			assertThat(personDetails.getBorn()).isEqualTo(1964);
			assertThat(personDetails.getActedIn())
					.hasSize(3)
					.extracting(Movie::getTitle).contains("The Matrix Reloaded");
			assertThat(personDetails.getRelated()).hasSize(5);
		});

		assertThat(peopleRepository.getDetailsByName("foobar")).isEmpty();
	}
}
