-- apply changes
create table migtest_e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(255),
  description                   varchar2(255),
  some_date                     timestamp,
  old_boolean                   number(1) default 0 not null,
  old_boolean2                  number(1),
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);
create sequence migtest_e_basic_seq;

create index ix_migtest_e_basic_name on migtest_e_basic (name);
