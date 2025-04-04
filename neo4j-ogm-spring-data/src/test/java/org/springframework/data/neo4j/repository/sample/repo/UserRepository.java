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
package org.springframework.data.neo4j.repository.sample.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.domain.sample.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michal Bachman
 * @author Vince Bickers
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, Long> {

	// DATAGRAPH-813
	Long deleteByLastname(String name); // return a count of deleted objects by name

	// DATAGRAPH-813
	List<Long> removeByLastname(String name); // remove users by name and return an iterable of the removed users' ids

	// DATAGRAPH-813
	Long countByLastname(String name); // return a count of objects with name

	/**
	 * Retrieve users by their lastname. The finder {@literal User.findByLastname} is declared in
	 * {@literal META-INF/orm.xml} .
	 *
	 * @param lastname
	 * @return all users with the given lastname
	 */
	List<User> findByLastname(String lastname);

	/**
	 * Redeclaration of {@link CrudRepository#findById(Object)} (java.io.Serializable)} to change transaction configuration.
	 */
	@Override
	@Transactional
	Optional<User> findById(Long primaryKey);

	/**
	 * Redeclaration of {@link CrudRepository#deleteById(Object)} (java.io.Serializable)}. to make sure the transaction configuration of
	 * the original method is considered if the redeclaration does not carry a {@link Transactional} annotation.
	 */
	@Override
	void deleteById(Long id);

	/**
	 * Retrieve users by their email address. The finder {@literal User.findByEmailAddress} is declared as annotation at
	 * {@code User}.
	 *
	 * @param emailAddress
	 * @return the user with the given email address
	 */
	User findByEmailAddress(String emailAddress);

	/**
	 * Retrieves a user by its username using the query annotated to the method.
	 *
	 * @param emailAddress
	 * @return
	 */
	@Query("MATCH (n:User{emailAddress:$emailAddress}) return n")
	@Transactional(readOnly = true)
	User findByAnnotatedQuery(@Param("emailAddress") String emailAddress);
}
