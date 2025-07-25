-- Migrationscripts for ebean unittest
-- drop dependencies
IF OBJECT_ID('fk_migtest_fk_cascade_one_id', 'F') IS NOT NULL alter table migtest_fk_cascade drop constraint fk_migtest_fk_cascade_one_id;
IF OBJECT_ID('fk_migtest_fk_set_null_one_id', 'F') IS NOT NULL alter table migtest_fk_set_null drop constraint fk_migtest_fk_set_null_one_id;
IF OBJECT_ID('ck_migtest_e_basic_status', 'C') IS NOT NULL alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
IF OBJECT_ID('ck_migtest_e_basic_status2', 'C') IS NOT NULL alter table migtest_e_basic drop constraint ck_migtest_e_basic_status2;
IF OBJECT_ID('uq_migtest_e_basic_indextest2', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest2') drop index uq_migtest_e_basic_indextest2 ON migtest_e_basic;
IF OBJECT_ID('uq_migtest_e_basic_indextest6', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest6') drop index uq_migtest_e_basic_indextest6 ON migtest_e_basic;
IF OBJECT_ID('uq_migtest_e_basic_indextest7', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest7;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest7') drop index uq_migtest_e_basic_indextest7 ON migtest_e_basic;
IF OBJECT_ID('ck_migtest_e_enum_test_status', 'C') IS NOT NULL alter table migtest_e_enum drop constraint ck_migtest_e_enum_test_status;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest1') drop index ix_migtest_e_basic_indextest1 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest5') drop index ix_migtest_e_basic_indextest5 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('"migtest_QuOtEd"','U') AND name = 'ix_migtest_quoted_status1') drop index ix_migtest_quoted_status1 ON "migtest_QuOtEd";
-- apply changes
create table drop_main (
  id                            integer identity(1,1) not null,
  constraint pk_drop_main primary key (id)
);

create table drop_main_drop_ref_many (
  drop_main_id                  integer not null,
  drop_ref_many_id              integer not null,
  constraint pk_drop_main_drop_ref_many primary key (drop_main_id,drop_ref_many_id)
);

create table drop_ref_many (
  id                            integer identity(1,1) not null,
  constraint pk_drop_ref_many primary key (id)
);

create table drop_ref_one (
  id                            integer identity(1,1) not null,
  parent_id                     integer,
  constraint pk_drop_ref_one primary key (id)
);

create table drop_ref_one_to_one (
  id                            integer identity(1,1) not null,
  parent_id                     integer,
  constraint pk_drop_ref_one_to_one primary key (id)
);

create table migtest_e_test_binary (
  id                            integer identity(1,1) not null,
  test_byte16                   image,
  test_byte256                  image,
  test_byte512                  image,
  test_byte1k                   image,
  test_byte2k                   image,
  test_byte4k                   image,
  test_byte8k                   image,
  test_byte16k                  image,
  test_byte32k                  image,
  test_byte64k                  image,
  test_byte128k                 image,
  test_byte256k                 image,
  test_byte512k                 image,
  test_byte1m                   image,
  test_byte2m                   image,
  test_byte4m                   image,
  test_byte8m                   image,
  test_byte16m                  image,
  test_byte32m                  image,
  constraint pk_migtest_e_test_binary primary key (id)
);

create table migtest_e_test_json (
  id                            integer identity(1,1) not null,
  json255                       text,
  json256                       text,
  json512                       text,
  json1k                        text,
  json2k                        text,
  json4k                        text,
  json8k                        text,
  json16k                       text,
  json32k                       text,
  json64k                       text,
  json128k                      text,
  json256k                      text,
  json512k                      text,
  json1m                        text,
  json2m                        text,
  json4m                        text,
  json8m                        text,
  json16m                       text,
  json32m                       text,
  constraint pk_migtest_e_test_json primary key (id)
);

create table migtest_e_test_lob (
  id                            integer identity(1,1) not null,
  lob255                        text,
  lob256                        text,
  lob512                        text,
  lob1k                         text,
  lob2k                         text,
  lob4k                         text,
  lob8k                         text,
  lob16k                        text,
  lob32k                        text,
  lob64k                        text,
  lob128k                       text,
  lob256k                       text,
  lob512k                       text,
  lob1m                         text,
  lob2m                         text,
  lob4m                         text,
  lob8m                         text,
  lob16m                        text,
  lob32m                        text,
  constraint pk_migtest_e_test_lob primary key (id)
);

create table migtest_e_test_varchar (
  id                            integer identity(1,1) not null,
  varchar255                    varchar(255),
  varchar256                    varchar(256),
  varchar512                    varchar(512),
  varchar1k                     varchar(1024),
  varchar2k                     varchar(2048),
  varchar4k                     varchar(4096),
  varchar8k                     text,
  varchar16k                    text,
  varchar32k                    text,
  varchar64k                    text,
  varchar128k                   text,
  varchar256k                   text,
  varchar512k                   text,
  varchar1m                     text,
  varchar2m                     text,
  varchar4m                     text,
  varchar8m                     text,
  varchar16m                    text,
  varchar32m                    text,
  constraint pk_migtest_e_test_varchar primary key (id)
);

create table migtest_e_user (
  id                            integer identity(1,1) not null,
  constraint pk_migtest_e_user primary key (id)
);

create table migtest_mtm_c_migtest_mtm_m (
  migtest_mtm_c_id              integer not null,
  migtest_mtm_m_id              bigint not null,
  constraint pk_migtest_mtm_c_migtest_mtm_m primary key (migtest_mtm_c_id,migtest_mtm_m_id)
);

create table migtest_mtm_m_migtest_mtm_c (
  migtest_mtm_m_id              bigint not null,
  migtest_mtm_c_id              integer not null,
  constraint pk_migtest_mtm_m_migtest_mtm_c primary key (migtest_mtm_m_id,migtest_mtm_c_id)
);

create table migtest_mtm_m_phone_numbers (
  migtest_mtm_m_id              bigint not null,
  value                         varchar(255) not null
);


update migtest_e_basic set status = 'A' where status is null;

-- rename all collisions;

--;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history2 set test_string = 'unknown' where test_string is null;
-- alter table migtest_e_history2 set (system_versioning = off (history_table=dbo.migtest_e_history2_history));
-- history migration goes here
-- alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
-- alter table migtest_e_history3 set (system_versioning = off (history_table=dbo.migtest_e_history3_history));
-- history migration goes here
-- alter table migtest_e_history3 set (system_versioning = on (history_table=dbo.migtest_e_history3_history));
-- alter table migtest_e_history4 set (system_versioning = off (history_table=dbo.migtest_e_history4_history));
-- history migration goes here
-- alter table migtest_e_history4 set (system_versioning = on (history_table=dbo.migtest_e_history4_history));
-- alter table migtest_e_history5 set (system_versioning = off (history_table=dbo.migtest_e_history5_history));
-- history migration goes here
-- alter table migtest_e_history5 set (system_versioning = on (history_table=dbo.migtest_e_history5_history));

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number1 = 42 where test_number1 is null;
-- alter table migtest_e_history6 set (system_versioning = off (history_table=dbo.migtest_e_history6_history));
-- history migration goes here
-- alter table migtest_e_history6 set (system_versioning = on (history_table=dbo.migtest_e_history6_history));
-- alter table [table] set (system_versioning = off (history_table=dbo.table_history));
-- history migration goes here
-- alter table [table] set (system_versioning = on (history_table=dbo.table_history));
-- apply alter tables
alter table [table] alter column textfield varchar(255);
alter table [table] add [select] varchar(255);
alter table [table] add textfield2 varchar(255);
alter table migtest_ckey_detail add one_key integer;
alter table migtest_ckey_detail add two_key varchar(127);
alter table migtest_ckey_parent add assoc_id integer;
EXEC usp_ebean_drop_default_constraint migtest_e_basic, status;
alter table migtest_e_basic alter column status varchar(1) not null;
alter table migtest_e_basic add default 'A' for status;
EXEC usp_ebean_drop_default_constraint migtest_e_basic, status2;
alter table migtest_e_basic alter column status2 varchar(127);
EXEC usp_ebean_drop_default_constraint migtest_e_basic, a_lob;
alter table migtest_e_basic alter column a_lob varchar(255);
alter table migtest_e_basic alter column default_test integer not null;
alter table migtest_e_basic alter column user_id integer;
alter table migtest_e_basic add new_string_field varchar(255) default 'foo''bar' not null;
alter table migtest_e_basic add new_boolean_field bit default 1 not null;
alter table migtest_e_basic add new_boolean_field2 bit default 1 not null;
alter table migtest_e_basic add progress integer default 0 not null;
alter table migtest_e_basic add new_integer integer default 42 not null;
alter table migtest_e_history alter column test_string bigint;
EXEC usp_ebean_drop_default_constraint migtest_e_history2, test_string;
alter table migtest_e_history2 alter column test_string varchar(255) not null;
alter table migtest_e_history2 add default 'unknown' for test_string;
alter table migtest_e_history2 add test_string2 varchar(255);
alter table migtest_e_history2 add test_string3 varchar(255) default 'unknown' not null;
alter table migtest_e_history2 add new_column varchar(20);
alter table migtest_e_history4 alter column test_number bigint;
alter table migtest_e_history5 add test_boolean bit default 0 not null;
EXEC usp_ebean_drop_default_constraint migtest_e_history6, test_number1;
alter table migtest_e_history6 alter column test_number1 integer not null;
alter table migtest_e_history6 add default 42 for test_number1;
alter table migtest_e_history6 alter column test_number2 integer;
alter table migtest_e_softdelete add deleted bit default 0 not null;
alter table migtest_oto_child add master_id bigint;
-- apply post alter
create unique nonclustered index uq_drop_ref_one_to_one_parent_id on drop_ref_one_to_one(parent_id) where parent_id is not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
create unique nonclustered index uq_migtest_e_basic_description on migtest_e_basic(description) where description is not null;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
create unique nonclustered index uq_migtest_e_basic_status_indextest1 on migtest_e_basic(status,indextest1) where indextest1 is not null;
create unique nonclustered index uq_migtest_e_basic_name on migtest_e_basic(name) where name is not null;
create unique nonclustered index uq_migtest_e_basic_indextest4 on migtest_e_basic(indextest4) where indextest4 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest5 on migtest_e_basic(indextest5) where indextest5 is not null;
alter table migtest_e_history
    add sys_periodFrom datetime2 GENERATED ALWAYS AS ROW START NOT NULL DEFAULT SYSUTCDATETIME(),
        sys_periodTo   datetime2 GENERATED ALWAYS AS ROW END   NOT NULL DEFAULT '9999-12-31T23:59:59.9999999',
period for system_time (sys_periodFrom, sys_periodTo);
alter table migtest_e_history set (system_versioning = on (history_table=dbo.migtest_e_history_history));
create unique nonclustered index uq_table_select on "table"("select") where "select" is not null;
-- foreign keys and indices
create index ix_drop_main_drop_ref_many_drop_main on drop_main_drop_ref_many (drop_main_id);
alter table drop_main_drop_ref_many add constraint fk_drop_main_drop_ref_many_drop_main foreign key (drop_main_id) references drop_main (id);

create index ix_drop_main_drop_ref_many_drop_ref_many on drop_main_drop_ref_many (drop_ref_many_id);
alter table drop_main_drop_ref_many add constraint fk_drop_main_drop_ref_many_drop_ref_many foreign key (drop_ref_many_id) references drop_ref_many (id);

create index ix_drop_ref_one_parent_id on drop_ref_one (parent_id);
alter table drop_ref_one add constraint fk_drop_ref_one_parent_id foreign key (parent_id) references drop_main (id);

alter table drop_ref_one_to_one add constraint fk_drop_ref_one_to_one_parent_id foreign key (parent_id) references drop_main (id);

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c on migtest_mtm_c_migtest_mtm_m (migtest_mtm_c_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id);

create index ix_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m on migtest_mtm_c_migtest_mtm_m (migtest_mtm_m_id);
alter table migtest_mtm_c_migtest_mtm_m add constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m on migtest_mtm_m_migtest_mtm_c (migtest_mtm_m_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

create index ix_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c on migtest_mtm_m_migtest_mtm_c (migtest_mtm_c_id);
alter table migtest_mtm_m_migtest_mtm_c add constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c foreign key (migtest_mtm_c_id) references migtest_mtm_c (id);

create index ix_migtest_mtm_m_phone_numbers_migtest_mtm_m_id on migtest_mtm_m_phone_numbers (migtest_mtm_m_id);
alter table migtest_mtm_m_phone_numbers add constraint fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id foreign key (migtest_mtm_m_id) references migtest_mtm_m (id);

alter table migtest_ckey_detail add constraint fk_migtest_ckey_detail_parent foreign key (one_key,two_key) references migtest_ckey_parent (one_key,two_key);
create index ix_migtest_ckey_parent_assoc_id on migtest_ckey_parent (assoc_id);
alter table migtest_ckey_parent add constraint fk_migtest_ckey_parent_assoc_id foreign key (assoc_id) references migtest_ckey_assoc (id);

alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id);
alter table migtest_fk_none add constraint fk_migtest_fk_none_one_id foreign key (one_id) references migtest_fk_one (id);
alter table migtest_fk_none_via_join add constraint fk_migtest_fk_none_via_join_one_id foreign key (one_id) references migtest_fk_one (id);
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id);
alter table migtest_oto_child add constraint fk_migtest_oto_child_master_id foreign key (master_id) references migtest_oto_master (id);

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
create index ix_migtest_e_basic_indextest7 on migtest_e_basic (indextest7);
create index ix_table_textfield2 on [table] (textfield2);
