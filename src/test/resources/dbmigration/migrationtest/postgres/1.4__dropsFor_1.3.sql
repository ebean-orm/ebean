-- Migrationscripts for ebean unittest
-- drop dependencies
drop trigger if exists migtest_e_history_history_upd on migtest_e_history cascade;
drop function if exists migtest_e_history_history_version();

drop view migtest_e_history_with_history;
alter table migtest_e_history drop column sys_period;
drop table migtest_e_history_history;

drop view if exists migtest_e_history2_with_history;
drop view if exists migtest_e_history5_with_history;

-- apply changes
alter table migtest_ckey_detail drop column one_key;

alter table migtest_ckey_detail drop column two_key;

alter table migtest_ckey_parent drop column assoc_id;

alter table migtest_e_basic drop column new_string_field;

alter table migtest_e_basic drop column new_boolean_field;

alter table migtest_e_basic drop column new_boolean_field2;

alter table migtest_e_basic drop column progress;

alter table migtest_e_basic drop column new_integer;

alter table migtest_e_history2 drop column test_string2;
alter table migtest_e_history2_history drop column test_string2;

alter table migtest_e_history2 drop column test_string3;
alter table migtest_e_history2_history drop column test_string3;

alter table migtest_e_history5 drop column test_boolean;
alter table migtest_e_history5_history drop column test_boolean;

alter table migtest_e_softdelete drop column deleted;

alter table migtest_oto_child drop column master_id;

drop table if exists migtest_e_user cascade;
drop sequence if exists migtest_e_user_seq;
drop table if exists migtest_mtm_c_migtest_mtm_m cascade;
drop table if exists migtest_mtm_m_migtest_mtm_c cascade;
-- changes: [drop test_string2, drop test_string3]
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;

create or replace function migtest_e_history2_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, obsolete_string2) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string, OLD.obsolete_string2);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, obsolete_string2) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string, OLD.obsolete_string2);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

-- changes: [drop test_boolean]
create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;

create or replace function migtest_e_history5_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history5_history (sys_period,id, test_number) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_number);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history5_history (sys_period,id, test_number) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_number);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

