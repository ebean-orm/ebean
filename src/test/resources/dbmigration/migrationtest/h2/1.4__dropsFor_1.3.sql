-- drop dependencies
drop trigger migtest_e_history_history_upd;
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
drop sequence if exists migtest_e_user_seq;
-- changes: [drop test_string2, drop test_string3]
drop trigger migtest_e_history2_history_upd;
create trigger migtest_e_history2_history_upd before update,delete on migtest_e_history2 for each row call "io.ebean.config.dbplatform.h2.H2HistoryTrigger";
