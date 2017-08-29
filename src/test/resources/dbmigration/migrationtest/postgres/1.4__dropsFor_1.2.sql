-- drop dependencies
drop trigger if exists migtest_e_history_history_upd on migtest_e_history cascade;
drop function if exists migtest_e_history_history_version();

drop view migtest_e_history_with_history;
alter table migtest_e_history drop column sys_period;
drop table migtest_e_history_history;


-- apply changes
alter table migtest_e_basic drop column new_string_field;

alter table migtest_e_basic drop column new_boolean_field;

alter table migtest_e_basic drop column new_boolean_field2;

alter table migtest_e_basic drop column progress;

drop table if exists migtest_e_user cascade;
