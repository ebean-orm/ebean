-- Migrationscripts for ebean unittest
-- drop dependencies
IF OBJECT_ID('fk_migtest_ckey_detail_parent', 'F') IS NOT NULL alter table migtest_ckey_detail drop constraint fk_migtest_ckey_detail_parent;
IF OBJECT_ID('fk_migtest_fk_cascade_one_id', 'F') IS NOT NULL alter table migtest_fk_cascade drop constraint fk_migtest_fk_cascade_one_id;
IF OBJECT_ID('fk_migtest_fk_none_one_id', 'F') IS NOT NULL alter table migtest_fk_none drop constraint fk_migtest_fk_none_one_id;
IF OBJECT_ID('fk_migtest_fk_none_via_join_one_id', 'F') IS NOT NULL alter table migtest_fk_none_via_join drop constraint fk_migtest_fk_none_via_join_one_id;
IF OBJECT_ID('fk_migtest_fk_set_null_one_id', 'F') IS NOT NULL alter table migtest_fk_set_null drop constraint fk_migtest_fk_set_null_one_id;
IF OBJECT_ID('ck_migtest_e_basic_status', 'C') IS NOT NULL alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
IF OBJECT_ID('ck_migtest_e_basic_status2', 'C') IS NOT NULL alter table migtest_e_basic drop constraint ck_migtest_e_basic_status2;
IF OBJECT_ID('uq_migtest_e_basic_description', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_description') drop index uq_migtest_e_basic_description ON migtest_e_basic;
IF OBJECT_ID('fk_migtest_e_basic_user_id', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id;
IF OBJECT_ID('uq_migtest_e_basic_status_indextest1', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_status_indextest1') drop index uq_migtest_e_basic_status_indextest1 ON migtest_e_basic;
IF OBJECT_ID('uq_migtest_e_basic_name', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_name') drop index uq_migtest_e_basic_name ON migtest_e_basic;
IF OBJECT_ID('uq_migtest_e_basic_indextest4', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest4') drop index uq_migtest_e_basic_indextest4 ON migtest_e_basic;
IF OBJECT_ID('uq_migtest_e_basic_indextest5', 'UQ') IS NOT NULL alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest5') drop index uq_migtest_e_basic_indextest5 ON migtest_e_basic;
IF OBJECT_ID('ck_migtest_e_enum_test_status', 'C') IS NOT NULL alter table migtest_e_enum drop constraint ck_migtest_e_enum_test_status;
IF OBJECT_ID('fk_drop_main_drop_ref_many_drop_main', 'F') IS NOT NULL alter table drop_main_drop_ref_many drop constraint fk_drop_main_drop_ref_many_drop_main;
IF OBJECT_ID('fk_drop_main_drop_ref_many_drop_ref_many', 'F') IS NOT NULL alter table drop_main_drop_ref_many drop constraint fk_drop_main_drop_ref_many_drop_ref_many;
IF OBJECT_ID('fk_drop_ref_one_parent_id', 'F') IS NOT NULL alter table drop_ref_one drop constraint fk_drop_ref_one_parent_id;
IF OBJECT_ID('fk_drop_ref_one_to_one_parent_id', 'F') IS NOT NULL alter table drop_ref_one_to_one drop constraint fk_drop_ref_one_to_one_parent_id;
IF OBJECT_ID('fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c', 'F') IS NOT NULL alter table migtest_mtm_c_migtest_mtm_m drop constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_c;
IF OBJECT_ID('fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m', 'F') IS NOT NULL alter table migtest_mtm_c_migtest_mtm_m drop constraint fk_migtest_mtm_c_migtest_mtm_m_migtest_mtm_m;
IF OBJECT_ID('fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m', 'F') IS NOT NULL alter table migtest_mtm_m_migtest_mtm_c drop constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_m;
IF OBJECT_ID('fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c', 'F') IS NOT NULL alter table migtest_mtm_m_migtest_mtm_c drop constraint fk_migtest_mtm_m_migtest_mtm_c_migtest_mtm_c;
IF OBJECT_ID('fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id', 'F') IS NOT NULL alter table migtest_mtm_m_phone_numbers drop constraint fk_migtest_mtm_m_phone_numbers_migtest_mtm_m_id;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest3') drop index ix_migtest_e_basic_indextest3 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest6') drop index ix_migtest_e_basic_indextest6 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest7') drop index ix_migtest_e_basic_indextest7 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('"table"','U') AND name = 'ix_table_textfield2') drop index ix_table_textfield2 ON "table";
-- apply changes
create table [migtest_QuOtEd] (
  id                            varchar(255) not null,
  status1                       varchar(1),
  status2                       varchar(1),
  constraint ck_migtest_quoted_status1 check ( status1 in ('N','A','I')),
  constraint ck_migtest_quoted_status2 check ( status2 in ('N','A','I')),
  constraint pk_migtest_quoted primary key (id)
);

create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  name                          varchar(127) not null,
  constraint pk_migtest_e_ref primary key (id)
);


update migtest_e_basic set status2 = 'N' where status2 is null;

update migtest_e_basic set a_lob = 'X' where a_lob is null;

update migtest_e_basic set user_id = 23 where user_id is null;
-- alter table migtest_e_history2 set (system_versioning = off (history_table=dbo.migtest_e_history2_history));
-- history migration goes here
-- alter table migtest_e_history2 set (system_versioning = on (history_table=dbo.migtest_e_history2_history));
-- alter table migtest_e_history3 set (system_versioning = off (history_table=dbo.migtest_e_history3_history));
-- history migration goes here
-- alter table migtest_e_history3 set (system_versioning = on (history_table=dbo.migtest_e_history3_history));
-- alter table migtest_e_history4 set (system_versioning = off (history_table=dbo.migtest_e_history4_history));
-- history migration goes here
-- alter table migtest_e_history4 set (system_versioning = on (history_table=dbo.migtest_e_history4_history));
-- alter table migtest_e_history6 set (system_versioning = off (history_table=dbo.migtest_e_history6_history));
-- history migration goes here
-- alter table migtest_e_history6 set (system_versioning = on (history_table=dbo.migtest_e_history6_history));

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
-- apply alter tables
EXEC usp_ebean_drop_default_constraint migtest_e_basic, status;
alter table migtest_e_basic alter column status varchar(1);
EXEC usp_ebean_drop_default_constraint migtest_e_basic, status2;
alter table migtest_e_basic alter column status2 varchar(1) not null;
alter table migtest_e_basic add default 'N' for status2;
EXEC usp_ebean_drop_default_constraint migtest_e_basic, a_lob;
alter table migtest_e_basic alter column a_lob varchar(255) not null;
alter table migtest_e_basic add default 'X' for a_lob;
EXEC usp_ebean_drop_default_constraint migtest_e_basic, user_id;
alter table migtest_e_basic alter column user_id integer not null;
alter table migtest_e_basic add default 23 for user_id;
alter table migtest_e_basic add description_file image;
alter table migtest_e_basic add old_boolean bit default 0 not null;
alter table migtest_e_basic add old_boolean2 bit;
alter table migtest_e_basic add eref_id integer;
EXEC usp_ebean_drop_default_constraint migtest_e_history2, test_string;
alter table migtest_e_history2 alter column test_string varchar(255);
alter table migtest_e_history2 add obsolete_string1 varchar(255);
alter table migtest_e_history2 add obsolete_string2 varchar(255);
alter table migtest_e_history4 alter column test_number integer;
EXEC usp_ebean_drop_default_constraint migtest_e_history6, test_number1;
alter table migtest_e_history6 alter column test_number1 integer;
EXEC usp_ebean_drop_default_constraint migtest_e_history6, test_number2;
alter table migtest_e_history6 alter column test_number2 integer not null;
alter table migtest_e_history6 add default 7 for test_number2;
-- apply post alter
create unique nonclustered index uq_migtest_quoted_status2 on "migtest_QuOtEd"(status2) where status2 is not null;
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic add constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I'));
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest7 unique  (indextest7);
alter table migtest_e_enum add constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I'));
-- foreign keys and indices
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on [migtest_QuOtEd] (status1);
