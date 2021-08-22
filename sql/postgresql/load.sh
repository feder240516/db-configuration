#!/bin/bash
pg_ctlcluster 12 data -o "-F -p 9901" start
psql -p 9901 -U postgres -a -f "/usr/local/bin/db-configuration/sql/postgresql/employees-postgresql.sql"
pg_ctlcluster 12 data -o "-F -p 9901" stop