package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.Article;
import org.tests.model.basic.Section;
import org.tests.model.basic.SubSection;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteCascadingOneToMany extends BaseTestCase {

  @Test
  public void testDeleteCascadingOneToMany() {
    Section s0 = new Section("some content");
    Article a0 = new Article("art1", "auth1");
    a0.addSection(s0);

    DB.save(a0);
    DB.delete(a0);
  }

  @Test
  public void l2cache_updateBeanOnly_expect_doesNotUpdateManyIds() {

    // setup, create our graph - Bean -> OneToMany -> OneToMany
    Section s0 = new Section("c0");
    s0.getSubSections().add(new SubSection("sub0"));
    Article a0 = new Article("a2", "a2");
    a0.getSections().add(s0);
    DB.save(a0);

    // load into l2 bean cache
    final Article bean0 = DB.find(Article.class, a0.getId());
    for (Section section : bean0.getSections()) {
      section.getSubSections().size(); // ensure the collections are loaded into l2 cache
    }

    // mutate the manyIds associated with the bean
    final Article bean1 = DB.find(Article.class, a0.getId());
    final Section section1 = bean1.getSections().get(0);
    section1.getSubSections().clear(); // orphan remove will delete sub0 - this is cause of EntityNotFoundException: Bean not found during lazy load or refresh
    section1.getSubSections().add(new SubSection("sub-replacement"));
    DB.save(bean1);

    bean0.setName("a2mod");
    // save bean but we have not mutated the collections
    // should NOT update the l2 manyIds (Bug will PUT the original manyIds into l2 COLL cache)
    DB.save(bean0);

    // fetch again hitting l2 cache
    final Article beanLast = DB.find(Article.class, a0.getId());

    // invoke lazy loading - hits l2 cache and get the expected result
    for (Section section : beanLast.getSections()) {
      for (SubSection subSection : section.getSubSections()) {
        assertThat(subSection.getTitle()).isEqualTo("sub-replacement");
      }
    }
  }

}
