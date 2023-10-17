package misc.migration.v1_1;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "migtest_e_enum")
public class EEnum {
  
  @Id
  Integer id;
  
  @Size (max = 1)
  String testStatus; // change to String from Enum Status
}
