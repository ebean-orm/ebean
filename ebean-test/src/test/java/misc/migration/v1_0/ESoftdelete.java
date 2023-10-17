package misc.migration.v1_0;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "migtest_e_softdelete")
public class ESoftdelete {
  
  @Id
  Integer id;
  
  
  String testString;
}
