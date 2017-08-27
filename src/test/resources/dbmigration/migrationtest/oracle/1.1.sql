-- apply changes
UPDATE migtest_e_basic set status = 'A' WHERE status is null;
alter table migtest_e_basic modify status not null;
UPDATE migtest_e_basic set some_date = '2000-01-01T00:00:00' WHERE some_date is null;
alter table migtest_e_basic modify some_date not null;
alter table migtest_e_basic add column new_string_field varchar2(255) not null default 'foo';
alter table migtest_e_basic add column new_boolean_field number(1) not null default 1;
update migtest_e_basic set new_boolean_field = old_boolean;
alter table migtest_e_basic add column new_boolean_field2 number(1) not null default 1;

