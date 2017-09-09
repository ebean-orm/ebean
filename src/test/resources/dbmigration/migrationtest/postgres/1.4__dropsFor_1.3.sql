-- drop dependencies
drop trigger if exists migtest_e_history_history_upd on migtest_e_history cascade;
drop function if exists migtest_e_history_history_version();

drop view migtest_e_history_with_history;
alter table migtest_e_history drop column sys_period;
drop table migtest_e_history_history;

drop view if exists migtest_e_history2_with_history;

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

drop table if exists migtest_e_user cascade;
drop sequence if exists migtest_e_user_seq;
-- changes: [drop test_string2, drop test_string3]
create or replace view migtest_e_history2_with_history as select id, test_string, sys_period from migtest_e_history2 union all select id, test_string, sys_period from migtest_e_history2_history;

create or replace function migtest_e_history2_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

