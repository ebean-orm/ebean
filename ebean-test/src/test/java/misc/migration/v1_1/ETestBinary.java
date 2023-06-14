package misc.migration.v1_1;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "migtest_e_test_binary")
public class ETestBinary {

  @Id
  int id;

  @Size(max = 16)
  byte[] test_byte16;

  @Size(max = 256)
  byte[] test_byte256;

  @Size(max = 512)
  byte[] test_byte512;

  @Size(max = 1024)
  byte[] test_byte1k;

  @Size(max = 2 * 1024)
  byte[] test_byte2k;

  @Size(max = 4 * 1024)
  byte[] test_byte4k;

  @Size(max = 8 * 1024)
  byte[] test_byte8k;

  @Size(max = 16 * 1024)
  byte[] test_byte16k;

  @Size(max = 32 * 1024)
  byte[] test_byte32k;

  @Size(max = 64 * 1024)
  byte[] test_byte64k;

  @Size(max = 128 * 1024)
  byte[] test_byte128k;

  @Size(max = 256 * 1024)
  byte[] test_byte256k;

  @Size(max = 512 * 1024)
  byte[] test_byte512k;

  @Size(max = 1024 * 1024)
  byte[] test_byte1m;

  @Size(max = 2 * 1024 * 1024)
  byte[] test_byte2m;

  @Size(max = 4 * 1024 * 1024)
  byte[] test_byte4m;

  @Size(max = 8 * 1024 * 1024)
  byte[] test_byte8m;

  @Size(max = 16 * 1024 * 1024)
  byte[] test_byte16m;

  @Size(max = 32 * 1024 * 1024)
  byte[] test_byte32m;

}
