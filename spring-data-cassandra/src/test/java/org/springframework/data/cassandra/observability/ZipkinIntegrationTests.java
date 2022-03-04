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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.ObservationHandler;
import io.micrometer.core.instrument.observation.ObservationRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.tracing.test.SampleTestRunner;
import io.micrometer.tracing.test.reporter.BuildingBlocks;

import java.util.Deque;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.support.AbstractTestJavaConfig;
import org.springframework.data.cassandra.test.util.CassandraExtension;
import org.springframework.data.cassandra.test.util.TestKeyspace;
import org.springframework.data.cassandra.test.util.TestKeyspaceName;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.datastax.oss.driver.api.core.CqlSession;

/**
 * Collection of tests that log metrics and tracing with an external tracing tool. Since this external tool must be up
 * and running after the test is completed, this test is ONLY run manually.
 *
 * @author Greg Turnquist
 * @since 4.0.0
 */
@Disabled("Run this manually to visually test spans in Zipkin")
@ExtendWith({ SpringExtension.class, CassandraExtension.class })
@TestKeyspaceName
public class ZipkinIntegrationTests extends SampleTestRunner {

	@Autowired CqlSession session;

	ZipkinIntegrationTests() {
		super(SampleRunnerConfig.builder().build(), TestConfig.METER_REGISTRY);
	}

	@BeforeAll
	static void setUp() {
		TestConfig.METER_REGISTRY.withTimerObservationHandler();
	}

	@Override
	public BiConsumer<BuildingBlocks, Deque<ObservationHandler>> customizeObservationHandlers() {

		return (buildingBlocks, observationHandlers) -> {
			observationHandlers.addLast(new CqlSessionTracingObservationHandler(buildingBlocks.getTracer()));
		};
	}

	@Override
	public TracingSetup[] getTracingSetup() {
		return new TracingSetup[] { TracingSetup.ZIPKIN_BRAVE };
	}

	@Override
	public SampleTestRunnerConsumer yourCode() {

		return (tracer, meterRegistry) -> {

			session.execute("DROP KEYSPACE IF EXISTS ConfigTest");
			session.execute("CREATE KEYSPACE ConfigTest " + "WITH "
					+ "REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
			session.execute("USE ConfigTest");
			session.execute("CREATE TABLE IF NOT EXISTS person (id int, firstName text, lastName text, PRIMARY KEY(id));");

			System.out.println(((SimpleMeterRegistry) meterRegistry).getMetersAsString());
		};
	}

	static class TestConfig extends AbstractTestJavaConfig {

		private final static MeterRegistry METER_REGISTRY = new SimpleMeterRegistry();

		@TestKeyspace CqlSession session;

		@Override
		protected String getKeyspaceName() {
			return "system";
		}

		@Bean
		ObservationRegistry meterRegistry() {
			return METER_REGISTRY;
		}

		@Bean
		CqlSessionTagsProvider tagsProvider() {
			return new DefaultCassandraTagsProvider();
		}

		@Bean
		CqlSessionTracingBeanPostProcessor traceCqlSessionBeanPostProcessor(ObservationRegistry observationRegistry,
				CqlSessionTagsProvider tagsProvider) {
			return new CqlSessionTracingBeanPostProcessor(observationRegistry, tagsProvider);
		}
	}
}
