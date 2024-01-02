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
package org.springframework.data.neo4j.transaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.examples.movies.domain.User;
import org.springframework.data.neo4j.examples.movies.repo.UserRepository;
import org.springframework.data.neo4j.examples.movies.service.UserService;
import org.springframework.data.neo4j.queries.MoviesContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Michal Bachman
 * @author Michael J. Simons
 */
@ContextConfiguration(classes = MoviesContextConfiguration.class)
@RunWith(SpringRunner.class)
public class TransactionIntegrationTests {

	@Autowired private DatabaseManagementService databaseManagementService;

	@Autowired private UserRepository userRepository;

	@Autowired private UserService userService;

	private TransactionEventListenerAdapter<Object> handler;

	@Before
	public void populateDatabase() {
		handler = new TransactionEventListenerAdapter<Object>() {
			@Override
			public Object beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) throws Exception {
				throw new TransactionInterceptException("Deliberate testing exception");
			}
		};
		databaseManagementService.registerTransactionEventListener("neo4j", handler);
	}

	@After
	public void cleanupHandler() {
		databaseManagementService.unregisterTransactionEventListener("neo4j", handler);
	}

	@Test(expected = Exception.class)
	public void whenImplicitTransactionFailsNothingShouldBeCreated() {
		userRepository.save(new User("Michal"));
	}

	@Test(expected = Exception.class)
	public void whenExplicitTransactionFailsNothingShouldBeCreated() {
		userService.saveWithTxAnnotationOnInterface(new User("Michal"));
	}

	@Test(expected = Exception.class)
	public void whenExplicitTransactionFailsNothingShouldBeCreated2() {
		userService.saveWithTxAnnotationOnImpl(new User("Michal"));
	}

	static class TransactionInterceptException extends Exception {

		public TransactionInterceptException(String msg) {
			super(msg);
		}
	}
}
