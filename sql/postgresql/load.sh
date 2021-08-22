#!/bin/bash
pg_ctlcluster 12 data -o "-F -p 9901" start &
sleep 5
psql -p 9901 -d employees -a -f "./employees-postgresql.sql"