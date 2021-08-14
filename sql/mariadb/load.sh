#!/bin/bash
/bin/bash -c "mysqld --datadir=\"/usr/local/bin/DBInstances/MariaDB/data\" --port=\"9001\"" \
    --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" \
    --pid-file="/usr/local/bin/DBInstances/MariaDB/data/mysql.pid" --skip-grant-tables
sleep 5
#mysql -u root -e "CREATE DATABASE mydb"
#mysql -u root mydb < /tmp/dump.sql