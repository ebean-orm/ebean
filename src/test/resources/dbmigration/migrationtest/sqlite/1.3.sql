-- apply changes
create table migtest_e_ref (
  id                            integer not null,
  constraint pk_migtest_e_ref primary key (id)
);

alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic alter column status drop default;
alter table migtest_e_basic alter column status set null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
alter table migtest_e_basic alter column some_date drop default;
alter table migtest_e_basic alter column some_date set null;

update migtest_e_basic set user_id = 23 where user_id is null;
alter table migtest_e_basic drop constraint if exists fk_migtest_e_basic_user_id;
alter table migtest_e_basic alter column user_id set default 23;
alter table migtest_e_basic alter column user_id set not null;
alter table migtest_e_basic add column old_boolean int default 0 not null;
alter table migtest_e_basic add column old_boolean2 int default 0;
alter table migtest_e_basic add column eref_id integer;

alter table migtest_e_history2 alter column test_string drop default;
alter table migtest_e_history2 alter column test_string set null;
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
drop index if exists ix_migtest_e_basic_indextest3;
drop index if exists ix_migtest_e_basic_indextest6;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

