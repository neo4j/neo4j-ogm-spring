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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.TransactionManager;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApplicationTests {

	@Container
	@ServiceConnection
	static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5");

	private final ObjectMapper objectMapper = new ObjectMapper();

	@LocalServerPort
	int port;

	@Test
	void contextLoadsAndRepositoriesAreFunctional() throws IOException, InterruptedException {

		var httpClient = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString("""
						{
						   "name":"Michael Simons",
						   "born":1979
						}
						"""))
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.uri(URI.create("http://localhost:%d/api/people".formatted(port)))
				.build();

		var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		var newPerson = objectMapper.readValue(response.body(), Person.class);

		assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(newPerson.getId()).isNotNegative();
		assertThat(newPerson.getName()).isEqualTo("Michael Simons");
	}

	@Test
	void transactionManagerAvailable(@Autowired ApplicationContext ctx) {

		assertThat(ctx.getBean(TransactionManager.class))
				.isNotNull().isInstanceOf(Neo4jTransactionManager.class);
	}
}
