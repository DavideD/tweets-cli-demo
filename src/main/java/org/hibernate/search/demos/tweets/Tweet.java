/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.demos.tweets;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

/**
 * An entity to index tweets with Hibernate Search.
 *
 * It's using a custom analyzer using a set of stopwords,
 * and we filter out noisy terms or weird characters well.
 */
@Indexed(index = "tweets")
@Analyzer(definition = "english")
@AnalyzerDef(name = "english",
	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
	filters = {
		@TokenFilterDef(factory = ASCIIFoldingFilterFactory.class),
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = StopFilterFactory.class, params = {
				@Parameter(name = "words", value = "stoplist.properties"),
				@Parameter(name = "ignoreCase", value = "false")
		})
})
@Entity
public class Tweet {

	private String id;
	private String message = "";
	private String sender = "";
	private long timestamp = 0L;

	public Tweet() {}

	public Tweet(String message, String sender, long timestamp) {
		this.message = message;
		this.sender = sender;
		this.timestamp = timestamp;
	}

	@Id @GeneratedValue
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	@Field
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	@Field(index=Index.YES, analyze=Analyze.NO)
	public String getSender() { return sender; }
	public void setSender(String sender) { this.sender = sender; }

	@Field
	@NumericField
	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

}
