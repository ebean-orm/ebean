package misc.migration.v1_0;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_ref")
public class ERef {
  
  @Id
  Integer id;
  
  @OneToMany
  List<EBasic> basics;
}
