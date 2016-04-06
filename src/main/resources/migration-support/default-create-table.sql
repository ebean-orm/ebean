create table ${table} (
  id                           integer not null,
  status                       varchar(10) not null,
  run_version                  varchar(150) not null,
  dep_version                  varchar(150) not null,
  comment                      varchar(150),
  checksum                     integer not null,
  run_on                       timestamp not null,
  run_by                       varchar(30) not null,
  run_ip                       varchar(30),
  constraint pk_${table} primary key (id)
);

