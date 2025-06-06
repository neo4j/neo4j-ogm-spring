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
package org.springframework.data.neo4j.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Neo4j OGM specific extension of {@link org.springframework.data.repository.Repository}.
 *
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Mark Paluch
 * @author Jens Schauder
 */
@NoRepositoryBean
public interface Neo4jRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID>, CrudRepository<T, ID> {

	<S extends T> S save(S s, int depth);

	<S extends T> Iterable<S> save(Iterable<S> entities, int depth);

	Optional<T> findById(ID id, int depth);

	Iterable<T> findAll();

	Iterable<T> findAll(int depth);

	Iterable<T> findAll(Sort sort);

	Iterable<T> findAll(Sort sort, int depth);

	Iterable<T> findAllById(Iterable<ID> ids);

	Iterable<T> findAllById(Iterable<ID> ids, int depth);

	Iterable<T> findAllById(Iterable<ID> ids, Sort sort);

	Iterable<T> findAllById(Iterable<ID> ids, Sort sort, int depth);

	/**
	 * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
	 * {@link Page#getTotalPages()} returns an estimation of the total number of pages and should not be relied upon for
	 * accuracy.
	 *
	 * @param pageable
	 * @return a page of entities
	 */
	Page<T> findAll(Pageable pageable);

	/**
	 * Returns a {@link Page} of entities meeting the paging restriction provided in the {@code Pageable} object.
	 * {@link Page#getTotalPages()} returns an estimation of the total number of pages and should not be relied upon for
	 * accuracy.
	 *
	 * @param pageable
	 * @param depth
	 * @return a page of entities
	 */
	Page<T> findAll(Pageable pageable, int depth);
}
