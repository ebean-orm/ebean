package misc.migration.v1_1;

import io.ebean.annotation.DbJson;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "migtest_e_test_json")
public class ETestJson {

  @Id
  int id;

  @DbJson(length = 255)
  List<String> json255;

  @DbJson(length = 256)
  List<String> json256;

  @DbJson(length = 512)
  List<String> json512;

  @DbJson(length = 1024)
  List<String> json1k;

  @DbJson(length = 2 * 1024)
  List<String> json2k;

  @DbJson(length = 4 * 1024)
  List<String> json4k;

  @DbJson(length = 8 * 1024)
  List<String> json8k;

  @DbJson(length = 16 * 1024)
  List<String> json16k;

  @DbJson(length = 32 * 1024)
  List<String> json32k;

  @DbJson(length = 64 * 1024)
  List<String> json64k;

  @DbJson(length = 128 * 1024)
  List<String> json128k;

  @DbJson(length = 256 * 1024)
  List<String> json256k;

  @DbJson(length = 512 * 1024)
  List<String> json512k;

  @DbJson(length = 1024 * 1024)
  List<String> json1m;

  @DbJson(length = 2 * 1024 * 1024)
  List<String> json2m;

  @DbJson(length = 4 * 1024 * 1024)
  List<String> json4m;

  @DbJson(length = 8 * 1024 * 1024)
  List<String> json8m;

  @DbJson(length = 16 * 1024 * 1024)
  List<String> json16m;

  @DbJson(length = 32 * 1024 * 1024)
  List<String> json32m;

}
