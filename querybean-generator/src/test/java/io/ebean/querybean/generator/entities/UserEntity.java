package io.ebean.querybean.generator.entities;

import io.ebean.Model;
import io.ebean.annotation.Encrypted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "realworld.user")
public class UserEntity extends Model {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private String username;

  @Encrypted
  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String bio;

  private String image;

  public UUID id() {
    return id;
  }

  public UserEntity id(UUID id) {
    this.id = id;
    return this;
  }

  public String username() {
    return username;
  }

  public UserEntity username(String username) {
    this.username = username;
    return this;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public UserEntity passwordHash(String passwordHash) {
    this.passwordHash = passwordHash;
    return this;
  }

  public String email() {
    return email;
  }

  public UserEntity email(String email) {
    this.email = email;
    return this;
  }

  public String bio() {
    return bio;
  }

  public UserEntity bio(String bio) {
    this.bio = bio;
    return this;
  }

  public String image() {
    return image;
  }

  public UserEntity image(String image) {
    this.image = image;
    return this;
  }
}
