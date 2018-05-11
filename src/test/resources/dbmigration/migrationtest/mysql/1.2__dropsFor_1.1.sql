-- Migrationscripts for ebean unittest
-- drop dependencies
drop view if exists migtest_e_history2_with_history;

-- apply changes
alter table migtest_e_basic drop column old_boolean;

alter table migtest_e_basic drop column old_boolean2;

alter table migtest_e_basic drop column eref_id;

alter table migtest_e_history2 drop column obsolete_string1;
alter table migtest_e_history2_history drop column obsolete_string1;

alter table migtest_e_history2 drop column obsolete_string2;
alter table migtest_e_history2_history drop column obsolete_string2;

drop table if exists migtest_e_ref;
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

lock tables migtest_e_history2 write;
-- changes: [drop obsolete_string1, drop obsolete_string2]
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string3) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.test_string3);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, test_string3) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.test_string3);
end$$
unlock tables;
