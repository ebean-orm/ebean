package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "migtest_e_test_lob")
public class ETestLob {

  @Id
  int id;

  @Size(max = 255)
  @Lob
  String lob255;

  @Size(max = 256)
  @Lob
  String lob256;

  @Size(max = 512)
  @Lob
  String lob512;

  @Size(max = 1024)
  @Lob
  String lob1k;

  @Size(max = 2 * 1024)
  @Lob
  String lob2k;

  @Size(max = 4 * 1024)
  @Lob
  String lob4k;

  @Size(max = 8 * 1024)
  @Lob
  String lob8k;

  @Size(max = 16 * 1024)
  @Lob
  String lob16k;

  @Size(max = 32 * 1024)
  @Lob
  String lob32k;

  @Size(max = 64 * 1024)
  @Lob
  String lob64k;

  @Size(max = 128 * 1024)
  @Lob
  String lob128k;

  @Size(max = 256 * 1024)
  @Lob
  String lob256k;

  @Size(max = 512 * 1024)
  @Lob
  String lob512k;

  @Size(max = 1024 * 1024)
  @Lob
  String lob1m;

  @Size(max = 2 * 1024 * 1024)
  @Lob
  String lob2m;

  @Size(max = 4 * 1024 * 1024)
  @Lob
  String lob4m;

  @Size(max = 8 * 1024 * 1024)
  @Lob
  String lob8m;

  @Size(max = 16 * 1024 * 1024)
  @Lob
  String lob16m;

  @Size(max = 32 * 1024 * 1024)
  @Lob
  String lob32m;

}
