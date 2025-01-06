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
package org.springframework.data.neo4j.repository.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.Test;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.repository.query.ParametersSource;

/**
 * @author Michael J. Simons
 */
public class GraphParametersTests {

	@Test
	public void shouldWorkWithZeroDepthParameters() throws NoSuchMethodException {

		var method = FakeRepo.class.getMethod("m1", String.class, String.class);
		var graphParameters = new GraphParameters(ParametersSource.of(method));
		assertThat(graphParameters.getDepthIndex()).isEqualTo(-1);
	}

	@Test
	public void shouldWorkWithOneDepthParameter() throws NoSuchMethodException {

		var method = FakeRepo.class.getMethod("m2", String.class, int.class);
		var graphParameters = new GraphParameters(ParametersSource.of(method));
		assertThat(graphParameters.getDepthIndex()).isOne();
	}

	@Test
	public void mustFailOnMultipleDepthParameters() throws NoSuchMethodException {

		var method = FakeRepo.class.getMethod("m3", String.class, int.class, int.class);
		assertThatIllegalStateException().isThrownBy(() -> new GraphParameters(ParametersSource.of(method)))
				.withMessage("Found multiple @Depth annotations on method public abstract void org.springframework.data.neo4j.repository.query.GraphParametersTests$FakeRepo.m3(java.lang.String,int,int)! Only one allowed!");
	}

	interface FakeRepo {

		void m1(String x, String y);

		void m2(String x, @Depth int y);

		void m3(String x, @Depth int y, @Depth int z);
	}
}
