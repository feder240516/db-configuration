FROM ubuntu
RUN apt-get update
RUN apt-get install -y openjdk-11-jdk
RUN apt-get install -y python3.8 python3.8-dev
RUN apt-get install -y apt-utils software-properties-common
RUN apt-get install -y apt-transport-https ca-certificates gnupg2 git curl build-essential systemd gcc mc python3-pip swig
RUN apt-get install -y wget

# RUN ["/bin/bash", "-c" "set -o pipefail && wget https://downloads.apache.org//db/derby/db-derby-10.15.2.0/db-derby-10.15.2.0-bin.tar.gz \
#     && mkdir /opt/Apache && mv db-derby-10.15.2.0-bin.tar.gz /opt/Apache && /opt/Apache \
#     && tar xzvf db-derby-10.15.2.0-bin.tar.gz"]

# install apache
RUN wget https://downloads.apache.org//db/derby/db-derby-10.15.2.0/db-derby-10.15.2.0-bin.tar.gz \
    && mkdir /opt/Apache && mv db-derby-10.15.2.0-bin.tar.gz /opt/Apache/ && cd /opt/Apache \
    && tar xzvf db-derby-10.15.2.0-bin.tar.gz && rm db-derby-10.15.2.0-bin.tar.gz
# install mariadb
RUN apt-get install -y mariadb-server
# install postgres
RUN apt-get install -y postgresql-12
# install hsqldb
RUN wget https://netactuate.dl.sourceforge.net/project/hsqldb/hsqldb/hsqldb_2_6/hsqldb-2.6.0.zip \
    && mkdir /opt/HSQLDB && mv hsqldb-2.6.0.zip /opt/HSQLDB/ && cd /opt/HSQLDB \
    && unzip hsqldb-2.6.0.zip && rm hsqldb-2.6.0.zip

# data MariaDB
RUN mkdir /usr/local/bin/DBInstances
RUN mkdir /usr/local/bin/DBInstances/MariaDB
RUN cp -r /var/lib/mysql /usr/local/bin/DBInstances/MariaDB/data
RUN chmod -R 777 /usr/local/bin/DBInstances/MariaDB/data
RUN mkdir /usr/local/bin/DBInstances/MariaDB/instances
# data Postgres
RUN pg_createcluster 12 data
RUN echo -e "host all all 0.0.0.0/0 md5 \nhost all all ::/0 md5" >> /etc/postgresql/12/data/pg_hba.conf
# data HSQLDB
RUN mkdir /usr/local/bin/DBInstances/HSQLDB
# RUN cp -r /opt/HSQLDB/hsqldb-2.6.0/ /usr/local/bin/DBInstances/HSQLDB/data
RUN mkdir /usr/local/bin/DBInstances/HSQLDB/data
RUN chmod -R 777 /usr/local/bin/DBInstances/HSQLDB/data
RUN mkdir /usr/local/bin/DBInstances/HSQLDB/instances
# data Apache Derby
RUN mkdir /usr/local/bin/DBInstances/ApacheDerby
RUN mkdir /usr/local/bin/DBInstances/ApacheDerby/data
RUN mkdir /usr/local/bin/DBInstances/ApacheDerby/instances
WORKDIR /usr/local/bin/DBInstances/ApacheDerby/data
RUN java -jar /opt/Apache/lib/derbyrun.jar ij /usr/local/bin/db-configuration/sql/apacheDerby/employees-apacheDerby.sql
# if python2 is installed, these steps are needed to change python alias for python3
# RUN apt-get install python3.8
# RUN rm /usr/bin/python
# RUN ln -s /usr/bin/python3 /usr/bin/python

# install python dependencies
RUN pip3 install wheel
RUN pip3 install argparse
RUN curl https://raw.githubusercontent.com/automl/auto-sklearn/master/requirements.txt | xargs -n 1 -L 1 pip install
RUN pip3 install grpcio
RUN pip3 install grpcio-tools
RUN pip3 install hpbandster

# RUN apt-get update && apt-get -y install sudo

# clone repo
ADD . /usr/local/bin/db-configuration/
RUN chmod +x /usr/local/bin/db-configuration/gradlew

WORKDIR /usr/local/bin/db-configuration
# ensure gradle download -- COMMENTED DUE TO FAILURE, GRADLE WILL BE DOWNLOADED DURING NORMAL USAGE
# RUN /usr/local/bin/db-configuration/gradlew

# install mariadb sample data
# RUN /bin/bash -c "mysqld --datadir=\"/usr/local/bin/DBInstances/MariaDB/data\" --port=\"9001\"" \
#     --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" \
#     --pid-file="/usr/local/bin/DBInstances/MariaDB/data/mysql.pid" --skip-grant-tables \
#     && sleep 5 \
#     && mysql -u root -P 9001 --socket="/usr/local/bin/DBInstances/MariaDB/data/mysql.sock" < /usr/local/bin/db-configuration/sql/mariadb/employees-mariadb.sql
RUN chmod +x /usr/local/bin/db-configuration/sql/mariadb/load.sh
RUN /usr/local/bin/db-configuration/sql/mariadb/load.sh
# RUN mysql < /usr/local/bin/db-configuration/sql/mariadb/employees-mariadb.sql


CMD [ "/bin/bash" ]