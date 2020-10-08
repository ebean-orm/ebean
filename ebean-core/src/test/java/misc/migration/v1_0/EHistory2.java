package misc.migration.v1_0;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.History;
import io.ebean.annotation.HistoryExclude;

@Entity
@Table(name = "migtest_e_history2")
@History
public class EHistory2 {

  @Id
  Integer id;


  String testString;

  @HistoryExclude
  String obsoleteString1;

  String obsoleteString2;
}
