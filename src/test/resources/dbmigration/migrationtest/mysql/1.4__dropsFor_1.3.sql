-- Migrationscripts for ebean unittest
-- drop dependencies
drop trigger migtest_e_history_history_upd;
drop trigger migtest_e_history_history_del;
drop view migtest_e_history_with_history;
alter table migtest_e_history drop column sys_period_start;
alter table migtest_e_history drop column sys_period_end;
drop table migtest_e_history_history;


-- apply changes
alter table migtest_e_basic drop column new_string_field;

alter table migtest_e_basic drop column new_boolean_field;

alter table migtest_e_basic drop column new_boolean_field2;

alter table migtest_e_basic drop column progress;

alter table migtest_e_basic drop column new_integer;

alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2_history drop column test_string2;

alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2_history drop column test_string3;

alter table migtest_e_softdelete drop column deleted;

drop table if exists migtest_e_user;
-- changes: [drop test_string2, drop test_string3]
lock tables migtest_e_history2 write;
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string);
end$$
unlock tables;
