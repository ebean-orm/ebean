-- Migrationscripts for ebean unittest
-- apply changes
alter table migtest_e_history2 drop versioning;
-- apply alter tables
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
call sysproc.admin_cmd('reorg table migtest_e_basic');
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
call sysproc.admin_cmd('reorg table migtest_e_history2');
alter table migtest_e_history2_history drop column obsolete_string1;
alter table migtest_e_history2_history drop column obsolete_string2;
call sysproc.admin_cmd('reorg table migtest_e_history2_history');
-- apply post alter
alter table migtest_e_history2 add versioning use history table migtest_e_history2_history;
drop table drop_main;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_MAIN_SEQ') then
  prepare stmt from 'drop sequence drop_main_seq';
  execute stmt;
end if;
end$$;
drop table drop_main_drop_ref_many;
drop table drop_ref_many;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_REF_MANY_SEQ') then
  prepare stmt from 'drop sequence drop_ref_many_seq';
  execute stmt;
end if;
end$$;
drop table drop_ref_one;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'DROP_REF_ONE_SEQ') then
  prepare stmt from 'drop sequence drop_ref_one_seq';
  execute stmt;
end if;
end$$;
drop table "migtest_QuOtEd";
drop table migtest_e_ref;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and ucase(seqname) = 'MIGTEST_E_REF_SEQ') then
  prepare stmt from 'drop sequence migtest_e_ref_seq';
  execute stmt;
end if;
end$$;
