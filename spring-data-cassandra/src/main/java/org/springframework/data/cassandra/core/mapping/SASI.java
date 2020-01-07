/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.data.cassandra.core.mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Identifies a secondary index using SASI indexing on a single column.
 * <p>
 * SASI uses significantly using fewer memory, disk, and CPU resources. It enables querying with {@literal PREFIX} and
 * {@literal CONTAINS} on strings, similar to the SQL implementation of {@literal LIKE = "foo*"} or
 * {@literal LIKE = "*foo*"}.
 *
 * <pre class="code">
 * &#64;Table
 * class Person {
 *
 * 	&#64;SASI(analyzed = true, indexMode = CONTAINS) @StandardAnalyzed("en") String names; // allows LIKE queries
 * 	&#64;SASI int age // allows age >= … queries;
 * }
 * </pre>
 * <p>
 * SASI indexing can apply an analyzer that is applied during indexing. Annotation-based indexing supports
 * {@link StandardAnalyzed standard analyzer} and {@link NonTokenizingAnalyzed non-tokenizing analyzer}.
 *
 * @author Mark Paluch
 * @since 2.0
 * @see StandardAnalyzed
 * @see NonTokenizingAnalyzed
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface SASI {

	/**
	 * The name of the index. If {@literal null} or empty, then the index name will be generated by Cassandra and will be
	 * unknown unless column metadata is used to discover the generated index name.
	 */
	String value() default "";

	/**
	 * The name of the index. If {@literal null} or empty, then the index name will be generated by Cassandra and will be
	 * unknown unless column metadata is used to discover the generated index name.
	 */
	IndexMode indexMode() default IndexMode.PREFIX;

	enum IndexMode {

		/**
		 * Allows prefix queries.
		 */
		PREFIX,

		/**
		 * Allows prefix, suffix and substring queries.
		 */
		CONTAINS,

		/**
		 * SPARSE mode is optimized for low-cardinality e.g. for indexed values having {@literal 5} or less corresponding
		 * rows.
		 */
		SPARSE
	}

	/**
	 * Apply standard analyzer to SASI indexing. This analyzer is used for analysis that involves stemming, case
	 * normalization, case sensitivity, skipping common words like "and" and "the", and localization of the language used
	 * to complete the analysis
	 *
	 * @see org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer
	 */
	@Retention(value = RetentionPolicy.RUNTIME)
	@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@interface StandardAnalyzed {

		/**
		 * Defines the locale for tokenization.
		 */
		@AliasFor("locale")
		String value() default "en";

		/**
		 * Defines the locale for tokenization.
		 */
		@AliasFor("value")
		String locale() default "en";

		/**
		 * Enable stemming of input text to reduce words to their base form, for example
		 * {@literal stemmer, stemming, stemmed} are based on {@literal stem}.
		 */
		boolean enableStemming() default false;

		/**
		 * Skips stop words from indexing.
		 */
		boolean skipStopWords() default false;

		/**
		 * Applies normalization to uppercase/lowercase.
		 */
		Normalization normalization() default Normalization.NONE;
	}

	/**
	 * Apply non-tokenizing analyzer to SASI indexing. Use this analyzer for cases where the text is not analyzed, but
	 * case normalization or sensitivity is required.
	 *
	 * @see org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer
	 */
	@Retention(value = RetentionPolicy.RUNTIME)
	@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@interface NonTokenizingAnalyzed {

		/**
		 * Enable case-sensitive matching.
		 */
		boolean caseSensitive() default true;

		/**
		 * Applies normalization to uppercase/lowercase.
		 */
		Normalization normalization() default Normalization.NONE;
	}

	enum Normalization {

		/**
		 * Do not apply normalization.
		 */
		NONE,

		/**
		 * Normalize to lowercase.
		 */
		LOWERCASE,

		/**
		 * Normalize to uppercase.
		 */
		UPPERCASE;
	}
}
