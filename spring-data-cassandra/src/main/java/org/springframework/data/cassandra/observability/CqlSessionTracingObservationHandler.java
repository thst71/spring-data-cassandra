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

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.observation.Observation;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link TracingObservationHandler} for {@link CqlSessionContext}.
 *
 * @author Greg Turnquist
 * @since 4.0.0
 */
public class CqlSessionTracingObservationHandler implements TracingObservationHandler<CqlSessionContext> {

	private static final Log log = LogFactory.getLog(CqlSessionTracingObservationHandler.class);

	private final Tracer tracer;

	public CqlSessionTracingObservationHandler(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void onStart(CqlSessionContext context) {

		Span.Builder builder = this.tracer.spanBuilder() //
				.name(context.getContextualName()) //
				.kind(Span.Kind.CLIENT);

		getTracingContext(context).setSpan(builder.start());
	}

	@Override
	public void onStop(CqlSessionContext context) {

		Span span = getRequiredSpan(context);
		tagSpan(context, span);

		String sessionName = null;
		String url = null;

		for (Tag tag : context.getLowCardinalityTags()) {

			if (tag.getKey().equals(CassandraObservation.LowCardinalityTags.SESSION_NAME.getKey())) {
				sessionName = tag.getValue();
			}

			if (tag.getKey().equals(CassandraObservation.LowCardinalityTags.URL.getKey())) {
				url = tag.getValue();
			}
		}

		if (sessionName != null) {
			span.remoteServiceName("cassandra-" + sessionName);
		}

		if (url != null) {
			try {
				URI uri = URI.create(url);
				span.remoteIpAndPort(uri.getHost(), uri.getPort());
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to parse the url [" + url + "]. Won't set this value on the span");
				}
			}
		}

		span.end();
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof CqlSessionContext;
	}

	@Override
	public Tracer getTracer() {
		return this.tracer;
	}
}
