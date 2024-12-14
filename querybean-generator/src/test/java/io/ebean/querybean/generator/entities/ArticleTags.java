package io.ebean.querybean.generator.entities;

import java.util.UUID;

import io.ebean.Model;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "realworld.article_tag")
public class ArticleTags extends Model {

  @EmbeddedId private ArticleTagId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false, insertable = false, updatable = false)
  private final ArticleEntity article;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "tag_id", nullable = false, insertable = false, updatable = false)
  private final TagEntity tag;

  public ArticleTags(ArticleEntity article, TagEntity tag) {
    this.article = article;
    this.tag = tag;
  }

  public ArticleEntity article() {
    return article;
  }

  public TagEntity tag() {
    return tag;
  }

  public ArticleTagId id() {
    return id;
  }

  public void id(ArticleTagId id) {
    this.id = id;
  }

  @Embeddable
  public static class ArticleTagId {
    UUID articleId;
    UUID tagId;

    public ArticleTagId(UUID articleId, UUID tagId) {}

    public UUID getArticleId() {
      return articleId;
    }

    public void setArticleId(UUID articleId) {
      this.articleId = articleId;
    }

    public UUID getTagId() {
      return tagId;
    }

    public void setTagId(UUID tagId) {
      this.tagId = tagId;
    }
  }
}
