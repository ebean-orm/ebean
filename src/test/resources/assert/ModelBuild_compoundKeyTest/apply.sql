create table ckey_assoc (
  id                            integer not null,
  assoc_one                     varchar(255),
  constraint pk_ckey_assoc primary key (id)
);
create sequence ckey_assoc_seq;

create table ckey_detail (
  id                            integer not null,
  something                     varchar(255),
  one_key                       integer,
  two_key                       varchar(255),
  constraint pk_ckey_detail primary key (id)
);
create sequence ckey_detail_seq;

create table ckey_parent (
  one_key                       integer not null,
  two_key                       varchar(255) not null,
  name                          varchar(255),
  assoc_id                      integer,
  version                       integer not null,
  constraint pk_ckey_parent primary key (one_key,two_key)
);

alter table ckey_detail add constraint fk_ckey_detail_parent foreign key (one_key,two_key) references ckey_parent (one_key,two_key) on delete restrict on update restrict;
create index ix_ckey_detail_parent on ckey_detail (one_key,two_key);

alter table ckey_parent add constraint fk_ckey_parent_assoc_id foreign key (assoc_id) references ckey_assoc (id) on delete restrict on update restrict;
create index ix_ckey_parent_assoc_id on ckey_parent (assoc_id);

