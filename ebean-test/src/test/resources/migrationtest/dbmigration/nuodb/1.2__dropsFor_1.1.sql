-- Migrationscripts for ebean unittest
-- apply changes
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
drop view migtest_e_history2_with_history;
-- apply alter tables
alter table migtest_e_basic drop column description_file;
alter table migtest_e_basic drop column old_boolean;
alter table migtest_e_basic drop column old_boolean2;
alter table migtest_e_basic drop column eref_id;
alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2 drop column obsolete_string2;
alter table migtest_e_history2_history drop column obsolete_string1;
alter table migtest_e_history2_history drop column obsolete_string2;
-- apply post alter
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
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

drop table if exists drop_main;
drop sequence if exists drop_main_seq;
drop table if exists drop_main_drop_ref_many;
drop table if exists drop_ref_many;
drop sequence if exists drop_ref_many_seq;
drop table if exists drop_ref_one;
drop sequence if exists drop_ref_one_seq;
drop table if exists "migtest_QuOtEd";
drop table if exists migtest_e_ref;
drop sequence if exists migtest_e_ref_seq;
