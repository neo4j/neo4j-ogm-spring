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

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.domain.sample.NodeWithUUIDAsId;
import org.springframework.data.neo4j.domain.sample.SampleEntity;
import org.springframework.data.neo4j.test.Neo4jIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Angrish
 * @author Mark Paluch
 * @author Jens Schauder
 * @author Michael J. Simons
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Neo4jRepositoryTests.Config.class)
@Transactional
public class Neo4jRepositoryTests {

	@Autowired SampleEntityRepository repository;

	@Autowired NodeWithUUIDAsIdRepository nodeWithUUIDAsIdRepository;

	@Test
	public void testCrudOperationsForCompoundKeyEntity() throws Exception {

		SampleEntity entity = new SampleEntity("foo", "bar");
		repository.save(entity);
		assertThat(repository.existsById(entity.getId())).isTrue();
		assertThat(repository.count()).isEqualTo(1L);

		Optional<SampleEntity> optional = repository.findById(entity.getId());
		assertThat(optional.isPresent()).isTrue();
		optional.ifPresent(actual -> assertThat(actual).isEqualTo(entity));

		repository.deleteAll(Arrays.asList(entity));
		assertThat(repository.count()).isEqualTo(0L);
	}

	@Test // DATAGRAPH-1144
	public void explicitIdsWithCustomTypesShouldWork() throws Exception {

		NodeWithUUIDAsId entity = new NodeWithUUIDAsId("someProperty");
		nodeWithUUIDAsIdRepository.save(entity);

		assertThat(nodeWithUUIDAsIdRepository.existsById(entity.getMyNiceId())).isTrue();
		assertThat(nodeWithUUIDAsIdRepository.count()).isEqualTo(1L);

		Optional<NodeWithUUIDAsId> retrievedEntity = nodeWithUUIDAsIdRepository.findById(entity.getMyNiceId());
		assertThat(retrievedEntity.isPresent()).isTrue();
		assertThat(retrievedEntity.get()).isEqualTo(entity);

		nodeWithUUIDAsIdRepository.deleteAll(Arrays.asList(entity));
		assertThat(nodeWithUUIDAsIdRepository.count()).isEqualTo(0L);
	}

	@Test // DATAGRAPH-1286
	public void findByIdEqualsInDerivedQueryMethodShouldWork() {

		NodeWithUUIDAsId entity = new NodeWithUUIDAsId("someProperty");
		nodeWithUUIDAsIdRepository.save(entity);

		Optional<NodeWithUUIDAsId> retrievedEntity = nodeWithUUIDAsIdRepository
				.findOneByMyNiceIdAndSomeProperty(entity.getMyNiceId(), entity.getSomeProperty());
		assertThat(retrievedEntity.isPresent()).isTrue();
		assertThat(retrievedEntity.get()).isEqualTo(entity);
	}

	@Configuration
	@Neo4jIntegrationTest(domainPackages = "org.springframework.data.neo4j.domain.sample", considerNestedRepositories = true)
	static class Config {}

	interface SampleEntityRepository extends Neo4jRepository<SampleEntity, Long> {}

	interface NodeWithUUIDAsIdRepository extends Neo4jRepository<NodeWithUUIDAsId, UUID> {

		Optional<NodeWithUUIDAsId> findOneByMyNiceIdAndSomeProperty(UUID id, String someProperty);
	}
}
