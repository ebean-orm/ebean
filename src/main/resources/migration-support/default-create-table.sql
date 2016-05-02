create table ${table} (
  id                           integer not null,
  mtype                        varchar(1) not null,
  mstatus                      varchar(10) not null,
  mversion                     varchar(150) not null,
  mcomment                     varchar(150) not null,
  mchecksum                    integer not null,
  run_on                       timestamp not null,
  run_by                       varchar(30) not null,
  run_time                     integer not null,
  constraint pk_${table} primary key (id)
);

