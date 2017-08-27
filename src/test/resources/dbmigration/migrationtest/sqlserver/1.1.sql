-- apply changes
UPDATE e_basic set status = 'A' WHERE status is null;
alter table e_basic alter column status varchar(1) not null;
UPDATE e_basic set some_date = '2000-01-01T00:00:00' WHERE some_date is null;
alter table e_basic alter column some_date datetime2 not null;
alter table e_basic add new_string_field varchar(255) not null default 'foo';
alter table e_basic add new_boolean_field bit not null default 1;
update e_basic set new_boolean_field = old_boolean_field;
alter table e_basic add new_boolean_field2 bit not null default 1;

