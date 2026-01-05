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
package org.springframework.data.neo4j.repository.query.spel;

import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.data.expression.ValueExpressionParser;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethodValueEvaluationContextAccessor;
import org.springframework.data.repository.query.ValueExpressionQueryRewriter;
import org.springframework.data.repository.query.ValueExpressionQueryRewriter.EvaluatingValueExpressionQueryRewriter;
import org.springframework.data.repository.query.ValueExpressionQueryRewriter.QueryExpressionEvaluator;

public class ParameterizedQuery {

	private final Parameters<?, ?> methodParameters;
	private final QueryExpressionEvaluator expressionEvaluator;

	private ParameterizedQuery(Parameters<?, ?> methodParameters, QueryExpressionEvaluator expressionEvaluator) {
		this.methodParameters = methodParameters;
		this.expressionEvaluator = expressionEvaluator;
	}

	public static ParameterizedQuery getParameterizedQuery(String queryString, Parameters<?, ?> methodParameters, QueryMethodValueEvaluationContextAccessor evaluationContextProvider) {

		Neo4jQueryPlaceholderSupplier supplier = new Neo4jQueryPlaceholderSupplier();

		EvaluatingValueExpressionQueryRewriter rewriter = ValueExpressionQueryRewriter.of(ValueExpressionParser.create(),
				(index, prefix) -> supplier.parameterName(index),
				(prefix, name) -> supplier.decoratePlaceholder(name)).withEvaluationContextAccessor(evaluationContextProvider);

		return new ParameterizedQuery(methodParameters, rewriter.parse(queryString, methodParameters));
	}

	public Map<String, Object> resolveParameter(Object[] parameters,
	                                            BiFunction<Parameters<?, ?>, Object[], Map<String, Object>> nativePlaceholderFunction) {

		Map<String, Object> parameterValues = expressionEvaluator.evaluate(parameters);
		Map<String, Object> nativeParameterValues = nativePlaceholderFunction.apply(methodParameters, parameters);
		parameterValues.putAll(nativeParameterValues);
		return parameterValues;
	}

	public String getQueryString() {
		return expressionEvaluator.getQueryString();
	}
}
