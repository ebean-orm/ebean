package misc.migration.v1_2;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import io.ebean.annotation.Index;
import io.ebean.annotation.NotNull;

@Entity
@Table(name = "migtest_e_ref")
public class ERef {
  
  @Id
  Integer id;
  
  @OneToMany
  List<EBasic> basics;

  @NotNull
  @Index(unique = true)
  @Size(max=127)
  String name;
}
