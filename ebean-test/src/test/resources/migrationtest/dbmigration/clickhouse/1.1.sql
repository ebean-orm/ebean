-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest7;
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
  parent_id                     UInt32
) ENGINE = Log();

create table drop_ref_one_to_one (
  id                            UInt32,
  parent_id                     UInt32
) ENGINE = Log();

create table migtest_e_test_binary (
  id                            UInt32,
  test_byte16                   String,
  test_byte256                  String,
  test_byte512                  String,
  test_byte1k                   String,
  test_byte2k                   String,
  test_byte4k                   String,
  test_byte8k                   String,
  test_byte16k                  String,
  test_byte32k                  String,
  test_byte64k                  String,
  test_byte128k                 String,
  test_byte256k                 String,
  test_byte512k                 String,
  test_byte1m                   String,
  test_byte2m                   String,
  test_byte4m                   String,
  test_byte8m                   String,
  test_byte16m                  String,
  test_byte32m                  String
) ENGINE = Log();

create table migtest_e_test_json (
  id                            UInt32,
  json255                       String,
  json256                       String,
  json512                       String,
  json1k                        String,
  json2k                        String,
  json4k                        String,
  json8k                        String,
  json16k                       String,
  json32k                       String,
  json64k                       String,
  json128k                      String,
  json256k                      String,
  json512k                      String,
  json1m                        String,
  json2m                        String,
  json4m                        String,
  json8m                        String,
  json16m                       String,
  json32m                       String
) ENGINE = Log();

create table migtest_e_test_lob (
  id                            UInt32,
  lob255                        String,
  lob256                        String,
  lob512                        String,
  lob1k                         String,
  lob2k                         String,
  lob4k                         String,
  lob8k                         String,
  lob16k                        String,
  lob32k                        String,
  lob64k                        String,
  lob128k                       String,
  lob256k                       String,
  lob512k                       String,
  lob1m                         String,
  lob2m                         String,
  lob4m                         String,
  lob8m                         String,
  lob16m                        String,
  lob32m                        String
) ENGINE = Log();

create table migtest_e_test_varchar (
  id                            UInt32,
  varchar255                    String,
  varchar256                    String,
  varchar512                    String,
  varchar1k                     String,
  varchar2k                     String,
  varchar4k                     String,
  varchar8k                     String,
  varchar16k                    String,
  varchar32k                    String,
  varchar64k                    String,
  varchar128k                   String,
  varchar256k                   String,
  varchar512k                   String,
  varchar1m                     String,
  varchar2m                     String,
  varchar4m                     String,
  varchar8m                     String,
  varchar16m                    String,
  varchar32m                    String
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
alter table migtest_e_basic add column new_boolean_field UInt8 default 1;
alter table migtest_e_basic add column new_boolean_field2 UInt8 default 1;
alter table migtest_e_basic add column progress UInt32 default 0;
alter table migtest_e_basic add column new_integer UInt32 default 42;
alter table migtest_e_history alter column test_string UInt64;
alter table migtest_e_history2 alter column test_string set default 'unknown';
alter table migtest_e_history2 alter column test_string set not null;
alter table migtest_e_history2 add column test_string2 String;
alter table migtest_e_history2 add column test_string3 String default 'unknown';
alter table migtest_e_history2 add column new_column String;
alter table migtest_e_history4 alter column test_number UInt64;
alter table migtest_e_history5 add column test_boolean UInt8 default 0;
alter table migtest_e_history6 alter column test_number1 set default 42;
alter table migtest_e_history6 alter column test_number1 set not null;
alter table migtest_e_history6 alter column test_number2 set null;
alter table migtest_e_softdelete add column deleted UInt8 default 0;
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
