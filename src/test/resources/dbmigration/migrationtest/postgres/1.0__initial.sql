-- apply changes
create table migtest_e_basic (
  id                            serial not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     timestamptz,
  old_boolean                   boolean default false not null,
  old_boolean2                  boolean,
  constraint ck_migtest_e_basic_status check ( status in ('N','A','I')),
  constraint pk_migtest_e_basic primary key (id)
);

create index ix_migtest_e_basic_name on migtest_e_basic (name);
