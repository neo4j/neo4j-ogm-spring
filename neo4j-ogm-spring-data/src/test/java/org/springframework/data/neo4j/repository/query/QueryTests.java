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
package org.springframework.data.neo4j.repository.query;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 *
 * @author Gerrit Meier
 */
class QueryTests {

	final Query query = new Query(new Filters(new Filter("someProperty", ComparisonOperator.EXISTS)), 10, Sort.unsorted());

	@Test
	void constructsCorrectPaginationParameters() {
		var pagination = query.getOptionalPagination(Pageable.ofSize(5), false);

		assertThat(pagination.toString()).isEqualTo(" SKIP 0 LIMIT 5");
	}

	@Test
	void constructsCorrectPaginationParametersForSlicing() {
		var pagination = query.getOptionalPagination(Pageable.ofSize(5), true);

		assertThat(pagination.toString()).isEqualTo(" SKIP 0 LIMIT 6");
	}

	@Test
	void doesThrowHelpfulExceptionOnOverflow() {
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> query.getOptionalPagination(Pageable.ofSize(2000).withPage(1100000), false))
				.withMessageContaining("Page offset 2200000000 exceeds the maximum size of 2147483647");
	}
}
