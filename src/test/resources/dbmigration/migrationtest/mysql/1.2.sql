-- apply changes
alter table migtest_e_basic alter status drop default;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic drop index uq_migtest_e_basic_description;
alter table migtest_e_basic alter some_date drop default;
alter table migtest_e_basic drop foreign key fk_migtest_e_basic_user_id;
UPDATE migtest_e_basic set user_id = ${default} WHERE user_id is null;
alter table migtest_e_basic modify user_id integer not null;
alter table migtest_e_basic add column old_boolean tinyint(1) not null default 0;
alter table migtest_e_basic add column old_boolean2 tinyint(1);
alter table migtest_e_basic add column eref_id integer;

alter table migtest_e_basic drop index uq_migtest_e_basic_name;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest4;
alter table migtest_e_basic drop index uq_migtest_e_basic_indextest5;
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
comment on column migtest_e_history.test_string is 'DROP COMMENT';
alter table migtest_e_history modify test_string varchar(255);
alter table migtest_e_history_history modify test_string varchar(255);
alter table migtest_e_history comment = 'DROP COMMENT';
create table migtest_e_ref (
  id                            integer auto_increment not null,
  constraint pk_migtest_e_ref primary key (id)
);

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
drop index ix_migtest_e_basic_indextest3 on migtest_e_basic;
drop index ix_migtest_e_basic_indextest6 on migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);

