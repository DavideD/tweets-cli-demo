/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.demos.tweets;

import java.io.IOException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * Example service
 */
public class ServiceImpl {

	/**
	 * this should be injected before use.
	 */
	public FullTextEntityManager fullTextEntityManager;
	private QueryBuilder tweetQueryBuilder;

	/**
	 * creates a FullTextQuery for all tweets mentioning a specific word/technology in the message field.
	 */
	public FullTextQuery messagesMentioning(String keyword) {
		Query query = getQueryBuilder().keyword().onField( "message" ).matching( keyword ).createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query );
		return fullTextQuery;
	}

	/**
	 * Searches for all tweets from a specific account. This is case-sensitive.
	 */
	public FullTextQuery messagesBy(String name) {
		Query query = getQueryBuilder().keyword().onField( "sender" ).matching( name ).createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query );
		return fullTextQuery;
	}

	/**
	 * To search for all tweets, sorted in creation order (assuming the timestamp is correct).
	 * @return
	 */
	public FullTextQuery allTweetsSortedByTime() {
		Query query = getQueryBuilder().all().createQuery();
		FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery( query );
		fullTextQuery.setSort( new Sort( new SortedNumericSortField( "timestamp", SortField.Type.LONG ) ) );
		return fullTextQuery;
	}

	/**
	 * This is the most complex case, and uses ScoredTerm to represent the return value.
	 * I guess this is a practical way to make a tag cloud out of all indexed terms.
	 * @param inField Will return only scoredTerms in the specified field
	 * @param minimumFrequency a minimum threshold, can be used to reduce not very significant words (see analyzers and stopwords for better results).
	 * @throws IOException
	Set<ScoredTerm> mostFrequentlyUsedTerms(String inField, int minimumFrequency) throws IOException {
		String internedFieldName = inField.intern();
		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		IndexReader indexReader = searchFactory.getIndexReaderAccessor().open( Tweet.class );
		TreeSet<ScoredTerm> sortedTerms = new TreeSet<ScoredTerm>();
		try {
			TermEnum termEnum = indexReader.terms();
			while ( termEnum.next() ) {
				Term term = termEnum.term();
				if ( internedFieldName != term.field() ) {
					continue;
				}
				int docFreq = termEnum.docFreq();
				if ( docFreq < minimumFrequency ) {
					continue;
				}
				String text = term.text();
				sortedTerms.add( new ScoredTerm( text, docFreq ) );
			}
		}
		finally {
			searchFactory.getIndexReaderAccessor().close( indexReader );
			indexReader.close();
		}
		return sortedTerms;
	}*/

	private QueryBuilder getQueryBuilder() {
		if ( tweetQueryBuilder == null ) {
			tweetQueryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity( Tweet.class ).get();
		}
		return tweetQueryBuilder;
	}

}
