#!/bin/bash
DB_SCRIPT_FILE="/usr/local/bin/db-configuration/sql/mariadb/init.sql"
mysqld --datadir="/usr/local/bin/DBInstances/MariaDB/data" --port="9001" \
    --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" \
    --pid-file="/usr/local/bin/DBInstances/MariaDB/data/mysql.pid" --skip-grant-tables &
sleep 5
mysql -P 9001 --protocol="tcp" --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" < $DB_SCRIPT_FILE
