-- apply changes
create table e_basic (
  id                            integer auto_increment not null,
  status                        varchar(1),
  name                          varchar(255),
  description                   varchar(255),
  some_date                     datetime(6),
  old_boolean                   tinyint(1) default 0 not null,
  old_boolean2                  tinyint(1),
  constraint ck_e_basic_status check ( status in ('N','A','I')),
  constraint pk_e_basic primary key (id)
);

create index ix_e_basic_name on e_basic (name);
