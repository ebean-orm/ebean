package misc.migration.history.v1_0;


import io.ebean.annotation.History;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
