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
package org.neo4j.ogm.spring.it.movies;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons
 */
@RestController
@RequestMapping("/api/movies")
public final class MoviesController {

	private final MovieRepository movieRepository;

	MoviesController(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@GetMapping({ "", "/" })
	public List<Movie> get() {
		return StreamSupport.stream(movieRepository.findAll(Sort.by("title").ascending()).spliterator(), false).collect(
			Collectors.toList());
	}
}
