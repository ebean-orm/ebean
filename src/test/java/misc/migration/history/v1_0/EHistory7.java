package misc.migration.history.v1_0;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.History;

/**
 * detects a bug where dropHistoryTable is not applied correctly
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
@Entity
@Table(name = "migtest_e_history7")
@History
public class EHistory7 {

  @Id
  Integer id;

}
