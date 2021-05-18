# Demo spring boot application - Integration testing

This folder demonstrates how to do integration testing of the Demo application using Docker and Docker Compose.

The [docker-compose.yml](./docker-compose.yml]) file defines 3 Docker containers: Spring Boot, Postgresql, and RabbitMq. 

# How to run
## 1. Create docker image
First you have to create a Docker image for the Demo Spring Boot application.
This is done by issuing a Maven command:
```sh
cd demo-project #make sure to cd to the .. folder
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=logb/demo-project
```
This will build a Docker image with the name logb/demo-project:latest.

You can inspect the Docker images with this command:
```sh
docker image ls
```
Make sure there is a _logb/demo-project:latest_ in the list. 

## 2. Start containers using Docker Compose
The [docker-compose.yml](./docker-compose.yml]) file defines 3 containers and you can start the containers like this:

```sh
cd demo-project/demo-project-integration-test
docker-compose up -d 
```

You can verify that the containers are running by making a GET request to this URL:
```
http://localhost:8080/values
```

