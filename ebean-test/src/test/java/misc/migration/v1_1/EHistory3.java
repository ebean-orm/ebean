package misc.migration.v1_1;


import io.ebean.annotation.History;
import io.ebean.annotation.HistoryExclude;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "migtest_e_history3")
@History
public class EHistory3 {

  @Id
  Integer id;

  @HistoryExclude
  String testString;

}
