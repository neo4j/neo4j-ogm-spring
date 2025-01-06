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
package org.springframework.data.neo4j.conversion;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.session.Utils;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.EntityInstantiator;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.neo4j.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.mapping.Neo4jPersistentEntity;
import org.springframework.data.neo4j.mapping.Neo4jPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Implements OGM instantiation callback in order to user Spring Data Commons infrastructure for instantiation.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 */
public class Neo4jOgmEntityInstantiatorAdapter implements org.neo4j.ogm.session.EntityInstantiator {

	private final Neo4jMappingContext context;
	private ConversionService conversionService;

	public Neo4jOgmEntityInstantiatorAdapter(MappingContext<Neo4jPersistentEntity<?>, Neo4jPersistentProperty> context,
			@Nullable ConversionService conversionService) {
		Assert.notNull(context, "MappingContext cannot be null");

		this.context = (Neo4jMappingContext) context;
		this.conversionService = conversionService;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T> T createInstance(Class<T> type, Map<String, Object> propertyValues) {

		Neo4jPersistentEntity<T> persistentEntity = (Neo4jPersistentEntity<T>) context.getRequiredPersistentEntity(type);
		EntityInstantiator sdnInstantiator = context.getInstantiatorFor(persistentEntity);

		return sdnInstantiator.createInstance(persistentEntity, getParameterProvider(propertyValues, conversionService));
	}

	@Override
	public <T> T createInstanceWithConstructorArgs(Class<T> type, Map<String, Object> propertyValues) {
		return createInstance(type, propertyValues);
	}

	private ParameterValueProvider<Neo4jPersistentProperty> getParameterProvider(Map<String, Object> propertyValues,
			ConversionService conversionService) {
		return new Neo4jPropertyValueProvider(propertyValues, conversionService);
	}

	private static class Neo4jPropertyValueProvider implements ParameterValueProvider<Neo4jPersistentProperty> {

		private Map<String, Object> propertyValues;
		private ConversionService conversionService;

		Neo4jPropertyValueProvider(Map<String, Object> propertyValues, ConversionService conversionService) {
			this.conversionService = conversionService;
			Assert.notNull(propertyValues, "Properties cannot be null");
			this.propertyValues = propertyValues;
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		@Nullable
		public <T> T getParameterValue(Parameter<T, Neo4jPersistentProperty> parameter) {

			T value = extractParameterValue(parameter);
			if (value == null || conversionService == null) {
				return value;
			} else {
				return conversionService.convert(value, parameter.getType().getType());
			}
		}

		@SuppressWarnings("unchecked")
		private <T> T extractParameterValue(Parameter<T, Neo4jPersistentProperty> parameter) {

			Object value = propertyValues.get(parameter.getName());

			// This recreates the behaviour of
			// org.neo4j.ogm.context.SingleUseEntityMapper.writeProperty and
			// org.neo4j.ogm.context.GraphEntityMapper.writeProperty
			// As sad as it is to have in 3 places, this is the least invasive solution over both Neo4j-OGM and SDN
			if (value != null && value.getClass().isArray()) {
				value = Arrays.asList((Object[]) value);
			}
			TypeInformation<T> typeInformation = parameter.getType();
			if (typeInformation.isCollectionLike()) {
				Class<?> collectionType = typeInformation.getType();
				Class<?> elementType = typeInformation.getComponentType().getType();
				value = collectionType.isArray()
						? EntityAccessManager.merge(collectionType, value, new Object[] {}, elementType)
						: EntityAccessManager.merge(collectionType, value, Collections.EMPTY_LIST, elementType);
			}

			return (T) Utils.coerceTypes(parameter.getType().getType(), value);
		}
	}
}
