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
alter table "table" drop column textfield;
alter table "table" drop column textfield2;
alter table migtest_ckey_detail drop column one_key;
alter table migtest_ckey_detail drop column two_key;
alter table migtest_ckey_parent drop column assoc_id;
alter table migtest_e_basic drop column new_string_field;
alter table migtest_e_basic drop column new_boolean_field;
alter table migtest_e_basic drop column new_boolean_field2;
alter table migtest_e_basic drop column progress;
alter table migtest_e_basic drop column new_integer;
alter table migtest_e_history drop column sys_period_start;
alter table migtest_e_history drop column sys_period_end;
alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2 drop column new_column;
alter table migtest_e_history2_history drop column test_string2;
alter table migtest_e_history2_history drop column test_string3;
alter table migtest_e_history2_history drop column new_column;
alter table migtest_e_history5 drop column test_boolean;
alter table migtest_e_history5_history drop column test_boolean;
alter table migtest_e_softdelete drop column deleted;
alter table migtest_oto_child drop column master_id;
alter table table_history drop column textfield;
alter table table_history drop column textfield2;
-- apply post alter
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
delimiter $$
create or replace trigger migtest_e_history2_history_upd for migtest_e_history2 before update for each row as 
    NEW.sys_period_start = greatest(current_timestamp, date_add(OLD.sys_period_start, interval 1 microsecond));
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2) values (OLD.sys_period_start, NEW.sys_period_start,OLD.id, OLD.test_string, OLD.obsolete_string2);
end_trigger;
$$

delimiter $$
create or replace trigger migtest_e_history2_history_del for migtest_e_history2 before delete for each row as
    insert into migtest_e_history2_history (sys_period_start,sys_period_end,id, test_string, obsolete_string2) values (OLD.sys_period_start, NEW.sys_period_start,OLD.id, OLD.test_string, OLD.obsolete_string2);
end_trigger;
$$

create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
delimiter $$
create or replace trigger migtest_e_history5_history_upd for migtest_e_history5 before update for each row as 
    NEW.sys_period_start = greatest(current_timestamp, date_add(OLD.sys_period_start, interval 1 microsecond));
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, NEW.sys_period_start,OLD.id, OLD.test_number);
end_trigger;
$$

delimiter $$
create or replace trigger migtest_e_history5_history_del for migtest_e_history5 before delete for each row as
    insert into migtest_e_history5_history (sys_period_start,sys_period_end,id, test_number) values (OLD.sys_period_start, NEW.sys_period_start,OLD.id, OLD.test_number);
end_trigger;
$$

create view table_with_history as select * from "table" union all select * from table_history;
delimiter $$
create or replace trigger table_history_upd for "table" before update for each row as 
    NEW.sys_period_start = greatest(current_timestamp, date_add(OLD.sys_period_start, interval 1 microsecond));
    insert into table_history (sys_period_start,sys_period_end,"index", "from", "to", "varchar", "select", "foreign") values (OLD.sys_period_start, NEW.sys_period_start,OLD."index", OLD."from", OLD."to", OLD."varchar", OLD."select", OLD."foreign");
end_trigger;
$$

delimiter $$
create or replace trigger table_history_del for "table" before delete for each row as
    insert into table_history (sys_period_start,sys_period_end,"index", "from", "to", "varchar", "select", "foreign") values (OLD.sys_period_start, NEW.sys_period_start,OLD."index", OLD."from", OLD."to", OLD."varchar", OLD."select", OLD."foreign");
end_trigger;
$$

drop table if exists migtest_e_user;
drop sequence if exists migtest_e_user_seq;
drop table if exists migtest_mtm_c_migtest_mtm_m;
drop table if exists migtest_mtm_m_migtest_mtm_c;
drop table if exists migtest_mtm_m_phone_numbers;
