-- Migrationscripts for ebean unittest
-- drop dependencies
drop view if exists migtest_e_history2_with_history;
-- apply changes
drop sequence if exists migtest_e_ref_seq;
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
alter table migtest_e_history2_history drop column obsolete_string1;
alter table migtest_e_history2_history drop column obsolete_string2;
drop table if exists migtest_e_ref;
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

-- changes: [drop obsolete_string1, drop obsolete_string2]
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
delimiter $$
create or replace trigger migtest_e_history2_history_upd for migtest_e_history2 before update for each row as 
    NEW.sys_period_start = greatest(current_timestamp, date_add(OLD.sys_period_start, interval 1 microsecond));
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string3, new_column) values (OLD.sys_period_start, NEW.sys_period_start,OLD.id, OLD.test_string, OLD.test_string3, OLD.new_column);
end_trigger;
$$

delimiter $$
create or replace trigger migtest_e_history2_history_del for migtest_e_history2 before delete for each row as
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string3, new_column) values (OLD.sys_period_start, NEW.sys_period_start,OLD.id, OLD.test_string, OLD.test_string3, OLD.new_column);
end_trigger;
$$

