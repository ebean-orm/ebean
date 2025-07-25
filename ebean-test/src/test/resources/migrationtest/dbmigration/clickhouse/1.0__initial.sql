-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            UInt32,
  assoc_one                     String
) ENGINE = Log();

create table migtest_ckey_detail (
  id                            UInt32,
  something                     String
) ENGINE = Log();

create table migtest_ckey_parent (
  one_key                       UInt32,
  two_key                       String,
  name                          String,
  version                       UInt32
) ENGINE = Log();

create table migtest_fk_cascade (
  id                            UInt64,
  one_id                        UInt64
) ENGINE = Log();

create table migtest_fk_cascade_one (
  id                            UInt64
) ENGINE = Log();

create table migtest_fk_none (
  id                            UInt64,
  one_id                        UInt64
) ENGINE = Log();

create table migtest_fk_none_via_join (
  id                            UInt64,
  one_id                        UInt64
) ENGINE = Log();

create table migtest_fk_one (
  id                            UInt64
) ENGINE = Log();

create table migtest_fk_set_null (
  id                            UInt64,
  one_id                        UInt64
) ENGINE = Log();

create table migtest_e_basic (
  id                            UInt32,
  status                        String,
  status2                       String default 'N',
  name                          String,
  description                   String,
  description_file              blob,
  json_list                     String,
  a_lob                         String default 'X',
  some_date                     DateTime,
  old_boolean                   UInt8 default 0,
  old_boolean2                  UInt8,
  eref_id                       UInt32,
  indextest1                    String,
  indextest2                    String,
  indextest3                    String,
  indextest4                    String,
  indextest5                    String,
  indextest6                    String,
  indextest7                    String,
  default_test                  UInt32 default 0,
  user_id                       UInt32
) ENGINE = Log();

create table migtest_e_enum (
  id                            UInt32,
  test_status                   String
) ENGINE = Log();

create table migtest_e_history (
  id                            UInt32,
  test_string                   String
) ENGINE = Log();

create table migtest_e_history2 (
  id                            UInt32,
  test_string                   String,
  obsolete_string1              String,
  obsolete_string2              String
) ENGINE = Log();

create table migtest_e_history3 (
  id                            UInt32,
  test_string                   String
) ENGINE = Log();

create table migtest_e_history4 (
  id                            UInt32,
  test_number                   UInt32
) ENGINE = Log();

create table migtest_e_history5 (
  id                            UInt32,
  test_number                   UInt32
) ENGINE = Log();

create table migtest_e_history6 (
  id                            UInt32,
  test_number1                  UInt32,
  test_number2                  UInt32
) ENGINE = Log();

create table "migtest_QuOtEd" (
  id                            String,
  status1                       String,
  status2                       String
) ENGINE = Log();

create table migtest_e_ref (
  id                            UInt32,
  name                          String
) ENGINE = Log();

create table migtest_e_softdelete (
  id                            UInt32,
  test_string                   String
) ENGINE = Log();

create table "table" (
  "index"                       String,
  "from"                        String,
  "to"                          String,
  "varchar"                     String,
  "foreign"                     String,
  textfield                     String
) ENGINE = Log();

create table migtest_mtm_c (
  id                            UInt32,
  name                          String
) ENGINE = Log();

create table migtest_mtm_m (
  id                            UInt64,
  name                          String
) ENGINE = Log();

create table migtest_oto_child (
  id                            UInt32,
  name                          String
) ENGINE = Log();

create table migtest_oto_master (
  id                            UInt64,
  name                          String
) ENGINE = Log();

