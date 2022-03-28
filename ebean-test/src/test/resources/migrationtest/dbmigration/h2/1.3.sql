-- Migrationscripts for ebean unittest
-- drop dependencies
alter table migtest_ckey_detail drop constraint if exists fk_migtest_ckey_detail_parent;
alter table migtest_fk_cascade drop constraint if exists fk_migtest_fk_cascade_one_id;
alter table migtest_fk_none drop constraint if exists fk_migtest_fk_none_one_id;
alter table migtest_fk_none_via_join drop constraint if exists fk_migtest_fk_none_via_join_one_id;
alter table migtest_fk_set_null drop constraint if exists fk_migtest_fk_set_null_one_id;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status;
alter table migtest_e_basic drop constraint if exists ck_migtest_e_basic_status2;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_description;
alter table migtest_e_basic drop constraint if exists fk_migtest_e_basic_user_id;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_status_indextest1;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_name;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest4;
alter table migtest_e_basic drop constraint uq_migtest_e_basic_indextest5;
alter table migtest_e_enum drop constraint if exists ck_migtest_e_enum_test_status;
drop index if exists ix_migtest_e_basic_indextest3;
drop index if exists ix_migtest_e_basic_indextest6;
-- apply changes
create table "migtest_QuOtEd" (
  id                            varchar(255) not null,
  status1                       varchar(1),
  status2                       varchar(1),
  constraint ck_migtest_quoted_status1 check ( status1 in ('N','A','I')),
  constraint ck_migtest_quoted_status2 check ( status2 in ('N','A','I')),
  constraint uq_migtest_quoted_status2 unique (status2),
  constraint pk_migtest_quoted primary key (id)
);

create table migtest_e_ref (
  id                            integer generated by default as identity not null,
  name                          varchar(127) not null,
  constraint uq_migtest_e_ref_name unique (name),
  constraint pk_migtest_e_ref primary key (id)
);


update migtest_e_basic set status2 = 'N' where status2 is null;

update migtest_e_basic set a_lob = 'X' where a_lob is null;

update migtest_e_basic set user_id = 23 where user_id is null;
drop trigger migtest_e_history2_history_upd;
drop view migtest_e_history2_with_history;
drop trigger migtest_e_history3_history_upd;
drop view migtest_e_history3_with_history;
drop trigger migtest_e_history4_history_upd;
drop view migtest_e_history4_with_history;
drop trigger migtest_e_history6_history_upd;
drop view migtest_e_history6_with_history;

-- NOTE: table has @History - special migration may be necessary
update migtest_e_history6 set test_number2 = 7 where test_number2 is null;
-- apply alter tables
alter table migtest_e_basic alter column status drop default;
alter table migtest_e_basic alter column status set null;
alter table migtest_e_basic alter column status2 varchar(1);
alter table migtest_e_basic alter column status2 set default 'N';
alter table migtest_e_basic alter column status2 set not null;
alter table migtest_e_basic alter column a_lob varchar(255);
alter table migtest_e_basic alter column a_lob set default 'X';
alter table migtest_e_basic alter column a_lob set not null;
alter table migtest_e_basic alter column user_id set default 23;
alter table migtest_e_basic alter column user_id set not null;
alter table migtest_e_basic add column description_file blob;
alter table migtest_e_basic add column old_boolean boolean default false not null;
alter table migtest_e_basic add column old_boolean2 boolean;
alter table migtest_e_basic add column eref_id integer;
alter table migtest_e_history2 alter column test_string drop default;
alter table migtest_e_history2 alter column test_string set null;
alter table migtest_e_history2 add column obsolete_string1 varchar(255);
alter table migtest_e_history2 add column obsolete_string2 varchar(255);
alter table migtest_e_history2_history alter column test_string set null;
alter table migtest_e_history2_history add column obsolete_string1 varchar(255);
alter table migtest_e_history2_history add column obsolete_string2 varchar(255);
alter table migtest_e_history4 alter column test_number integer;
alter table migtest_e_history4_history alter column test_number integer;
alter table migtest_e_history6 alter column test_number1 drop default;
alter table migtest_e_history6 alter column test_number1 set null;
alter table migtest_e_history6 alter column test_number2 set default 7;
alter table migtest_e_history6 alter column test_number2 set not null;
alter table migtest_e_history6_history alter column test_number1 set null;
-- apply post alter
alter table migtest_e_basic add constraint ck_migtest_e_basic_status check ( status in ('N','A','I'));
alter table migtest_e_basic add constraint ck_migtest_e_basic_status2 check ( status2 in ('N','A','I'));
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest2 unique  (indextest2);
alter table migtest_e_basic add constraint uq_migtest_e_basic_indextest6 unique  (indextest6);
alter table migtest_e_enum add constraint ck_migtest_e_enum_test_status check ( test_status in ('N','A','I'));
comment on column migtest_e_history.test_string is '';
comment on table migtest_e_history is '';
create view migtest_e_history2_with_history as select * from migtest_e_history2 union all select * from migtest_e_history2_history;
create trigger migtest_e_history2_history_upd before update,delete on migtest_e_history2 for each row call "io.ebean.platform.h2.H2HistoryTrigger";
create view migtest_e_history3_with_history as select * from migtest_e_history3 union all select * from migtest_e_history3_history;
create trigger migtest_e_history3_history_upd before update,delete on migtest_e_history3 for each row call "io.ebean.platform.h2.H2HistoryTrigger";
create view migtest_e_history4_with_history as select * from migtest_e_history4 union all select * from migtest_e_history4_history;
create trigger migtest_e_history4_history_upd before update,delete on migtest_e_history4 for each row call "io.ebean.platform.h2.H2HistoryTrigger";
create view migtest_e_history6_with_history as select * from migtest_e_history6 union all select * from migtest_e_history6_history;
create trigger migtest_e_history6_history_upd before update,delete on migtest_e_history6 for each row call "io.ebean.platform.h2.H2HistoryTrigger";
comment on column "table"."index" is 'this is a comment';
-- foreign keys and indices
alter table migtest_fk_cascade add constraint fk_migtest_fk_cascade_one_id foreign key (one_id) references migtest_fk_cascade_one (id) on delete cascade on update restrict;
alter table migtest_fk_set_null add constraint fk_migtest_fk_set_null_one_id foreign key (one_id) references migtest_fk_one (id) on delete set null on update restrict;
create index ix_migtest_e_basic_eref_id on migtest_e_basic (eref_id);
alter table migtest_e_basic add constraint fk_migtest_e_basic_eref_id foreign key (eref_id) references migtest_e_ref (id) on delete restrict on update restrict;

create index ix_migtest_e_basic_indextest1 on migtest_e_basic (indextest1);
create index ix_migtest_e_basic_indextest5 on migtest_e_basic (indextest5);
create index ix_migtest_quoted_status1 on "migtest_QuOtEd" (status1);
