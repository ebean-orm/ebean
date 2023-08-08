package misc.migration.v1_1;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "migtest_e_test_varchar")
public class ETestVarchar {

  @Id
  int id;

  @Size(max = 255)
  String varchar255;

  @Size(max = 256)
  String varchar256;

  @Size(max = 512)
  String varchar512;

  @Size(max = 1024)
  String varchar1k;

  @Size(max = 2 * 1024)
  String varchar2k;

  @Size(max = 4 * 1024)
  String varchar4k;

  // Override definition for MariaDB
  @Column(columnDefinition = "mariadb,mysql;varchar(8193);")
  @Size(max = 8 * 1024)
  String varchar8k;

  @Size(max = 16 * 1024)
  String varchar16k;

  @Size(max = 32 * 1024)
  String varchar32k;

  @Size(max = 64 * 1024)
  String varchar64k;

  @Size(max = 128 * 1024)
  String varchar128k;

  @Size(max = 256 * 1024)
  String varchar256k;

  @Size(max = 512 * 1024)
  String varchar512k;

  @Size(max = 1024 * 1024)
  String varchar1m;

  @Size(max = 2 * 1024 * 1024)
  String varchar2m;

  @Size(max = 4 * 1024 * 1024)
  String varchar4m;

  @Size(max = 8 * 1024 * 1024)
  String varchar8m;

  @Size(max = 16 * 1024 * 1024)
  String varchar16m;

  @Size(max = 32 * 1024 * 1024)
  String varchar32m;

}
