-- Migrationscripts for ebean unittest
-- apply changes
create table migtest_e_ref (
  id                            integer not null,
  name                          nvarchar(127) not null,
  constraint pk_migtest_e_ref primary key (id)
);
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);
create sequence migtest_e_ref_seq as bigint  start with 1 ;

IF OBJECT_ID('fk_migtest_ckey_detail_parent', 'F') IS NOT NULL alter table migtest_ckey_detail drop constraint fk_migtest_ckey_detail_parent;
IF OBJECT_ID('fk_migtest_fk_cascade_one_id', 'F') IS NOT NULL alter table migtest_fk_cascade drop constraint fk_migtest_fk_cascade_one_id;
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update cascade;
IF OBJECT_ID('fk_migtest_fk_none_one_id', 'F') IS NOT NULL alter table migtest_fk_none drop constraint fk_migtest_fk_none_one_id;
IF OBJECT_ID('fk_migtest_fk_none_via_join_one_id', 'F') IS NOT NULL alter table migtest_fk_none_via_join drop constraint fk_migtest_fk_none_via_join_one_id;
IF OBJECT_ID('fk_migtest_fk_set_null_one_id', 'F') IS NOT NULL alter table migtest_fk_set_null drop constraint fk_migtest_fk_set_null_one_id;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update set null;
IF (OBJECT_ID('ck_migtest_e_basic_status', 'C') IS NOT NULL) alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
EXEC usp_ebean_drop_default_constraint migtest_e_basic, status;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
IF (OBJECT_ID('uq_migtest_e_basic_description', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_description') drop index uq_migtest_e_basic_description ON migtest_e_basic;

update migtest_e_basic set user_id = 23 where user_id is null;
IF OBJECT_ID('fk_migtest_e_basic_user_id', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id;
alter table migtest_e_basic add default 23 for user_id;
alter table migtest_e_basic alter column user_id integer not null;
alter table migtest_e_basic add old_boolean bit default 0 not null;
alter table migtest_e_basic add old_boolean2 bit;
alter table migtest_e_basic add eref_id integer;

IF (OBJECT_ID('uq_migtest_e_basic_status_indextest1', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_status_indextest1') drop index uq_migtest_e_basic_status_indextest1 ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_name', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_name') drop index uq_migtest_e_basic_name ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_indextest4', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest4') drop index uq_migtest_e_basic_indextest4 ON migtest_e_basic;
IF (OBJECT_ID('uq_migtest_e_basic_indextest5', 'UQ') IS NOT NULL) alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'uq_migtest_e_basic_indextest5') drop index uq_migtest_e_basic_indextest5 ON migtest_e_basic;
create unique nonclustered index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) where indextest2 is not null;
create unique nonclustered index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) where indextest6 is not null;
IF (OBJECT_ID('ck_migtest_e_enum_test_status', 'C') IS NOT NULL) alter table migtest_e_enum drop constraint ck_migtest_e_enum_test_status;
alter table migtest_e_enum add constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I'));
EXEC usp_ebean_drop_default_constraint migtest_e_history2, test_string;
alter table migtest_e_history2 add obsolete_string1 nvarchar(255);
alter table migtest_e_history2 add obsolete_string2 nvarchar(255);

alter table migtest_e_history4 alter column test_number integer;
EXEC usp_ebean_drop_default_constraint migtest_e_history6, test_number1;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
alter table migtest_e_history6 add default 7 for test_number2;
alter table migtest_e_history6 alter column test_number2 integer not null;
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest3') drop index ix_migtest_e_basic_indextest3 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest6') drop index ix_migtest_e_basic_indextest6 ON migtest_e_basic;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);

-- alter table migtest_e_history3 set (system_versioning = off (history_table=dbo.migtest_e_history3_history));
-- history migration goes here
-- alter table migtest_e_history3 set (system_versioning = on (history_table=dbo.migtest_e_history3_history));
