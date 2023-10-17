package misc.migration.v1_0;

import io.ebean.annotation.Index;
import io.ebean.annotation.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import javax.validation.constraints.Size;
import java.util.List;

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
