-- apply changes
create table migtest_e_ref (
  id                            number(10) not null,
  constraint pk_migtest_e_ref primary key (id)
);
create sequence migtest_e_ref_seq;

alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic modify status drop default;
alter table migtest_e_basic modify status null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
alter table migtest_e_basic modify some_date drop default;
alter table migtest_e_basic modify some_date null;

update migtest_e_basic set user_id = 23 where user_id is null;
alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id;
alter table migtest_e_basic modify user_id default 23;
alter table migtest_e_basic modify user_id not null;
alter table migtest_e_basic add column old_boolean number(1) default 0 not null;
alter table migtest_e_basic add column old_boolean2 number(1) default 0;
alter table migtest_e_basic add column eref_id number(10);

comment on column migtest_e_history.test_string is '';
comment on table migtest_e_history is '';
alter table migtest_e_history2 modify test_string drop default;
alter table migtest_e_history2 modify test_string null;
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
drop index ix_migtest_e_basic_indextest3;
drop index ix_migtest_e_basic_indextest6;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

