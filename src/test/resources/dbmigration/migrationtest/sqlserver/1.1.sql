-- apply changes
UPDATE migtest_e_basic set status = 'A' WHERE status is null;
alter table migtest_e_basic alter column status varchar(1) not null;
UPDATE migtest_e_basic set some_date = '2000-01-01T00:00:00' WHERE some_date is null;
alter table migtest_e_basic alter column some_date datetime2 not null;
alter table migtest_e_basic add new_string_field varchar(255) not null default 'foo';
alter table migtest_e_basic add new_boolean_field bit not null default 1;
update migtest_e_basic set new_boolean_field = old_boolean;
alter table migtest_e_basic add new_boolean_field2 bit not null default 1;

