-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_ckey_assoc (
  id                            UInt32,
  assoc_one                     Nullable(String)
) ENGINE = Log();

create table migtest_ckey_detail (
  id                            UInt32,
  something                     Nullable(String)
) ENGINE = Log();

create table migtest_ckey_parent (
  one_key                       UInt32,
  two_key                       String,
  name                          Nullable(String),
  version                       UInt32
) ENGINE = Log();

create table migtest_fk_cascade (
  id                            UInt64,
  one_id                        Nullable(UInt64)
) ENGINE = Log();

create table migtest_fk_cascade_one (
  id                            UInt64
) ENGINE = Log();

create table migtest_fk_none (
  id                            UInt64,
  one_id                        Nullable(UInt64)
) ENGINE = Log();

create table migtest_fk_none_via_join (
  id                            UInt64,
  one_id                        Nullable(UInt64)
) ENGINE = Log();

create table migtest_fk_one (
  id                            UInt64
) ENGINE = Log();

create table migtest_fk_set_null (
  id                            UInt64,
  one_id                        Nullable(UInt64)
) ENGINE = Log();

create table migtest_e_basic (
  id                            UInt32,
  status                        Nullable(String),
  status2                       String default 'N',
  name                          Nullable(String),
  description                   Nullable(String),
  description_file              Nullable(blob),
  json_list                     Nullable(JSON),
  a_lob                         String default 'X',
  some_date                     Nullable(DateTime),
  old_boolean                   Bool default false,
  old_boolean2                  Nullable(Bool),
  eref_id                       Nullable(UInt32),
  indextest1                    Nullable(String),
  indextest2                    Nullable(String),
  indextest3                    Nullable(String),
  indextest4                    Nullable(String),
  indextest5                    Nullable(String),
  indextest6                    Nullable(String),
  user_id                       UInt32
) ENGINE = Log();

create table migtest_e_enum (
  id                            UInt32,
  test_status                   Nullable(String)
) ENGINE = Log();

create table migtest_e_history (
  id                            UInt32,
  test_string                   Nullable(String)
) ENGINE = Log();

create table migtest_e_history2 (
  id                            UInt32,
  test_string                   Nullable(String),
  obsolete_string1              Nullable(String),
  obsolete_string2              Nullable(String)
) ENGINE = Log();

create table migtest_e_history3 (
  id                            UInt32,
  test_string                   Nullable(String)
) ENGINE = Log();

create table migtest_e_history4 (
  id                            UInt32,
  test_number                   Nullable(UInt32)
) ENGINE = Log();

create table migtest_e_history5 (
  id                            UInt32,
  test_number                   Nullable(UInt32)
) ENGINE = Log();

create table migtest_e_history6 (
  id                            UInt32,
  test_number1                  Nullable(UInt32),
  test_number2                  UInt32
) ENGINE = Log();

create table "migtest_QuOtEd" (
  id                            String,
  status1                       Nullable(String),
  status2                       Nullable(String)
) ENGINE = Log();

create table migtest_e_ref (
  id                            UInt32,
  name                          String
) ENGINE = Log();

create table migtest_e_softdelete (
  id                            UInt32,
  test_string                   Nullable(String)
) ENGINE = Log();

create table "table" (
  "index"                       String,
  "from"                        Nullable(String),
  "to"                          Nullable(String),
  "varchar"                     Nullable(String),
  "foreign"                     Nullable(String),
  textfield                     String
) ENGINE = Log();

create table migtest_mtm_c (
  id                            UInt32,
  name                          Nullable(String)
) ENGINE = Log();

create table migtest_mtm_m (
  id                            UInt64,
  name                          Nullable(String)
) ENGINE = Log();

create table migtest_oto_child (
  id                            UInt32,
  name                          Nullable(String)
) ENGINE = Log();

create table migtest_oto_master (
  id                            UInt64,
  name                          Nullable(String)
) ENGINE = Log();

