package org.tests.model.basic;

import io.ebean.annotation.Cache;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Cache
@Entity
public class Section extends BasicDomain {

  private static final long serialVersionUID = 1L;

  public enum Type {
    GENERAL,
    NOTE
  }

  @ManyToOne
  Article article;

  Type type = Type.GENERAL;

  @Lob
  String content;

  @OneToMany(cascade = CascadeType.ALL)
  List<SubSection> subSections;

  public Section() {
  }

  public Section(String content) {
    this.content = content;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Article getArticle() {
    return article;
  }

  public void setArticle(Article article) {
    this.article = article;
  }

  public List<SubSection> getSubSections() {
    return subSections;
  }

  public void setSubSections(List<SubSection> subSections) {
    this.subSections = subSections;
  }

  public void addSubSection(SubSection s) {
    if (subSections == null) {
      subSections = new ArrayList<>();
    }
    subSections.add(s);
  }

}
