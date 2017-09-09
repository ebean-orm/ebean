-- apply changes
create table migtest_e_user (
  id                            number(10) not null,
  constraint pk_migtest_e_user primary key (id)
);
create sequence migtest_e_user_seq;


update migtest_e_basic set status = 'A' where status is null;
alter table migtest_e_basic drop constraint ck_migtest_e_basic_status;
alter table migtest_e_basic modify status default 'A';
alter table migtest_e_basic modify status not null;
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I','?'));

-- rename all collisions;
alter table migtest_e_basic add constraint uq_migtest_e_basic_description unique  (description);

update migtest_e_basic set some_date = '2000-01-01T00:00:00' where some_date is null;
alter table migtest_e_basic modify some_date default '2000-01-01T00:00:00';
alter table migtest_e_basic modify some_date not null;

insert into migtest_e_user (id) select distinct user_id from migtest_e_basic;
alter table migtest_e_basic add constraint fk_migtest_e_basic_user_id foreign key (user_id) references migtest_e_user (id);
alter table migtest_e_basic modify user_id null;
alter table migtest_e_basic add column new_string_field varchar2(255) not null default 'foo''bar';
alter table migtest_e_basic add column new_boolean_field number(1) default 0 not null;
update migtest_e_basic set new_boolean_field = old_boolean;

alter table migtest_e_basic add column new_boolean_field2 number(1) default 0 not null;
alter table migtest_e_basic add column progress number(10) not null default 0;
alter table migtest_e_basic add constraint ck_migtest_e_basic_progress check ( progress in (0,1,2));
alter table migtest_e_basic add column new_integer number(10) not null default 42;

comment on column migtest_e_history.test_string is 'Column altered to long now';
alter table migtest_e_history modify test_string number(19);
comment on table migtest_e_history is 'We have history now';

update migtest_e_history2 set test_string = 'unknown' where test_string is null;
alter table migtest_e_history2 modify test_string default 'unknown';
alter table migtest_e_history2 modify test_string not null;
alter table migtest_e_history2 add column test_string2 varchar2(255);
alter table migtest_e_history2 add column test_string3 varchar2(255) not null default 'unknown';

alter table migtest_e_softdelete add column deleted number(1) default 0 not null;

create index ix_migtest_e_basic_indextest3 on migtest_e_basic (indextest3);
create index ix_migtest_e_basic_indextest6 on migtest_e_basic (indextest6);
drop index ix_migtest_e_basic_indextest1;
drop index ix_migtest_e_basic_indextest5;
