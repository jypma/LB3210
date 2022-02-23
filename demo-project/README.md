# Demo spring boot application

This application demonstrates some of the concepts of the Lund & Bendsen course.

# Architectural guidelines

When selecting and configuring frameworks, the following architectural check-list has been followed:

- Minimize "magic", i.e. prefer visible method calls and lamdas over thread-local variables and classpath scanning.
- Maximize use of types to indicate semantics
- Data objects are immutable

# Running the application

First, use `docker-compose` to start its dependencies:

```sh
docker-compose up -d
```

Then, you can use the included Maven wrapper to run the application

```sh
./mvnw spring-boot:run
```

# Docker

```sh
docker exec -ti demo-project_db_1 psql  demo -U demo
```
