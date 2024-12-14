package io.ebean.querybean.generator.entities;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "realworld.article")
public class ArticleEntity extends Model {

  @Id @GeneratedValue private UUID id;

  @WhenCreated
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @WhenModified
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity author;

  @Column(nullable = false)
  private String slug;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private String body;

  @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private final List<ArticleTags> tags = new ArrayList<>();

  public UUID id() {
    return id;
  }

  public ArticleEntity id(UUID id) {
    this.id = id;
    return this;
  }

  public UserEntity author() {
    return author;
  }

  public ArticleEntity author(UserEntity user) {
    this.author = user;
    return this;
  }

  public String slug() {
    return slug;
  }

  public ArticleEntity slug(String slug) {
    this.slug = slug;
    return this;
  }

  public String title() {
    return title;
  }

  public ArticleEntity title(String title) {
    this.title = title;
    return this;
  }

  public String description() {
    return description;
  }

  public ArticleEntity description(String description) {
    this.description = description;
    return this;
  }

  public String body() {
    return body;
  }

  public ArticleEntity body(String body) {
    this.body = body;
    return this;
  }

  public List<ArticleTags> tags() {
    return tags;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public void createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime updatedAt() {
    return updatedAt;
  }

  public void updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
