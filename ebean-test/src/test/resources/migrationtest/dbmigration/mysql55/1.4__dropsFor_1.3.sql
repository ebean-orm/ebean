-- Migrationscripts for ebean unittest
-- drop dependencies
drop trigger migtest_e_history_history_upd;
drop trigger migtest_e_history_history_del;
drop view migtest_e_history_with_history;
drop table migtest_e_history_history;

-- apply changes
drop trigger migtest_e_history2_history_upd;
drop trigger migtest_e_history2_history_del;
drop view migtest_e_history2_with_history;
drop trigger migtest_e_history5_history_upd;
drop trigger migtest_e_history5_history_del;
drop view migtest_e_history5_with_history;
drop trigger table_history_upd;
drop trigger table_history_del;
drop view table_with_history;
-- apply alter tables
CALL usp_ebean_drop_column('table', 'textfield');
CALL usp_ebean_drop_column('table', 'textfield2');
CALL usp_ebean_drop_column('migtest_ckey_detail', 'one_key');
CALL usp_ebean_drop_column('migtest_ckey_detail', 'two_key');
CALL usp_ebean_drop_column('migtest_ckey_parent', 'assoc_id');
CALL usp_ebean_drop_column('migtest_e_basic', 'new_string_field');
CALL usp_ebean_drop_column('migtest_e_basic', 'new_boolean_field');
CALL usp_ebean_drop_column('migtest_e_basic', 'new_boolean_field2');
CALL usp_ebean_drop_column('migtest_e_basic', 'progress');
CALL usp_ebean_drop_column('migtest_e_basic', 'new_integer');
CALL usp_ebean_drop_column('migtest_e_history', 'sys_period_start');
CALL usp_ebean_drop_column('migtest_e_history', 'sys_period_end');
CALL usp_ebean_drop_column('migtest_e_history2', 'test_string2');
CALL usp_ebean_drop_column('migtest_e_history2', 'test_string3');
CALL usp_ebean_drop_column('migtest_e_history2', 'new_column');
CALL usp_ebean_drop_column('migtest_e_history2_history', 'test_string2');
CALL usp_ebean_drop_column('migtest_e_history2_history', 'test_string3');
CALL usp_ebean_drop_column('migtest_e_history2_history', 'new_column');
CALL usp_ebean_drop_column('migtest_e_history5', 'test_boolean');
CALL usp_ebean_drop_column('migtest_e_history5_history', 'test_boolean');
CALL usp_ebean_drop_column('migtest_e_softdelete', 'deleted');
CALL usp_ebean_drop_column('migtest_oto_child', 'master_id');
CALL usp_ebean_drop_column('table_history', 'textfield');
CALL usp_ebean_drop_column('table_history', 'textfield2');
-- apply post alter
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
lock tables migtest_e_history2 write;
delimiter $$
create trigger migtest_e_history2_history_upd before update on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history2_history_del before delete on migtest_e_history2 for each row begin
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_string, OLD.obsolete_string2);
end$$
unlock tables;
create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
lock tables migtest_e_history5 write;
delimiter $$
create trigger migtest_e_history5_history_upd before update on migtest_e_history5 for each row begin
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger migtest_e_history5_history_del before delete on migtest_e_history5 for each row begin
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, now(6),OLD.id, OLD.test_number);
end$$
unlock tables;
create view table_with_history as select * from `table` union all select * from table_history;
lock tables `table` write;
delimiter $$
create trigger table_history_upd before update on `table` for each row begin
    insert into table_history (sys_period_start,sys_period_end,`index`, `from`, `to`, `varchar`, `select`, `foreign`) values (OLD.sys_period_start, now(6),OLD.`index`, OLD.`from`, OLD.`to`, OLD.`varchar`, OLD.`select`, OLD.`foreign`);
    set NEW.sys_period_start = now(6);
end$$
delimiter $$
create trigger table_history_del before delete on `table` for each row begin
    insert into table_history (sys_period_start,sys_period_end,`index`, `from`, `to`, `varchar`, `select`, `foreign`) values (OLD.sys_period_start, now(6),OLD.`index`, OLD.`from`, OLD.`to`, OLD.`varchar`, OLD.`select`, OLD.`foreign`);
end$$
unlock tables;
drop table if exists drop_main;
drop table if exists drop_main_drop_ref_many;
drop table if exists drop_ref_many;
drop table if exists drop_ref_one;
drop table if exists drop_ref_one_to_one;
drop table if exists migtest_e_test_binary;
drop table if exists migtest_e_test_json;
drop table if exists migtest_e_test_lob;
drop table if exists migtest_e_test_varchar;
drop table if exists migtest_e_user;
drop table if exists migtest_mtm_c_migtest_mtm_m;
drop table if exists migtest_mtm_m_migtest_mtm_c;
drop table if exists migtest_mtm_m_phone_numbers;
