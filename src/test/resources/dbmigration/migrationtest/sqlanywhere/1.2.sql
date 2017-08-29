-- apply changes
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic alter column status drop default;
alter table migtest_e_basic alter column status set null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
alter table migtest_e_basic alter column some_date drop default;
alter table migtest_e_basic alter column some_date set null;
alter table migtest_e_basic drop constraint if exists fk_migtest_e_basic_user_id;
UPDATE migtest_e_basic set user_id = ${default} WHERE user_id is null;
alter table migtest_e_basic alter column user_id set not null;
alter table migtest_e_basic add column old_boolean bit not null default false;
alter table migtest_e_basic add column old_boolean2 bit;
alter table migtest_e_basic add column eref_id integer;

alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
comment on column migtest_e_history.test_string is 'DROP COMMENT';
alter table migtest_e_history alter column test_string varchar(255);
alter table migtest_e_history_history alter column test_string varchar(255);
comment on table migtest_e_history is 'DROP COMMENT';
create table migtest_e_ref (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_ref primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
drop index if exists ix_migtest_e_basic_indextest3;
drop index if exists ix_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

