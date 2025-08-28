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

import java.util.Objects;

import org.neo4j.ogm.cypher.query.SortOrder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.util.PagingAndSortingUtils;
import org.springframework.data.repository.query.ParametersParameterAccessor;

/**
 * Custom {@link ParametersParameterAccessor} to allow access to the {@link Depth} parameter.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 */
public class GraphParametersParameterAccessor extends ParametersParameterAccessor implements GraphParameterAccessor {

	private static final int DEFAULT_QUERY_DEPTH = 1;
	private final GraphQueryMethod method;

	/**
	 * Creates a new {@link ParametersParameterAccessor}.
	 *
	 * @param method must not be {@literal null}.
	 * @param values must not be {@literal null}.
	 */
	public GraphParametersParameterAccessor(GraphQueryMethod method, Object[] values) {
		super(method.getParameters(), values);

		this.method = method;
	}

	@Override
	public int getDepth() {

		Depth methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), Depth.class);
		if (methodAnnotation != null) {
			Object value = AnnotationUtils.getValue(methodAnnotation);
			return value == null ? DEFAULT_QUERY_DEPTH : (int) value;
		}

		var parameters = method.getParameters();
		if(parameters instanceof GraphParameters graphParameters && graphParameters.getDepthIndex() != -1) {
			return Objects.requireNonNullElse(getValue(graphParameters.getDepthIndex()), DEFAULT_QUERY_DEPTH);
		}
		return DEFAULT_QUERY_DEPTH;
	}

	@Override
	public SortOrder getOgmSort() {

		return PagingAndSortingUtils.convert(getSort());
	}
}
