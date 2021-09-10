package io.ebean;


import io.ebean.search.Match;
import io.ebean.search.MultiMatch;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;

public class TextExpressionListTest {

  @Test
  public void syntax() {

    DB.find(Order.class)
      .text().match("name", "rob");


    DB.find(Order.class)
      .text().should()
      .match("name", "rob")
      .match("note", "war and peace");


    DB.find(Order.class)
      .text()
      .should()
      .match("title", "war and peace")
      .match("author", "leo tolstoy")
      .should()
      .match("translator", "Constance Garnett")
      .match("translator", "Louise Maude");


    DB.find(Order.class)
      .text()
      .should()
      .match("title", "war and peace")
      .match("author", "leo tolstoy")
      .should()
      .match("translator", "Constance Garnett", new Match().opAnd().boost(2).minShouldMatch("75%"))
      .match("translator", "Louise Maude")
      .where()
      .gt("reviewDate", 12345);


    DB.find(Order.class)
      .text()
      .must()
      .match("title", "quick")
      .endJunction()
      .should()
      .match("title", "brown")
      .match("title", "dog")
      .endJunction()
      .mustNot()
      .match("title", "lazy")
      .endJunction()
      .where()
      .gt("reviewDate", 12345);


  }

  @Test
  public void syntax_multiMatch() {

    DB.find(Order.class)
      .text()
      .multiMatch("Will Smith", "title", "*name");

    MultiMatch match = MultiMatch.fields("title", "*name")
      .opAnd()
      .type(MultiMatch.Type.PHRASE_PREFIX);

    DB.find(Order.class)
      .text()
      .multiMatch("Will Smith", match);

  }
}
