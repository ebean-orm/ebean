package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Article;
import org.tests.model.basic.Section;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.ebean.Query.LockType.NO_KEY_UPDATE;
import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryForUpdatePostgresLock extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestQueryForUpdatePostgresLock.class);

  private long timePreInsert;
  private long timePostInsert;
  private long timePreLock;
  private long timePostLock;

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void testForUpdatePostgresLock() throws InterruptedException {

    Article article = new Article("lockTest", "auth");
    DB.save(article);
    final Integer id = article.getId();

    ExecutorService exec = Executors.newFixedThreadPool(2);
    exec.submit(() -> lockArticle(id));
    exec.submit(() -> insertSection(id));
    exec.awaitTermination(2, TimeUnit.SECONDS);
    exec.shutdown();

    // assert the lock was obtained before the insert was attempted
    assertThat(timePreLock).isLessThan(timePreInsert);
    // assert that the insert wasn't waiting on the lock to complete
    assertThat(timePostInsert).isLessThan(timePostLock);
  }

  /**
   * This holds row lock on article for 1 second.
   * With FOR NO KEY UPDATE this does not block the insert.
   */
  @Transactional
  private void lockArticle(Integer id) {
    timePreLock = System.currentTimeMillis();
    log.info("lock start");
    DB.find(Article.class).setId(id).withLock(NO_KEY_UPDATE).findOne();
    sleep(1000);
    timePostLock = System.currentTimeMillis();
    log.info("lock done");
  }

  /**
   * This inserts with FK to the article that is locked.
   * With FOR NO KEY UPDATE this insert does not wait on the lock.
   */
  @Transactional
  private void insertSection(Integer id) {
    sleep(100);
    log.info("insert start");
    timePreInsert = System.currentTimeMillis();
    Section section = new Section();
    section.setArticle(DB.getReference(Article.class, id));
    DB.save(section);
    timePostInsert = System.currentTimeMillis();
    log.info("inserted");
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
