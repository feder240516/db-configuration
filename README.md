# db-configuration
A project about testing and benchmarking different settings for multiple db
engines.

## Requirements and setup
Current project only works on Linux, as some evaluated algorithms (SMAC) only run on OS based on this kernel.

However, to ensure a fast start (and to allow working on Windows), included in this repository is a Dockerfile, which you can build if you have Docker installed by cloning this repository and executing
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
 - queryProfile: The id number of the query profile to execute. By default, using the 
   provided database, this goes from 1 to 5, however the specific query profiles should be
   created by the user in the MainExecuteAnyAlgorithm file.
 - threads: The number of threads that this software is allowed to use. Note that the python
   algorithms and the RDBMS run aside from the java execution, so be sure of having enough
   processing for all of this.
 - timeLimit: The limit in minutes the selected algorithm is allowed to run.
 - numOfExecutions (optional) (default=10): The number of executions for the selected algorithm.
   Note that the total execution of the software will be timeLimit * numOfExecutions.
 - numOfLastExecution (optional) (default=10): If the software hangs for any reason, you can
   use this parameter to keep the enumeration of the log files.

 ## However, 
before running the project, you should setup your databases in the sql folder,
and the query profiles in the MainExecuteAnyAlgorithm java file. The java library used for this purpose
is called JOOQ, and its docs can be find here: [JOOQ Docs](https://www.jooq.org)


   