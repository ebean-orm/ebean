The migration dir contains 3 folders `v1_0`, `v1_1`, `v1_2`.

The general idea is, that we try to perform a migration (=`v1.0` -> `v1.1`) and also if it is revertable (= `v1.1` -> `v1.0`)

To achieve that, the directories `v1_0` and `v1_2` should be as equal as possible. Annomalies should be commented.

It is also important, if new models are added, that all table names (also for M2M) are added in the `DbMigrationTest`
cleanup routine.


