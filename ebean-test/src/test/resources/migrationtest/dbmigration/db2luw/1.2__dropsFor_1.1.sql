-- Migrationscripts for ebean unittest
-- apply changes
alter table migtest_e_basic drop column description2;

alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

alter table migtest_e_history2 drop column obsolete_string1;

alter table migtest_e_history2 drop column obsolete_string2;

drop table migtest_e_ref;
delimiter $$
begin
if exists (select seqschema from syscat.sequences where seqschema = current_schema and seqname = 'MIGTEST_E_REF_SEQ') then
  prepare stmt from 'drop sequence migtest_e_ref_seq';
  execute stmt;
end if;
end$$;
call sysproc.admin_cmd('reorg table migtest_e_history2') /* reorg #1 */;
call sysproc.admin_cmd('reorg table migtest_e_basic') /* reorg #2 */;
