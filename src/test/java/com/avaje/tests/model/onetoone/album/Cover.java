package com.avaje.tests.model.onetoone.album;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.SoftDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PreRemove;

@Entity
public class Cover extends Model {

  private static final Logger logger = LoggerFactory.getLogger(Cover.class);

  public static final Finder<Long, Cover> find = new Finder<Long, Cover>(Cover.class);

  @Id
  protected Long id;

  @SoftDelete
  @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  protected boolean deleted;

  protected String s3Url;

  public String getS3Url() {
    return this.s3Url;
  }

  public void setS3Url(String s3Url) {
    this.s3Url = s3Url;
  }

  @PreRemove
  public void deleteRemoteFile() {
    logger.debug("Cover::deleteRemoteFile() --> Remove file from Amazon S3");
  }

  public boolean isDeleted() {
    return this.deleted;
  }

  public Long getId() {
    return this.id;
  }

}
