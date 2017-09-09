-- apply changes
create table migtest_e_ref (
  id                            integer identity(1,1) not null,
  constraint pk_migtest_e_ref primary key (id)
);

alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic drop constraint df_migtest_e_basic_status;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
alter table migtest_e_basic drop constraint df_migtest_e_basic_some_date;

update migtest_e_basic set user_id = 23 where user_id is null;
IF OBJECT_ID('fk_migtest_e_basic_user_id', 'F') IS NOT NULL alter table migtest_e_basic drop constraint fk_migtest_e_basic_user_id;
alter table migtest_e_basic add constraint df_migtest_e_basic_user_id default 23 for user_id;
alter table migtest_e_basic alter column user_id integer not null;
alter table migtest_e_basic add old_boolean bit default 0 not null;
alter table migtest_e_basic add old_boolean2 bit default 0;
alter table migtest_e_basic add eref_id integer;

alter table migtest_e_history2 drop constraint df_migtest_e_history2_test_string;
create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest3') drop index ix_migtest_e_basic_indextest3 ON migtest_e_basic;
IF EXISTS (SELECT name FROM sys.indexes WHERE object_id = OBJECT_ID('migtest_e_basic','U') AND name = 'ix_migtest_e_basic_indextest6') drop index ix_migtest_e_basic_indextest6 ON migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id);
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

