/*
 /*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.demos.tweets;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.search.demos.tweets.ServiceImpl;
import org.hibernate.search.demos.tweets.Tweet;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceTest {

	private EntityManagerFactory entityManagerFactory;

	@Test
	public void testHibernateSearchJPAAPIUsage() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager( entityManagerFactory.createEntityManager() );
		ServiceImpl service = new ServiceImpl();
		service.fullTextEntityManager = fullTextEntityManager;

		FullTextQuery droolsQuery = service.messagesMentioning( "Drools" );
		Assert.assertEquals( 2, droolsQuery.getResultSize() );
		List list = droolsQuery.getResultList();

		// now with weird characters, still works fine:
		droolsQuery = service.messagesMentioning( "dRoÖls" );
		Assert.assertEquals( 2, droolsQuery.getResultSize() );

		FullTextQuery infinispanQuery = service.messagesMentioning( "infinispan" );
		Assert.assertEquals( 1, infinispanQuery.getResultSize() );
		Tweet infinispanRelatedTweet = (Tweet) infinispanQuery.getResultList().get( 0 );
		Assert.assertEquals( "we are looking forward to Ìnfinispan", infinispanRelatedTweet.getMessage() );

		FullTextQuery messagesBySmartMarketingGuy = service.messagesBy( "SmartMarketingGuy" );
		Assert.assertEquals( 3, messagesBySmartMarketingGuy.getResultSize() );

		entityManager.close();
	}

	@Before
	public void prepareTestData() throws Exception {
		entityManagerFactory = Persistence.createEntityManagerFactory("test-tweets");

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		beginTransaction();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager( entityManagerFactory.createEntityManager() );

		// make sure the indexes are empty at start as we're using a filesystem based index:
		fullTextEntityManager.purgeAll( Tweet.class );
		fullTextEntityManager.flushToIndexes();

		// store some tweets
		fullTextEntityManager.persist( new Tweet( "What's Drools? never heard of it", "SmartMarketingGuy", 50l ) );
		fullTextEntityManager.persist( new Tweet( "We love Hibernate", "SmartMarketingGuy", 30l ) );
		fullTextEntityManager.persist( new Tweet( "I wouldn't vote for Drools", "SmartMarketingGuy", 2l ) );
		//note the accent on "I", still needs to match search for "infinispan"
		fullTextEntityManager.persist( new Tweet( "we are looking forward to Ìnfinispan", "AnotherMarketingGuy", 600000l ) );
		fullTextEntityManager.persist( new Tweet( "Hibernate OGM", "AnotherMarketingGuy", 600001l ) );
		fullTextEntityManager.persist( new Tweet( "What is Hibernate OGM?", "ME!", 61000l ) );

		commitTransaction();
		entityManager.close();
	}

	private void commitTransaction() throws Exception {
		extractJBossTransactionManager(entityManagerFactory).getTransaction().commit();
	}

	private void beginTransaction() throws Exception {
		extractJBossTransactionManager(entityManagerFactory).begin();
	}

	public static TransactionManager extractJBossTransactionManager(EntityManagerFactory factory) {
		SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) ( (HibernateEntityManagerFactory) factory ).getSessionFactory();
		return sessionFactory.getServiceRegistry().getService( JtaPlatform.class ).retrieveTransactionManager();
	}

	@After
	public void cleanData() throws Exception {
		if (entityManagerFactory != null) entityManagerFactory.close();
	}

}
