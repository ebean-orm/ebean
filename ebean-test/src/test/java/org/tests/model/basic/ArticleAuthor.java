package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheBeanTuning;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;


@Cache
@CacheBeanTuning(maxSecsToLive = 45)
@Entity
public class ArticleAuthor extends BasicDomain {
  private static final long serialVersionUID = -7181090513848918784L;

  String name;

  @OneToMany(mappedBy = "articleAuthor", cascade = CascadeType.ALL)
  List<Article> articles;

  public ArticleAuthor(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
