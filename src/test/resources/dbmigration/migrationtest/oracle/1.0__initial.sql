-- apply changes
create table e_basic (
  id                            number(10) not null,
  status                        varchar2(1),
  name                          varchar2(255),
  description                   varchar2(255),
  some_date                     timestamp,
  constraint ck_e_basic_status check ( status in ('N','A','I')),
  constraint pk_e_basic primary key (id)
);
create sequence e_basic_seq;

create index ix_e_basic_name on e_basic (name);
