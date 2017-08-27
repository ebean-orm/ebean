-- apply changes
create table migtest_e_basic (
  id                            integer not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     datetime2,
  old_boolean                   bit default 0 not null,
  old_boolean2                  bit,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);
create sequence migtest_e_basic_seq as bigint  start with 1 ;

create index ix_migtest_e_basic_name on migtest_e_basic (name);
