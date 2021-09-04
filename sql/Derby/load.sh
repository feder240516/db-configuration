#!/bin/bash
cd /usr/local/bin/DBInstances/Derby/data
echo "ap path = "
pwd
DB_SCRIPT_FILE="/usr/local/bin/db-configuration/sql/employees-apacheDerby.sql"
#java -jar %DERBY_HOME%/lib/derbyrun.jar server start &
java -jar /opt/Apache/lib/derbyrun.jar ij $DB_SCRIPT_FILE
#sleep 5
#mysql -P 9001 --protocol="tcp" --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" < /usr/local/bin/db-configuration/sql/mariadb/employees-mariadb.sql
