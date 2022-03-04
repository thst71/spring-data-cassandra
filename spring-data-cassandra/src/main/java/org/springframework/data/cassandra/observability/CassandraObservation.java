/*
 * Copyright 2013-2022 the original author or authors.
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
package org.springframework.data.cassandra.observability;

import io.micrometer.core.instrument.docs.DocumentedObservation;
import io.micrometer.core.instrument.docs.TagKey;

/**
 * Cassandra-based implemention of {@link DocumentedObservation}.
 *
 * @author Mark Paluch
 * @author Marcin Grzejszczak
 * @author Greg Turnquist
 * @since 4.0.0
 */
enum CassandraObservation implements DocumentedObservation {

	/**
	 * Create an {@link io.micrometer.core.instrument.observation.Observation} for Cassandra-based queries.
	 */
	CASSANDRA_QUERY_OBSERVATION {

		@Override
		public String getName() {
			return "spring.data.cassandra.query";
		}

		@Override
		public String getContextualName() {
			return "query";
		}

		@Override
		public TagKey[] getLowCardinalityTagKeys() {
			return LowCardinalityTags.values();
		}

		@Override
		public TagKey[] getHighCardinalityTagKeys() {
			return HighCardinalityTags.values();
		}

		@Override
		public String getPrefix() {
			return "spring.data.cassandra.";
		}
	};

	enum LowCardinalityTags implements TagKey {

		/**
		 * Name of the Cassandra keyspace.
		 */
		KEYSPACE_NAME {
			@Override
			public String getKey() {
				return "spring.data.cassandra.keyspace";
			}
		},

		/**
		 * Cassandra session
		 */
		SESSION_NAME {
			@Override
			public String getKey() {
				return "spring.data.cassandra.sessionName";
			}
		},

		/**
		 * The method name
		 */
		METHOD_NAME {
			@Override
			public String getKey() {
				return "spring.data.cassandra.methodName";
			}
		},

		/**
		 * Cassandra URL
		 */
		URL {
			@Override
			public String getKey() {
				return "spring.data.cassandra.url";
			}
		},

		/**
		 * A tag containing error that occurred for the given node.
		 */
		NODE_ERROR_TAG {
			@Override
			public String getKey() {
				return "spring.data.cassandra.node[%s].error";
			}
		}
	}

	enum HighCardinalityTags implements TagKey {

		/**
		 * A tag containing Cassandra CQL.
		 */
		CQL_TAG {
			@Override
			public String getKey() {
				return "spring.data.cassandra.cql";
			}
		}
	}
}
