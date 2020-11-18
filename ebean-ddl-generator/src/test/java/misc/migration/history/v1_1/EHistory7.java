package misc.migration.history.v1_1;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * detects a bug where dropHistoryTable is not applied correctly
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
@Entity
@Table(name = "migtest_e_history7")
public class EHistory7 {

  @Id
  Integer id;

}
