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

# REST API
## Create a new resource
```
POST localhost:8080/values
Header:
    Content-Type: application/json
Body: 
    {"id":0,
    "key":"some key string",
    "value":"some value string"
    }
```

This returns a http response with a body like this:
```json
{
    "key":"some key string",
    "value":"some value string",
    "id": 3
}
```
and a Location header like: http://localhost:8080/values/3

## Get all values
```
GET localhost:8080/values
```
which returns a json array like this:
```json
[
  {
    "key": "key 1",
    "value": "a value",
    "id": 1
  },
  {
    "key": "key 2",
    "value": "a value",
    "id": 2
  },
  {
    "key": "key 3",
    "value": "a value",
    "id": 3
  }
]
```

## Get a single value
```
GET localhost:8080/values/1
```
which returns a json document like this:
```json
{
    "key": "key 1",
    "value": "a value",
    "id": 1
}
```

## Replace an existing value
```
PUT localhost:8080/values/3
Header:
    Content-Type: application/json
Body: 
    {"id":0,
    "key":"some key string",
    "value":"some value string"
    }
```

This returns a http response with a body like this:
```json
{
    "key":"some key string",
    "value":"some value string",
    "id": 3
}
```
and a Location header like: http://localhost:8080/values/3

