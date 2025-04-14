-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;
-- apply changes
create table drop_main (
  id                            UInt32
) ENGINE = Log();

create table drop_main_drop_ref_many (
  drop_main_id                  UInt32,
  drop_ref_many_id              UInt32
) ENGINE = Log();

create table drop_ref_many (
  id                            UInt32
) ENGINE = Log();

create table drop_ref_one (
  id                            UInt32,
  parent_id                     Nullable(UInt32)
) ENGINE = Log();

create table drop_ref_one_to_one (
  id                            UInt32,
  parent_id                     Nullable(UInt32)
) ENGINE = Log();

create table migtest_e_test_binary (
  id                            UInt32,
  test_byte16                   Nullable(String),
  test_byte256                  Nullable(String),
  test_byte512                  Nullable(String),
  test_byte1k                   Nullable(String),
  test_byte2k                   Nullable(String),
  test_byte4k                   Nullable(String),
  test_byte8k                   Nullable(String),
  test_byte16k                  Nullable(String),
  test_byte32k                  Nullable(String),
  test_byte64k                  Nullable(String),
  test_byte128k                 Nullable(String),
  test_byte256k                 Nullable(String),
  test_byte512k                 Nullable(String),
  test_byte1m                   Nullable(String),
  test_byte2m                   Nullable(String),
  test_byte4m                   Nullable(String),
  test_byte8m                   Nullable(String),
  test_byte16m                  Nullable(String),
  test_byte32m                  Nullable(String)
) ENGINE = Log();

create table migtest_e_test_json (
  id                            UInt32,
  json255                       Nullable(JSON),
  json256                       Nullable(JSON),
  json512                       Nullable(JSON),
  json1k                        Nullable(JSON),
  json2k                        Nullable(JSON),
  json4k                        Nullable(JSON),
  json8k                        Nullable(JSON),
  json16k                       Nullable(JSON),
  json32k                       Nullable(JSON),
  json64k                       Nullable(JSON),
  json128k                      Nullable(JSON),
  json256k                      Nullable(JSON),
  json512k                      Nullable(JSON),
  json1m                        Nullable(JSON),
  json2m                        Nullable(JSON),
  json4m                        Nullable(JSON),
  json8m                        Nullable(JSON),
  json16m                       Nullable(JSON),
  json32m                       Nullable(JSON)
) ENGINE = Log();

create table migtest_e_test_lob (
  id                            UInt32,
  lob255                        Nullable(String),
  lob256                        Nullable(String),
  lob512                        Nullable(String),
  lob1k                         Nullable(String),
  lob2k                         Nullable(String),
  lob4k                         Nullable(String),
  lob8k                         Nullable(String),
  lob16k                        Nullable(String),
  lob32k                        Nullable(String),
  lob64k                        Nullable(String),
  lob128k                       Nullable(String),
  lob256k                       Nullable(String),
  lob512k                       Nullable(String),
  lob1m                         Nullable(String),
  lob2m                         Nullable(String),
  lob4m                         Nullable(String),
  lob8m                         Nullable(String),
  lob16m                        Nullable(String),
  lob32m                        Nullable(String)
) ENGINE = Log();

create table migtest_e_test_varchar (
  id                            UInt32,
  varchar255                    Nullable(String),
  varchar256                    Nullable(String),
  varchar512                    Nullable(String),
  varchar1k                     Nullable(String),
  varchar2k                     Nullable(String),
  varchar4k                     Nullable(String),
  varchar8k                     Nullable(String),
  varchar16k                    Nullable(String),
  varchar32k                    Nullable(String),
  varchar64k                    Nullable(String),
  varchar128k                   Nullable(String),
  varchar256k                   Nullable(String),
  varchar512k                   Nullable(String),
  varchar1m                     Nullable(String),
  varchar2m                     Nullable(String),
  varchar4m                     Nullable(String),
  varchar8m                     Nullable(String),
  varchar16m                    Nullable(String),
  varchar32m                    Nullable(String)
) ENGINE = Log();

create table migtest_e_user (
  id                            UInt32
) ENGINE = Log();

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              UInt32,
  migtest_mtm_m_id              UInt64
) ENGINE = Log();

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              UInt64,
  migtest_mtm_c_id              UInt32
) ENGINE = Log();

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              UInt64,
  value                         String
) ENGINE = Log();


update migtest_e_basic set status = 'A' where status is null;

-- rename all collisions;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
-- apply alter tables
alter table "table" alter column textfield set null;
alter table "table" add column "select" String;
alter table "table" add column textfield2 String;
alter table migtest_ckey_detail add column one_key UInt32;
alter table migtest_ckey_detail add column two_key String;
alter table migtest_ckey_parent add column assoc_id UInt32;
alter table migtest_e_basic alter column status set default 'A';
alter table migtest_e_basic alter column status set not null;
alter table migtest_e_basic alter column status2 String;
alter table migtest_e_basic alter column status2 drop default;
alter table migtest_e_basic alter column status2 set null;
alter table migtest_e_basic alter column a_lob drop default;
alter table migtest_e_basic alter column a_lob set null;
alter table migtest_e_basic alter column user_id set null;
alter table migtest_e_basic add column new_string_field String default 'foo''bar';
alter table migtest_e_basic add column new_boolean_field Bool default true;
alter table migtest_e_basic add column new_boolean_field2 Bool default true;
alter table migtest_e_basic add column progress UInt32 default 0;
alter table migtest_e_basic add column new_integer UInt32 default 42;
alter table migtest_e_history alter column test_string UInt64;
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 String;
alter table migtest_e_history2 add column test_string3 String default 'unknown';
alter table migtest_e_history2 add column new_column String;
alter table migtest_e_history4 alter column test_number UInt64;
alter table migtest_e_history5 add column test_boolean Bool default false;
alter table migtest_e_history6 alter column test_number1 set default 42;
alter table migtest_e_history6 alter column test_number1 set not null;
alter table migtest_e_history6 alter column test_number2 set null;
alter table migtest_e_softdelete add column deleted Bool default false;
alter table migtest_oto_child add column master_id UInt64;
-- apply post alter
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add constraint uq_migtest_e_basic_status_indextest1 unique  (status,indextest1);
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
alter table "table" add constraint uq_table_select unique  ("select");
