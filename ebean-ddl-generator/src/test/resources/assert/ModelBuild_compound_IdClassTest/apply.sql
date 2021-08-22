create table cksite_user (
  site_id                       uuid not null,
  user_id                       uuid not null,
  access_level                  varchar(255),
  version                       bigint not null,
  constraint pk_cksite_user primary key (site_id,user_id)
);

