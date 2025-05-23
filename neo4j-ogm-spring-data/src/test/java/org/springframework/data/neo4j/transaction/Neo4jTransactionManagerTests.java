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
package org.springframework.data.neo4j.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Mark Angrish
 */
public class Neo4jTransactionManagerTests {

	private SessionFactory sf;

	private Session session;

	private Transaction tx;

	private Neo4jTransactionManager tm;

	private TransactionTemplate tt;

	@Before
	public void setUp() throws Exception {
		sf = mock(SessionFactory.class);
		session = mock(Session.class);
		tx = mock(Transaction.class);

		tm = new Neo4jTransactionManager(sf);
		tt = new TransactionTemplate(tm);

		given(session.getTransaction()).willReturn(tx);
		given(sf.openSession()).willReturn(session);

		tm.setSessionFactory(sf);
		tt.setTransactionManager(tm);
	}

	@After
	public void tearDown() throws Exception {
		assertThat(TransactionSynchronizationManager.getResourceMap().isEmpty()).isTrue();
		assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();
		assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly())
				.isFalse();
		assertThat(TransactionSynchronizationManager.isActualTransactionActive())
				.isFalse();
	}

	@Test
	public void testFailBegin() throws Exception {

		given(session.beginTransaction(any(Transaction.Type.class), anyCollection())).willThrow(RuntimeException.class);
		given(session.getTransaction()).willReturn(null);

		try {
			tt.execute(status -> {
				return null;
			});
			fail("Should not have executed");
		} catch (CannotCreateTransactionException ex) {
			// expected
		}
	}

	@Test
	public void testTransactionCommit() throws Exception {

		final List list = new ArrayList();
		HashMap<String, Object> entry = new HashMap<>();
		entry.put("test", "test");
		list.add(entry);

		Result res = mock(Result.class);

		given(session.query("some query string", Collections.<String, Object> emptyMap())).willReturn(res);
		given(res.queryResults()).willReturn(list);

		assertThat(!TransactionSynchronizationManager.hasResource(sf))
				.as("Transaction Synchronization already has a thread bound session")
				.isTrue();
		assertThat(!TransactionSynchronizationManager.isSynchronizationActive())
				.as("Synchronizations not active").isTrue();

		Object result = tt.execute((TransactionCallback) status -> {
			assertThat(TransactionSynchronizationManager.hasResource(sf))
					.as("Transaction Synchronization doesn't have a thread bound session")
					.isTrue();
			assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly())
					.isFalse();
			assertThat(TransactionSynchronizationManager.isActualTransactionActive())
					.isTrue();
			Session session = ((SessionHolder) TransactionSynchronizationManager.getResource(sf)).getSession();
			return session.query("some query string", Collections.<String, Object> emptyMap()).queryResults();
		});

		assertThat(result == list).as("Incorrect result list").isTrue();
		assertThat(!TransactionSynchronizationManager.hasResource(sf))
				.as("Transaction Synchronization still has a thread bound session")
				.isTrue();
		assertThat(!TransactionSynchronizationManager.isSynchronizationActive())
				.as("Synchronizations not active").isTrue();

		verify(session).beginTransaction(any(Transaction.Type.class), anyCollection());
		verify(tx).commit();
		verify(tx).close();
	}

	@Test
	public void testTransactionRollback() throws Exception {

		assertThat(!TransactionSynchronizationManager.hasResource(sf))
				.as("Transaction Synchronization already has a thread bound session")
				.isTrue();
		assertThat(!TransactionSynchronizationManager.isSynchronizationActive())
				.as("Synchronizations not active").isTrue();

		try {
			tt.execute(status -> {
				assertThat(TransactionSynchronizationManager.hasResource(sf))
						.as("Transaction Synchronization doesn't have a thread bound session")
						.isTrue();
				throw new RuntimeException("application exception");
			});
			fail("Should have thrown RuntimeException");
		} catch (RuntimeException ex) {
			// expected
		}

		assertThat(!TransactionSynchronizationManager.hasResource(sf))
				.as("Transaction Synchronization still has a thread bound session")
				.isTrue();
		assertThat(!TransactionSynchronizationManager.isSynchronizationActive())
				.as("Synchronizations not active").isTrue();

		verify(session).beginTransaction(any(Transaction.Type.class), anyCollection());
		verify(tx).rollback();
		verify(tx).close();
	}

	@Test
	public void testTransactionRollbackOnly() throws Exception {

		assertThat(!TransactionSynchronizationManager.hasResource(sf))
				.as("Transaction Synchronization already has a thread bound session")
				.isTrue();

		tt.execute(status -> {
			assertThat(TransactionSynchronizationManager.hasResource(sf))
					.as("Transaction Synchronization doesn't have a thread bound session")
					.isTrue();
			status.setRollbackOnly();
			return null;
		});

		assertThat(!TransactionSynchronizationManager.hasResource(sf))
				.as("Transaction Synchronization still has a thread bound session")
				.isTrue();

		verify(session).beginTransaction(any(Transaction.Type.class), anyCollection());
		verify(tx).rollback();
		verify(tx).close();
	}

	@Test
	public void testParticipatingTransactionWithCommit() throws Exception {
		final List l = new ArrayList();
		l.add("test");

		Object result = tt.execute(status -> tt.execute((TransactionCallback) status1 -> l));
		assertThat(result == l).as("Correct result list").isTrue();

		verify(session).beginTransaction(any(Transaction.Type.class), anyCollection());
		verify(tx).commit();
		verify(tx).close();
	}

	@Test
	public void testParticipatingTransactionWithRollback() throws Exception {

		try {
			tt.execute(status -> tt.execute(status1 -> {
				throw new RuntimeException("application exception");
			}));
			fail("Should have thrown RuntimeException");
		} catch (RuntimeException ex) {
			// expected
		}

		verify(session).beginTransaction(any(Transaction.Type.class), anyCollection());
		verify(tx).rollback();
		verify(tx).close();
	}

	@Test
	@Ignore
	public void testParticipatingTransactionWithRollbackOnly() throws Exception {

		try {
			tt.execute(status -> tt.execute(status1 -> {
				status1.setRollbackOnly();
				return null;
			}));
			fail("Should have thrown UnexpectedRollbackException");
		} catch (UnexpectedRollbackException ex) {
			// expected
		}

		verify(session).beginTransaction(any(Transaction.Type.class), anyCollection());
		verify(tx).rollback();
		verify(tx).close();
	}

	//
	// @Test
	// public void testTransactionCommitWithPreBound() throws Exception {
	// tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
	// final List l = new ArrayList();
	// l.add("test");
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	//
	// Object result = tt.execute(new TransactionCallback() {
	// @Override
	// public Object doInTransaction(TransactionStatus status) {
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
	// assertTrue("Has thread transaction", sessionHolder.getSession().getTransaction() != null);
	// Session sess = ((SessionHolder) TransactionSynchronizationManager.getResource(sf)).getSession();
	// assertEquals(session, sess);
	// return l;
	// }
	// });
	// assertTrue("Correct result list", result == l);
	//
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
	// assertTrue("Hasn't thread transaction", sessionHolder.getSession().getTransaction() == null);
	// TransactionSynchronizationManager.unbindResource(sf);
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	//
	// verify(tx).commit();
	// }
	//
	// @Test
	// public void testTransactionRollbackWithPreBound() throws Exception {
	//
	// final Transaction tx1 = mock(Transaction.class);
	// final Transaction tx2 = mock(Transaction.class);
	//
	// given(session.beginTransaction()).willReturn(tx1, tx2);
	//
	// tm.setSessionFactory(sf);
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	//
	// try {
	// tt.execute(new TransactionCallbackWithoutResult() {
	// @Override
	// public void doInTransactionWithoutResult(TransactionStatus status) {
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
	// assertEquals(tx1, sessionHolder.getSession().getTransaction());
	// tt.execute(new TransactionCallbackWithoutResult() {
	// @Override
	// public void doInTransactionWithoutResult(TransactionStatus status) {
	// status.setRollbackOnly();
	// Session sess = ((SessionHolder) TransactionSynchronizationManager.getResource(sf)).getSession();
	// assertEquals(session, sess);
	// }
	// });
	// }
	// });
	// fail("Should have thrown UnexpectedRollbackException");
	// }
	// catch (UnexpectedRollbackException ex) {
	// // expected
	// }
	//
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
	// assertTrue("Hasn't thread transaction", sessionHolder.getSession().getTransaction() == null);
	// assertTrue("Not marked rollback-only", !sessionHolder.isRollbackOnly());
	//
	// tt.execute(new TransactionCallbackWithoutResult() {
	// @Override
	// public void doInTransactionWithoutResult(TransactionStatus status) {
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
	// assertEquals(tx2, sessionHolder.getSession().getTransaction());
	// Session sess = ((SessionHolder) TransactionSynchronizationManager.getResource(sf)).getSession();
	// assertEquals(session, sess);
	// }
	// });
	//
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// assertTrue("Hasn't thread transaction", sessionHolder.getSession().getTransaction() == null);
	// TransactionSynchronizationManager.unbindResource(sf);
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	//
	// verify(tx1).rollback();
	// verify(tx2).commit();
	// InOrder ordered = inOrder(session);
	// ordered.verify(session).clear();
	// }
	//
	//
	// @Test
	// public void testTransactionCommitWithNonExistingDatabase() throws Exception {
	// tm.setSessionFactory(sf);
	// tm.afterPropertiesSet();
	// TransactionTemplate tt = new TransactionTemplate(tm);
	// tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
	// tt.setTimeout(10);
	// assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	//
	// try {
	// tt.execute(new TransactionCallback() {
	// @Override
	// public Object doInTransaction(TransactionStatus status) {
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// Session session = ((SessionHolder) TransactionSynchronizationManager.getResource(sf)).getSession();
	// return session.query("from java.lang.Object", Collections.<String, Object>emptyMap()).queryResults();
	// }
	// });
	// fail("Should have thrown CannotCreateTransactionException");
	// }
	// catch (CannotCreateTransactionException ex) {
	// // expected
	// }
	//
	// assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	// }
	//
	// @Test
	// public void testTransactionCommitWithPreBoundSessionAndNonExistingDatabase() throws Exception {
	// tm.setSessionFactory(sf);
	// tm.afterPropertiesSet();
	// tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
	// tt.setTimeout(10);
	// assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	//
	// Session session = sf.openSession();
	// TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
	// try {
	// tt.execute(new TransactionCallback() {
	// @Override
	// public Object doInTransaction(TransactionStatus status) {
	// assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
	// Session session = ((SessionHolder) TransactionSynchronizationManager.getResource(sf)).getSession();
	// return session.query("from java.lang.Object", Collections.<String, Object>emptyMap()).queryResults();
	// }
	// });
	// fail("Should have thrown CannotCreateTransactionException");
	// }
	// catch (CannotCreateTransactionException ex) {
	// // expected
	// SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
	// assertFalse(holder.isSynchronizedWithTransaction());
	// }
	// finally {
	// TransactionSynchronizationManager.unbindResource(sf);
	// }
	//
	// assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
	// assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	// }
	//
	//
	// @Test
	// public void testTransactionCommitWithRollbackException() {
	// willThrow(new RuntimeException()).given(tx).commit();
	//
	// final List<String> l = new ArrayList<>();
	// l.add("test");
	//
	// assertTrue(!TransactionSynchronizationManager.hasResource(sf));
	// assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
	//
	// try {
	// Object result = tt.execute(new TransactionCallback() {
	// @Override
	// public Object doInTransaction(TransactionStatus status) {
	// assertTrue(TransactionSynchronizationManager.hasResource(sf));
	// return l;
	// }
	// });
	// assertSame(l, result);
	// } catch (TransactionSystemException tse) {
	// // expected
	// assertTrue(tse.getCause() instanceof RuntimeException);
	// }
	//
	// assertTrue(!TransactionSynchronizationManager.hasResource(sf));
	// assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
	// }
	//
}
