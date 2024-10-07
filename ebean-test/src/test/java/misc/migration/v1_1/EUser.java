package misc.migration.v1_1;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "migtest_e_user")
public class EUser {
  
  @Id
  Integer id;
}