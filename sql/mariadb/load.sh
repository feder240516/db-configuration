#!/bin/bash
mysqld --datadir="/usr/local/bin/DBInstances/MariaDB/data" --port="9001" \
    --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" \
    --pid-file="/usr/local/bin/DBInstances/MariaDB/data/mysql.pid" --skip-grant-tables &
sleep 5
mysql -P 9001 --protocol="tcp" --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" < /usr/local/bin/db-configuration/sql/mariadb/employees-mariadb.sql
