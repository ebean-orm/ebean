-- apply changes
UPDATE migtest_e_basic set status = 'A' WHERE status is null;
alter table migtest_e_basic alter column status set not null;
UPDATE migtest_e_basic set some_date = '2000-01-01T00:00:00' WHERE some_date is null;
alter table migtest_e_basic alter column some_date set not null;
alter table migtest_e_basic add column new_string_field varchar(255) not null default 'foo';
alter table migtest_e_basic add column new_boolean_field boolean not null default true;
update migtest_e_basic set new_boolean_field = old_boolean;
alter table migtest_e_basic add column new_boolean_field2 boolean not null default true;

