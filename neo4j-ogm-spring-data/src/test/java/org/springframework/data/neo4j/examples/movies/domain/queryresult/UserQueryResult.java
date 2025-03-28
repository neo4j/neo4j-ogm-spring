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
package org.springframework.data.neo4j.examples.movies.domain.queryresult;

import org.neo4j.ogm.annotation.Property;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.examples.movies.repo.UserRepository;

/**
 * Example POJO {@link QueryResult} to test mapping onto arbitrary objects, even for properties that are on the nodes
 * but not in the entity classes.
 *
 * @author Adam George
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
@QueryResult
public class UserQueryResult {

	private Long id;
	Long userId;
	private String userName;

	@Property(name = "user.age") private int age;

	UserQueryResult() {
		// default constructor for OGM
	}

	public UserQueryResult(String name, int age) {
		this.userName = name;
		this.age = age;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public int getAge() {
		return age;
	}

	@Override
	public int hashCode() {
		final int prime = 23;
		int result = 1;
		result = prime * result + age;
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserQueryResult other = (UserQueryResult) obj;
		if (age != other.age)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserQueryResult{" +
				"id=" + id +
				", userId=" + userId +
				", userName='" + userName + '\'' +
				", age=" + age +
				'}';
	}
}
