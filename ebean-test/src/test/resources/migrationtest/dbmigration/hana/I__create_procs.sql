-- Inital script to create stored procedures etc for the hana platform
delimiter $$
--
-- PROCEDURE: usp_ebean_drop_foreign_keys TABLE, COLUMN
-- deletes all constraints and foreign keys referring to TABLE.COLUMN
--
CREATE OR REPLACE PROCEDURE usp_ebean_drop_foreign_keys(IN table_name NVARCHAR(256), IN column_name NVARCHAR(256))
AS
-- play-ebean-start
BEGIN
DECLARE foreign_key_names TABLE(CONSTRAINT_NAME NVARCHAR(256), TABLE_NAME NVARCHAR(256));
DECLARE i INT;

foreign_key_names = SELECT CONSTRAINT_NAME, TABLE_NAME FROM SYS.REFERENTIAL_CONSTRAINTS WHERE SCHEMA_NAME=CURRENT_SCHEMA AND TABLE_NAME=UPPER(:table_name) AND COLUMN_NAME=UPPER(:column_name);

FOR I IN 1 .. RECORD_COUNT(:foreign_key_names) DO
EXEC 'ALTER TABLE "' || ESCAPE_DOUBLE_QUOTES(:foreign_key_names.TABLE_NAME[i]) || '" DROP CONSTRAINT "' || ESCAPE_DOUBLE_QUOTES(:foreign_key_names.CONSTRAINT_NAME[i]) || '"';
END FOR;

END;
-- play-ebean-end
$$

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_column TABLE, COLUMN
-- deletes the column and ensures that all indices and constraints are dropped first
--
CREATE OR REPLACE PROCEDURE usp_ebean_drop_column(IN table_name NVARCHAR(256), IN column_name NVARCHAR(256))
AS
-- play-ebean-start
BEGIN
CALL usp_ebean_drop_foreign_keys(table_name, column_name);
EXEC 'ALTER TABLE "' || UPPER(ESCAPE_DOUBLE_QUOTES(table_name)) || '" DROP ("' || UPPER(ESCAPE_DOUBLE_QUOTES(column_name)) || '")';
END;
-- play-ebean-end
$$
