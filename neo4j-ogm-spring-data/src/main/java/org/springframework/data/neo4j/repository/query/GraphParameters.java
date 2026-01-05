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

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersSource;

/**
 * Custom extension of {@link Parameters} discovering additional to handle @link{Depth} special parameter.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 */
public class GraphParameters extends Parameters<GraphParameters, GraphParameters.GraphParameter> {

	private final int depthIndex;

	GraphParameters(ParametersSource parametersSource) {
		super(parametersSource, GraphParameter::new);
		var depthParameter = super.stream().filter(GraphParameter::isDepthParameter).toList();

		if (depthParameter.size() > 1) {
			throw new IllegalStateException(String.format("Found multiple @Depth annotations on method %s! Only one allowed!",
				parametersSource.getMethod()));
		} else if (depthParameter.isEmpty()) {
			depthIndex = -1;
		} else {
			depthIndex = depthParameter.get(0).getIndex();
		}
	}

	private GraphParameters(List<GraphParameter> originals, int depthIndex) {
		super(originals);
		this.depthIndex = depthIndex;
	}

	@Override
	protected GraphParameters createFrom(List<GraphParameter> parameters) {
		return new GraphParameters(parameters, this.depthIndex);
	}

	int getDepthIndex() {
		return this.depthIndex;
	}

	static class GraphParameter extends Parameter {

		private final MethodParameter parameter;

		/**
		 * Creates a new {@link GraphParameter}.
		 *
		 * @param parameter must not be {@literal null}.
		 */
		GraphParameter(MethodParameter parameter) {
			super(parameter, TypeInformation.of(Parameter.class));
			this.parameter = parameter;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.Parameter#isSpecialParameter()
		 */
		@Override
		public boolean isSpecialParameter() {
			return super.isSpecialParameter() || Distance.class.isAssignableFrom(getType())
			       || parameter.getParameterAnnotation(Depth.class) != null || Distance.class.isAssignableFrom(getType())
			       || Point.class.isAssignableFrom(getType());
		}

		boolean isDepthParameter() {
			return parameter.getParameterAnnotation(Depth.class) != null;
		}
	}
}
