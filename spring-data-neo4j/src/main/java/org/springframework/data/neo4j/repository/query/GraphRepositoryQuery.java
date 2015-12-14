/*
 * Copyright (c)  [2011-2015] "Pivotal Software, Inc." / "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.springframework.data.neo4j.repository.query;


import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Statistics;
import org.neo4j.ogm.session.Session;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;

import java.util.HashMap;
import java.util.Map;


/**
 * Specialisation of {@link RepositoryQuery} that handles mapping to object annotated with <code>&#064;Query</code>.
 *
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class GraphRepositoryQuery implements RepositoryQuery {

    private final GraphQueryMethod graphQueryMethod;

    protected final Session session;

    public GraphRepositoryQuery(GraphQueryMethod graphQueryMethod, Session session) {
        this.graphQueryMethod = graphQueryMethod;
        this.session = session;
    }

    @Override
    public final Object execute(Object[] parameters) {
        Class<?> returnType = graphQueryMethod.getMethod().getReturnType();
        Class<?> concreteType = graphQueryMethod.resolveConcreteReturnType();

        Map<String, Object> params = resolveParams(parameters);
        
        ParameterAccessor accessor = new ParametersParameterAccessor(graphQueryMethod.getParameters(), parameters);
        ResultProcessor processor = graphQueryMethod.getResultProcessor();
        Object result = execute(returnType, concreteType, getQueryString(), params);
        
        return QueryStatistics.class.equals(returnType) ? result : 
        	processor.withDynamicProjection(accessor).processResult(result);
    }

    protected Object execute(Class<?> returnType, Class<?> concreteType, String cypherQuery, Map<String, Object> queryParams) {

        if (returnType.equals(Void.class) || returnType.equals(void.class)) {
            session.query(cypherQuery, queryParams);
            return null;
        }

        if (Iterable.class.isAssignableFrom(returnType) && !queryReturnsStatistics()) {
            // Special method to handle SDN Iterable<Map<String, Object>> behaviour.
            // TODO: Do we really want this method in an OGM? It's a little too low level and/or doesn't really fit.
            if (Map.class.isAssignableFrom(concreteType)) {
                return session.query(cypherQuery, queryParams).model();
            }
            return session.query(concreteType, cypherQuery, queryParams);
        }

        if (queryReturnsStatistics()) {
            return session.query(cypherQuery, queryParams);
        }

        return session.queryForObject(returnType, cypherQuery, queryParams);
    }

    private Map<String, Object> resolveParams(Object[] parameters) {

        Map<String, Object> params = new HashMap<>();
        Parameters<?, ?> methodParameters = graphQueryMethod.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = methodParameters.getParameter(i);

            //The parameter might be an entity, try to resolve its id
            Object parameterValue = session.resolveGraphIdFor(parameters[i]);
            if(parameterValue == null) { //Either not an entity or not persisted
                parameterValue = parameters[i];
            }

            if (parameter.isNamedParameter()) {
                params.put(parameter.getName(), parameterValue);
            } else {
                params.put("" + i, parameterValue);
            }
        }
        return params;
    }

    @Override
    public GraphQueryMethod getQueryMethod() {
        return graphQueryMethod;
    }

    protected String getQueryString() {
        return getQueryMethod().getQuery();
    }

    private boolean queryReturnsStatistics() {
        Class returnType = graphQueryMethod.getMethod().getReturnType();
        return Statistics.class.isAssignableFrom(returnType) || QueryStatistics.class.isAssignableFrom(returnType);
    }

}