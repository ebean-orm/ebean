-- apply changes
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
UPDATE migtest_e_basic set status = 'A' WHERE status is null;
alter table migtest_e_basic alter column status set default A;
alter table migtest_e_basic alter column status set not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);
UPDATE migtest_e_basic set some_date = '2000-01-01T00:00:00' WHERE some_date is null;
alter table migtest_e_basic alter column some_date set default '2000-01-01T00:00:00';
alter table migtest_e_basic alter column some_date set not null;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id) on delete restrict on update restrict;
alter table migtest_e_basic alter column user_id drop not null;
alter table migtest_e_basic add column new_string_field varchar(255) not null default 'foo';
alter table migtest_e_basic add column new_boolean_field boolean not null default true;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add column new_boolean_field2 boolean not null default false;
alter table migtest_e_basic add column progress integer not null constraint ck_migtest_e_basic_progress check ( progress in ('0','1','2')) default 0;

alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint uq_migtest_e_basic_name unique  (name);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest4 unique  (indextest4);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest5 unique  (indextest5);
comment on column migtest_e_history.test_string is 'Column altered to long now';
alter table migtest_e_history alter column test_string type bigint;
comment on table migtest_e_history is 'We have history now';
create table migtest_e_user (
  id                            serial not null,
  constraint pk_migtest_e_user primary key (id)
);

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
drop index if exists ix_migtest_e_basic_indextest1;
drop index if exists ix_migtest_e_basic_indextest5;
alter table migtest_e_history add column sys_period tstzrange not null default tstzrange(current_timestamp, null);
create table migtest_e_history_history(like migtest_e_history);
create view migtest_e_history_with_history as select * from migtest_e_history union all select * from migtest_e_history_history;

create or replace function migtest_e_history_history_version() returns trigger as $$
begin
  if (TG_OP = 'UPDATE') then
    insert into migtest_e_history_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    NEW.sys_period = tstzrange(current_timestamp,null);
    return new;
  elsif (TG_OP = 'DELETE') then
    insert into migtest_e_history_history (sys_period,id, test_string) values (tstzrange(lower(OLD.sys_period), current_timestamp), OLD.id, OLD.test_string);
    return old;
  end if;
end;
$$ LANGUAGE plpgsql;

create trigger migtest_e_history_history_upd
  before update or delete on migtest_e_history
  for each row execute procedure migtest_e_history_history_version();

