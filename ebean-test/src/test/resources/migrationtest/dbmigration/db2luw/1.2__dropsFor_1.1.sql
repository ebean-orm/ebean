-- Migrationscripts for ebean unittest
-- apply alter tables
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
call sysproc.admin_cmd('reorg table migtest_e_basic');
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
call sysproc.admin_cmd('reorg table migtest_e_history2');
-- apply post alter
drop table migtest_e_ref;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and seqname = 'MIGTEST_E_REF_SEQ') then
  prepare stmt from 'drop sequence migtest_e_ref_seq';
  execute stmt;
end if;
end$$;
