package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_user")
public class EUser {
  
  @Id
  Integer id;
}