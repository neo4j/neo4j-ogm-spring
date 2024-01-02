/*
 * Copyright 2011-2024 the original author or authors.
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
package org.neo4j.ogm.springframework.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "org.neo4j.ogm")
public class Neo4jOGMProperties {

	/**
	 * An optional list of packages to scan. If empty, all classes annotated with
	 * {@link org.neo4j.ogm.annotation.NodeEntity @NodeEntity}
	 * or {@link org.neo4j.ogm.annotation.RelationshipEntity @RelationshipEntity} will be added to the index.
	 */
	private String[] basePackages;

	/**
	 * Should Neo4j native types be used for dates, times and similar?
	 */
	private boolean useNativeTypes;

	/**
	 * This flag instructs OGM to use all static labels when querying domain objects.
	 */
	private boolean useStrictQuerying;

	public String[] getBasePackages() {
		return basePackages;
	}

	public void setBasePackages(String[] basePackages) {
		this.basePackages = basePackages;
	}

	public boolean isUseNativeTypes() {
		return useNativeTypes;
	}

	public void setUseNativeTypes(boolean useNativeTypes) {
		this.useNativeTypes = useNativeTypes;
	}

	public boolean isUseStrictQuerying() {
		return useStrictQuerying;
	}

	public void setUseStrictQuerying(boolean useStrictQuerying) {
		this.useStrictQuerying = useStrictQuerying;
	}
}
