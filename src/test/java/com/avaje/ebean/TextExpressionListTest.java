package com.avaje.ebean;


import com.avaje.ebean.search.Match;
import com.avaje.ebean.search.MultiMatch;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

public class TextExpressionListTest {

  @Test
  public void syntax() {

    Ebean.find(Order.class)
        .text().match("name", "rob");


    Ebean.find(Order.class)
        .text().should()
          .match("name", "rob")
          .match("note", "war and peace");


    Ebean.find(Order.class)
        .text()
        .should()
          .match("title", "war and peace")
          .match("author", "leo tolstoy")
          .should()
            .match("translator", "Constance Garnett")
            .match("translator", "Louise Maude");


    Ebean.find(Order.class)
        .text()
        .should()
          .match("title", "war and peace")
          .match("author", "leo tolstoy")
          .should()
            .match("translator", "Constance Garnett", Match.AND().boost(2).minShouldMatch("75%"))
            .match("translator", "Louise Maude")
        .where()
          .gt("reviewDate", 12345);


    Ebean.find(Order.class)
        .text()
        .must()
          .match("title", "quick")
        .endMust()
        .should()
          .match("title", "brown")
          .match("title", "dog")
        .endShould()
        .mustNot()
          .match("title", "lazy")
        .endMustNot()
        .where()
          .gt("reviewDate", 12345);


  }

  @Test
  public void syntax_multiMatch() {

    Ebean.find(Order.class)
        .text()
        .multiMatch("Will Smith", "title", "*name");

    MultiMatch match = MultiMatch.fields("title", "*name")
        .opAnd()
        .type(MultiMatch.Type.PHRASE_PREFIX);

    Ebean.find(Order.class)
        .text()
        .multiMatch("Will Smith", match);

  }
}