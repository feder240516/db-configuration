# db-configuration
A project about testing and benchmarking different settings for multiple db
engines.

## Requirements and setup
Current project only works on Linux, as some evaluated algorithms (SMAC) only 
run on OS based on this kernel.

However, to ensure a fast start (and to allow working on Windows), included in 
this repository is a Dockerfile, which you can build if you have Docker 
installed by cloning this repository and executing
```
docker build . -t db-configuration
```
inside the repo folder. 
This will setup a containerized Ubuntu environment able to run the tests.

To run the project, you can run
```
./gradlew run --args="algorithm queryProfile threads timeLimit [numOfExecutions] [numOfLastExecution]"
```

where
 - algorithm: The algorithm to evaluate: HASCO | BOHB | SMAC | Random
 - queryProfile: The id number of the query profile to execute. By default, by  
   using the provided database, this goes from 1 to 5, however the specific 
   query profiles should be created by the user in the MainExecuteAnyAlgorithm 
   file.
 - threads: The number of threads that this software is allowed to use. Note 
   that the python algorithms and the RDBMS run independently from the java 
   execution, so be sure of having enough processing for all of this.
 - timeLimit: The limit in minutes the selected algorithm is allowed to run.
 - numOfExecutions (optional) (default=10): The number of executions for the 
   selected algorithm. Note that the total execution of the software will be 
   timeLimit * numOfExecutions.
 - numOfLastExecution (optional) (default=10): If the software hangs for any 
   reason, you can use this parameter to keep the enumeration of the log files.

## However, 
before running the project, you should setup your databases in the sql folder,
and the query profiles in the MainExecuteAnyAlgorithm java file. The java 
library used for this purpose is called JOOQ, and its docs can be find here: 
[JOOQ Docs](https://www.jooq.org)

## How to setup your own data

First of all, you should populate the data for every used RDBMS (Apache Derby, 
MariaDB, HSQLDB, PostgreSQL are supported). When you finish populating these
systems, dump the data inside each sql/[RDBMS name] folder. An example of how
these dump files could look is given for each RDBMS.
 
 - For MariaDB: See https://mariadb.com/kb/en/making-backups-with-mysqldump/
 - For Apache Derby: See https://db.apache.org/derby/docs/10.11/adminguide/cadminimport16245.html
 - For PostgreSQL: See https://www.postgresql.org/docs/9.1/backup-dump.html
 - For HSQLDB: After populating and shutting down your database, copy the 
   .script file to the sql folder.

Next to filling the databases, it's neccessary to modify the load.sh files to
change the name of the sql file.

To create your own query profiles, modify the buildTestDescription function on 
the MainExecuteAnyAlgorithm java file. This will allow you to run specific sql 
queries that will run on any RDBMS.

Finally, the search space can be modified by changing the file
src/main/java/configuration/dbTestProblem.json. A sample file with all
currently configurable properties is given, however take into account that if 
you modify these parameters over their limits, the RDBMSs may fail while
initializing these parameters, and whether it does or not is implementation
specific (PostgreSQL fails while trying to set an non-existent property, while
other RDBMSs just ignore these unknown properties).

## RDBMSs versions
 - Apache Derby 10.15.2.0
 - HSQLDB 2.6.0
 - MariaDB latest (see notes below)
 - PostgreSQL 12

## Notes

 - The preferred way for working with the software is by using Docker, however
   if you want to run it in a real system, preferrably do it in a Linux VM. It
   *does not work on Windows* without using Docker, due to some Python
   requirements only existent on Linux.
 - For ease of use, authentication concerns were omitted during the development 
   of this software. For that reason, if you use this software on a cloud 
   environment, be sure of taking your own precautions (firewall, ssh 
   protection, port exposure, etc.)
 - This software comes without any warranty, and its creators won't be
   responsible of any damage.
 - MariaDB installation defaults to the latest MariaDB server and client 
   version. If you want a different version, you will have to manually change it
   on the Dockerfile, but beware: [MariaDB on Ubuntu - specific version](https://stackoverflow.com/a/67812247)

## Known issues

 - When initiating Postgres, it will complain about not being able to bind to
   running port (could not bind IPv6 address "::1": Cannot assign requested 
   address). However, this doesn't mean it hasn't initiated; in fact, if the
   last log line says "database system is ready to accept connections", it means
   that Postgres server was succesfully started.