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

drop table if exists migtest_e_ref cascade;
drop sequence if exists migtest_e_ref_seq;
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

-- changes: [drop obsolete_string1, drop obsolete_string2]
create or replace function migtest_e_history2_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, test_string3) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string, OLD.test_string3);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, test_string3) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string, OLD.test_string3);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

