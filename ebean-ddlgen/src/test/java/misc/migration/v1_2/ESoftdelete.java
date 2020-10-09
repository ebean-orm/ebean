package misc.migration.v1_2;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_softdelete")
public class ESoftdelete {
  
  @Id
  Integer id;
  
  
  String testString;
}
