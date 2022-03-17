-- Migrationscripts for ebean unittest
-- drop dependencies
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'FK_MIGTEST_CKEY_DETAIL_PARENT' and tabname = 'MIGTEST_CKEY_DETAIL') then
  prepare stmt from 'alter table migtest_ckey_detail drop constraint fk_migtest_ckey_detail_parent';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'FK_MIGTEST_FK_CASCADE_ONE_ID' and tabname = 'MIGTEST_FK_CASCADE') then
  prepare stmt from 'alter table migtest_fk_cascade drop constraint fk_migtest_fk_cascade_one_id';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'FK_MIGTEST_FK_NONE_ONE_ID' and tabname = 'MIGTEST_FK_NONE') then
  prepare stmt from 'alter table migtest_fk_none drop constraint fk_migtest_fk_none_one_id';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'FK_MIGTEST_FK_NONE_VIA_JOIN_ONE_ID' and tabname = 'MIGTEST_FK_NONE_VIA_JOIN') then
  prepare stmt from 'alter table migtest_fk_none_via_join drop constraint fk_migtest_fk_none_via_join_one_id';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'FK_MIGTEST_FK_SET_NULL_ONE_ID' and tabname = 'MIGTEST_FK_SET_NULL') then
  prepare stmt from 'alter table migtest_fk_set_null drop constraint fk_migtest_fk_set_null_one_id';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'CK_MIGTEST_E_BASIC_STATUS' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint ck_migtest_e_basic_status';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'CK_MIGTEST_E_BASIC_STATUS2' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint ck_migtest_e_basic_status2';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'UQ_MIGTEST_E_BASIC_DESCRIPTION' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_description';
  execute stmt;
end if;
end$$
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'UQ_MIGTEST_E_BASIC_DESCRIPTION') then
  prepare stmt from 'drop index uq_migtest_e_basic_description';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'FK_MIGTEST_E_BASIC_USER_ID' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'UQ_MIGTEST_E_BASIC_STATUS_INDEXTEST1' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1';
  execute stmt;
end if;
end$$
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'UQ_MIGTEST_E_BASIC_STATUS_INDEXTEST1') then
  prepare stmt from 'drop index uq_migtest_e_basic_status_indextest1';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'UQ_MIGTEST_E_BASIC_NAME' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_name';
  execute stmt;
end if;
end$$
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'UQ_MIGTEST_E_BASIC_NAME') then
  prepare stmt from 'drop index uq_migtest_e_basic_name';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'UQ_MIGTEST_E_BASIC_INDEXTEST4' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4';
  execute stmt;
end if;
end$$
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'UQ_MIGTEST_E_BASIC_INDEXTEST4') then
  prepare stmt from 'drop index uq_migtest_e_basic_indextest4';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'UQ_MIGTEST_E_BASIC_INDEXTEST5' and tabname = 'MIGTEST_E_BASIC') then
  prepare stmt from 'alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5';
  execute stmt;
end if;
end$$
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'UQ_MIGTEST_E_BASIC_INDEXTEST5') then
  prepare stmt from 'drop index uq_migtest_e_basic_indextest5';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select constname from syscat.tabconst where tabschema = current_schema and constname = 'CK_MIGTEST_E_ENUM_TEST_STATUS' and tabname = 'MIGTEST_E_ENUM') then
  prepare stmt from 'alter table migtest_e_enum drop constraint ck_migtest_e_enum_test_status';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'IX_MIGTEST_E_BASIC_INDEXTEST3') then
  prepare stmt from 'drop index ix_migtest_e_basic_indextest3';
  execute stmt;
end if;
end$$;
delimiter $$
begin
if exists (select indname from syscat.indexes where indschema = current_schema and indname = 'IX_MIGTEST_E_BASIC_INDEXTEST6') then
  prepare stmt from 'drop index ix_migtest_e_basic_indextest6';
  execute stmt;
end if;
end$$;
-- apply changes
create table migtest_e_ref (
  id                            integer generated by default as identity not null,
  name                          varchar(127) not null,
  constraint pk_migtest_e_ref primary key (id)
);


update migtest_e_basic set status2 = 'N' where status2 is null;

update migtest_e_basic set a_lob = 'X' where a_lob is null;

update migtest_e_basic set user_id = 23 where user_id is null;
alter table migtest_e_history2 drop versioning;
alter table migtest_e_history3 drop versioning;
alter table migtest_e_history4 drop versioning;
alter table migtest_e_history6 drop versioning;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
CALL SYSPROC.ADMIN_MOVE_TABLE(CURRENT_SCHEMA,'MIGTEST_MTM_C','USERSPACE1','USERSPACE1','USERSPACE1','','','','','','MOVE');
CALL SYSPROC.ADMIN_MOVE_TABLE(CURRENT_SCHEMA,'MIGTEST_MTM_M','USERSPACE1','USERSPACE1','USERSPACE1','','','','','','MOVE');
-- apply alter tables
alter table migtest_e_basic alter column status drop default;
alter table migtest_e_basic alter column status drop not null;
alter table migtest_e_basic alter column status2 set data type varchar(1);
alter table migtest_e_basic alter column status2 set default 'N';
alter table migtest_e_basic alter column status2 set not null;
alter table migtest_e_basic alter column a_lob set data type clob(16K);
alter table migtest_e_basic alter column a_lob set inline length 500;
-- ignored options for migtest_e_basic.a_lob: compact=true, logged=true;
alter table migtest_e_basic alter column a_lob set default 'X';
alter table migtest_e_basic alter column a_lob set not null;
alter table migtest_e_basic alter column user_id set default 23;
alter table migtest_e_basic alter column user_id set not null;
alter table migtest_e_basic add column description_file blob(64M);
alter table migtest_e_basic add column old_boolean smallint default 0 default false not null;
alter table migtest_e_basic add column old_boolean2 smallint default 0;
alter table migtest_e_basic add column eref_id integer;
call sysproc.admin_cmd('reorg table migtest_e_basic');
alter table migtest_e_history2 alter column test_string drop default;
alter table migtest_e_history2 alter column test_string drop not null;
alter table migtest_e_history2 add column obsolete_string1 varchar(255);
alter table migtest_e_history2 add column obsolete_string2 varchar(255);
alter table migtest_e_history2_history alter column test_string drop not null;
alter table migtest_e_history2_history add column obsolete_string1 varchar(255);
alter table migtest_e_history2_history add column obsolete_string2 varchar(255);
alter table migtest_e_history4 alter column test_number set data type integer;
call sysproc.admin_cmd('reorg table migtest_e_history4');
alter table migtest_e_history4_history alter column test_number set data type integer;
call sysproc.admin_cmd('reorg table migtest_e_history4_history');
alter table migtest_e_history6 alter column test_number1 drop default;
alter table migtest_e_history6 alter column test_number1 drop not null;
alter table migtest_e_history6 alter column test_number2 set default 7;
alter table migtest_e_history6 alter column test_number2 set not null;
call sysproc.admin_cmd('reorg table migtest_e_history6');
alter table migtest_e_history6_history alter column test_number1 drop not null;
alter table migtest_e_history6_history alter column test_number2 set not null;
call sysproc.admin_cmd('reorg table migtest_e_history6_history');
-- apply post alter
alter table migtest_e_ref add constraint uq_migtest_e_ref_name unique  (name);
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic add constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I'));
create unique index uq_migtest_e_basic_indextest2 on migtest_e_basic(indextest2) exclude null keys;
create unique index uq_migtest_e_basic_indextest6 on migtest_e_basic(indextest6) exclude null keys;
alter table migtest_e_enum add constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I'));
comment on column migtest_e_history.test_string is '';
comment on table migtest_e_history is '';
alter table migtest_e_history2 add versioning use history table migtest_e_history2_history;
alter table migtest_e_history3 add versioning use history table migtest_e_history3_history;
alter table migtest_e_history4 add versioning use history table migtest_e_history4_history;
alter table migtest_e_history6 add versioning use history table migtest_e_history6_history;
-- foreign keys and indices
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
