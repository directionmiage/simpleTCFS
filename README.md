# The Cookie Factory... in Spring (aka the simple TCFS)

  * Author: Philippe Collet
  * Author: Nassim Bounouas
  * Reviewer: Anne-Marie Déry
  * some code and doc borrowed from the original Cookie Factory by Sebastiem Mosser, last fork being [https://github.com/collet/4A_ISA_TheCookieFactory](https://github.com/collet/4A_ISA_TheCookieFactory)

This case study is used to illustrate the different technologies involved in the _Introduction to Software Architecture_  course given at Polytech Nice - Sophia Antipolis at the graduate level. This demonstration code requires the following software to run properly:

  * Build & Spring environment configuration: Maven >=3.8.1
  * J2E implementation language: Java >=11 or above (Java language level is set to Java 11)
  * .Net implementation language: Mono >=6.12

## Product vision

_The Cookie Factory_ (TCF) is a major bakery brand in the USA. The _Cookie on Demand_ (CoD) system is an innovative service offered by TCF to its customer. They can order cookies online thanks to an application, and select when they'll pick-up their order in a given shop. The CoD system ensures to TCF's happy customers that they'll always retrieve their pre-paid warm cookies on time.

## Chapters

  1. [Architecture](chapters/Architecture.md)
  2. [Business Components](chapters/BusinessComponents.md)
  3. [Controllers](chapters/Controllers.md)
  4. [Testing](chapters/Testing.md)
  5. [Persistence](chapters/Persistence.md)
  6. [AOP, logging, and monitoring](chapters/AOPLogging.md)

## How to use this repository

  * The `develop` branch (the default one) represents the system under development. 
    * The [commit tagged v1.0.0](https://github.com/CookieFactoryInSpring/simpleTCFS/tree/v1.0.0) references the codebase that implements the system without persistence;
    * The `HEAD` of the `develop` branch includes a persistent version with a containerized postgres DB and a the standard in-memory H2 DB for testing.

The following "build and run" documentation is divided in three versions from bare run to "everything in a container" run.

### Basic build and run (with persistence)

The first step is to build the backend and the cli. This can be done manually using the command:

    mvn clean package
 
from both folders (it will generate the corresponding jar into the target folder). Note that this comand will only run unit tests, you can use:

    mvn clean verify
    
to run both unit and integration tests. See the page on [Testing](chapters/Testing.md#running-different-types-of-test-with-maven) for more details.

With a postgres DB running inside docker but accessible outside (in your host), first run:

   ./run-postgres-out-of-docker-compose.sh
   
This will run a postgres server listening on the 5432 port of your host machine.

Do not forget to run the dotNet external system as well!

To run the server (from the corresponding folder):

    POSTGRES_HOST=127.0.0.1:5432 mvn spring-boot:run
    
or

    POSTGRES_HOST=127.0.0.1:5432 java -jar target/simpleTCFS-0.0.1-SNAPSHOT.jar

To run the cli (from the corresponding folder):

    mvn spring-boot:run
    
or

    java -jar target/cli-0.0.1-SNAPSHOT.jar

At startup the cli must provide the following prompt :

    shell:>

Running the command `help` will guide you in the CLI usage.

### Containerized backend (only without persistence)

*The following explanations are only valid for the version without persistence.*

In this version, we will run the cli as previously, however we will run the backend in a docker container.

  * To build the backend docker image from the corresponding folder, the script `build.sh` can be used or directly the command `docker build --build-arg JAR_FILE=target/simpleTCFS-0.0.1-SNAPSHOT.jar -t pcollet/tcf-spring-backend .`

  * To run it the script `run.sh` can be used or directly `docker run --rm -d -p 8080:8080 pcollet/tcf-spring-backend`.

Note: It's necessary to stop the "basic" version of the backend to release the 8080 port.

### Everything containerized and composed (with persistence)

We will now run the backend with postgres, the CLI, and the external system into docker. It requires to build the cli docker and the dotNet external system images (the backend's one is considered built during the previous step).

To build the cli docker image from the corresponding folder, the script `build.sh` can be used or directly the command `docker build --build-arg JAR_FILE=target/cli-0.0.1-SNAPSHOT.jar -t pcollet/tcf-spring-cli .`

To build the external system, use also its [`build.sh`](dotNet/build.sh).

As for the postgres database, we reuse its standard image and configure several environment variables that will be use to configure the backend (so that the JPA configuration will connect to the DB).

The whole system can now be deployed locally from the root folder using the command:

    docker-compose up -d
    
after few seconds (be sure to wait enough to let JPA set up the database):

    docker attach cli

enables to use the containerized cli (see docker-compose.yml and the devops lecture for more information). In the spring shell, you can run a demo with `script demo.txt`.

As for persistence, you can use the `psql` command within the postgres image to connect to the DB with a SQL cli:

    docker exec -it db psql -U postgresuser -W -d tcf-db
    
And then commands like:

   * `\dt+` to list all tables
   * `SELECT * FROM customer;` to check that the two customers have been created by the demo script.

Note that you cannot run the two docker images separately and expect them to communicate with each other, each one being isolated in its own container. That's one of the main purpose of `docker-compose` to enable composition of container, with by default a shared network.

The docker-compose file contains a volume declaration (see the cli section) to mount the `demo.txt` file which can be directly used from the cli as below to iterate through a complete scenario and check that verify is running fine :

```
shell:>script --file demo.txt
CHOCOLALALA
DARK_TEMPTATION
SOO_CHOCOLATE
[...]
```

Some actions will display intended errors :

```
403 : [{"error":"Attempting to update the cookie quantity to a negative value","details":"from Customer kadocwith cookie SOO_CHOCOLATE leading to quantity -1"}]
403 : [{"error":"Cart is empty","details":"from Customer kadoc"}]
409 : [no body]
400 : [{"error":"Payment was rejected","details":"from Customer tatie for amount 1.3"}]
```

### Troubleshoot

### dotNet project : compile.sh

On a bare windows setup, `compile.sh` cannot be used. You can install Cygwin or directly use the `csc` command to build the project (see `compile.sh`).
### "/usr/bin/env: 'bash\r': No such file or directory"

If during the docker-compose start up you encounter this error `/usr/bin/env: 'bash\r': No such file or directory` with the back or the cli container, you need to rebuild the corresponding image with the `wait-for-it.sh` configured to use LF end of line sequence instead CRLF. This can occur if you open the file with an editor like VSCode configured to use CRLF.
