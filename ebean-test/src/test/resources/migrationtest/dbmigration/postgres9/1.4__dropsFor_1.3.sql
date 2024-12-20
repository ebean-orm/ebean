-- Migrationscripts for ebean unittest
-- drop dependencies
drop trigger if exists migtest_e_history_history_upd on migtest_e_history cascade;
drop function if exists migtest_e_history_history_version();

drop view migtest_e_history_with_history;
drop table migtest_e_history_history;

-- apply changes
drop trigger if exists migtest_e_history2_history_upd on migtest_e_history2 cascade;
drop function if exists migtest_e_history2_history_version();

drop view migtest_e_history2_with_history;
drop trigger if exists migtest_e_history5_history_upd on migtest_e_history5 cascade;
drop function if exists migtest_e_history5_history_version();

drop view migtest_e_history5_with_history;
drop trigger if exists table_history_upd on "table" cascade;
drop function if exists table_history_version();

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
alter table migtest_e_history drop column sys_period;
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
create or replace function migtest_e_history2_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, obsolete_string2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string, OLD.obsolete_string2);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history2_history (sys_period,id, test_string, obsolete_string2) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_string, OLD.obsolete_string2);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history2_history_upd
  before update or delete on migtest_e_history2
  for each row execute procedure migtest_e_history2_history_version();

create view migtest_e_history5_with_history as select * from migtest_e_history5 union all select * from migtest_e_history5_history;
create or replace function migtest_e_history5_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history5_history (sys_period,id, test_number) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number);
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history5_history (sys_period,id, test_number) values (tstzrange(lowerTs,upperTs), OLD.id, OLD.test_number);
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger migtest_e_history5_history_upd
  before update or delete on migtest_e_history5
  for each row execute procedure migtest_e_history5_history_version();

create view table_with_history as select * from "table" union all select * from table_history;
create or replace function table_history_version() returns trigger as $$
-- play-ebean-start
declare
  lowerTs timestamptz;
  upperTs timestamptz;
begin
  lowerTs = lower(OLD.sys_period);
  upperTs = greatest(lowerTs + '1 microsecond',current_timestamp);
  if (TG_OP = 'UPDATE') then
    insert into table_history (sys_period,"index", "from", "to", "varchar", "select", "foreign") values (tstzrange(lowerTs,upperTs), OLD."index", OLD."from", OLD."to", OLD."varchar", OLD."select", OLD."foreign");
    NEW.sys_period = tstzrange(upperTs,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into table_history (sys_period,"index", "from", "to", "varchar", "select", "foreign") values (tstzrange(lowerTs,upperTs), OLD."index", OLD."from", OLD."to", OLD."varchar", OLD."select", OLD."foreign");
    return old;
  end if;
end;
-- play-ebean-end
$$ LANGUAGE plpgsql;

create trigger table_history_upd
  before update or delete on "table"
  for each row execute procedure table_history_version();

drop table if exists drop_main cascade;
drop sequence if exists drop_main_seq;
drop table if exists drop_main_drop_ref_many cascade;
drop table if exists drop_ref_many cascade;
drop sequence if exists drop_ref_many_seq;
drop table if exists drop_ref_one cascade;
drop sequence if exists drop_ref_one_seq;
drop table if exists drop_ref_one_to_one cascade;
drop sequence if exists drop_ref_one_to_one_seq;
drop table if exists migtest_e_test_binary cascade;
drop sequence if exists migtest_e_test_binary_seq;
drop table if exists migtest_e_test_json cascade;
drop sequence if exists migtest_e_test_json_seq;
drop table if exists migtest_e_test_lob cascade;
drop sequence if exists migtest_e_test_lob_seq;
drop table if exists migtest_e_test_varchar cascade;
drop sequence if exists migtest_e_test_varchar_seq;
drop table if exists migtest_e_user cascade;
drop sequence if exists migtest_e_user_seq;
drop table if exists migtest_mtm_c_migtest_mtm_m cascade;
drop table if exists migtest_mtm_m_migtest_mtm_c cascade;
drop table if exists migtest_mtm_m_phone_numbers cascade;
