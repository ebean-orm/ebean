package org.tests.model.embedded;

import io.ebean.annotation.DbArray;

import javax.persistence.*;
import java.util.List;

@Entity
public class EmbArrayMaster {

  @Embeddable
  public static class EmbArrayDetail {
    @DbArray
    List<String> vals;

    public EmbArrayDetail(List<String> vals) {
      this.vals = vals;
    }
  }

  @Id
  int id;

  @ElementCollection
  @CollectionTable(name = "test_array_detail", joinColumns = {@JoinColumn(name = "master_id")})
  List<EmbArrayDetail> details;

  public EmbArrayMaster(List<EmbArrayDetail> details) {
    this.details = details;
  }
}
