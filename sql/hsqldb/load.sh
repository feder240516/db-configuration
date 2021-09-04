#!/bin/bash
echo "path = "
pwd
exit 1
DB_SCRIPT_FILE="employees-hsqldb.script"
RUN cp /usr/local/bin/db-configuration/sql/hsqldb/$DB_SCRIPT_FILE /usr/local/bin/DBInstances/HSQLDB/data/employees.script