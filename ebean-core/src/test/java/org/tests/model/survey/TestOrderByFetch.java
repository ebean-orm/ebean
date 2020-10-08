package org.tests.model.survey;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestOrderByFetch extends BaseTestCase {

  @Test
  public void test() {

    Survey survey = new Survey("s1");
    List<Category> categories = new ArrayList<>();
    Category category = new Category("c1");
    category.setSequenceNumber(1);
    categories.add(category);
    survey.setCategories(categories);
    List<Group> groups = new ArrayList<>();
    Group group1 = group(1);
    Group group2 = group(2);
    groups.add(group1);
    groups.add(group2);
    category.setGroups(groups);

    List<Question> questionListGroup1 = new ArrayList<>();
    questionListGroup1.add(question(1));
    questionListGroup1.add(question(2));
    questionListGroup1.add(question(3));
    group1.setQuestions(questionListGroup1);

    List<Question> questionListGroup2 = new ArrayList<>();
    questionListGroup2.add(question(1));
    questionListGroup2.add(question(2));
    group2.setQuestions(questionListGroup2);

    Ebean.save(survey);

    Survey foundSurvey = Ebean.find(Survey.class)
      .setDisableLazyLoading(true)
      .fetch("categories")
      .fetch("categories.groups")
      .fetch("categories.groups.questions")
      .findOne();

    assertEquals(1, foundSurvey.getCategories().size());

    Category category1 = foundSurvey.getCategories().get(0);
    List<Group> groups2 = category1.getGroups();
    assertEquals(2, groups2.size());
  }

  private Group group(int number) {
    Group group1 = new Group("g" + number);
    group1.setSequenceNumber(number);
    return group1;
  }

  private Question question(int sequence) {
    Question q2 = new Question("q" + sequence);
    q2.setSequenceNumber(sequence);
    return q2;
  }

}
