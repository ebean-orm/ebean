package misc.migration.v1_2;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_history")
public class EHistory {
  
  @Id
  Integer id;
  
  
  Long testString; // keep it as long as history prevents altering
}
