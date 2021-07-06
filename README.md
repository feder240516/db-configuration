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
This will setup a containerized Ubuntu environment able to run the tests