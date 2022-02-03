-- Migrationscripts for ebean unittest
-- apply changes
CALL usp_ebean_drop_column('migtest_e_basic', 'status2');

CALL usp_ebean_drop_column('migtest_e_basic', 'description');

