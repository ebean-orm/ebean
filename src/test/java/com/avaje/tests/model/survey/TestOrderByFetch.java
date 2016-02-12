package com.avaje.tests.model.survey;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;

import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TestOrderByFetch extends BaseTestCase {

  @Test
  public void test() {

    Survey survey = new Survey();
    List<Category> categories = new ArrayList<Category>();
    Category category = new Category();
    category.setSequenceNumber(1);
    categories.add(category);
    survey.setCategories(categories);
    List<Group> groups = new ArrayList<Group>();    
    Group group1 = new Group();
    group1.setSequenceNumber(1);
    Group group2 = new Group();
    group2.setSequenceNumber(2);
    groups.add(group1);
    groups.add(group2);
    category.setGroups(groups);

    List<Question> questionListGroup1 = new ArrayList<Question>();
    Question q1 = new Question();
    q1.setSequenceNumber(1);
    Question q2 = new Question();
    q2.setSequenceNumber(2);
    questionListGroup1.add(q1);
    questionListGroup1.add(q2);
    group1.setQuestions(questionListGroup1);

    List<Question> questionListGroup2 = new ArrayList<Question>();
    Question q3 = new Question();
    q3.setSequenceNumber(1);
    Question q4 = new Question();
    q4.setSequenceNumber(2);
    questionListGroup2.add(q3);
    questionListGroup2.add(q4);
    group2.setQuestions(questionListGroup2);

    Ebean.save(survey);

    Survey foundSurvey = Ebean.find(Survey.class)
       .fetch("categories")
       .fetch("categories.groups")
       .fetch("categories.groups.questions")
      .findUnique();

    assertEquals(1, foundSurvey.getCategories().size());
    assertEquals(2, foundSurvey.getCategories().get(0).getGroups().size());
    
  }

}
