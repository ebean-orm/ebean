package io.ebean.querybean.generator.entities;

import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;

@Entity
@Table(name = "comments")
@NamedEntityGraph(name = "fetch-author", attributeNodes = @NamedAttributeNode("author"))
public class CommentEntity extends Model {

  @Column(nullable = false)
  private String body;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private UserEntity author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private ArticleEntity article;

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public UserEntity getAuthor() {
    return author;
  }

  public void setAuthor(UserEntity author) {
    this.author = author;
  }

  public ArticleEntity getArticle() {
    return article;
  }

  public void setArticle(ArticleEntity article) {
    this.article = article;
  }
}
