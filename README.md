Play - Movies
=============
This is a simple webservice made using Scala and the Play Framework. It implements a simple
REST API for maintaining a collection of data about movies.

Requires
--------
* `Java`
  - developed using Java 11 from Adopt Open JDK 11
* `sbt`
  - developed using sbt script version 1.3.13
* `Docker` 
  - developed against = 19.03.12
* `docker-compose`
  - developed against = 1.26.2
  - compose file format = 3.8

Run It
------
1. First, clone this repository on a machine with `Docker` and `docker-compose`
2. Then from the root repository directory run `docker-compose up`
3. Get a cup of coffee, Docker will be working for a while

That's it!

`docker-compose` will stand up two containers: one for `MongoDB`, and one for the application. The application container
is built from the image described in the [Dockerfile](./Dockerfile). When that container is started, Docker will compile
and run the Play application using `sbt`.

The API
-------
The webservice currently supports the following URIs:

**Get a list of all movies**
```html
GET (baseUri)/movies
```

Sample Request:
```bash
curl -X GET localhost:9000/movies
```

Sample Response:
```json
[
    {
        "_id": "5f162d636986206e2fc54f6a",
        "title": "Pulp Fiction",
        "year": 1994,
        "rated": "R",
        "released": "1994-10-14",
        "genre": [
            "Crime",
            "Drama"
        ]
    }
]
```

**Add a Movie** 
```html
POST localhost:9000/movies    
```

Sample Request:
```bash
curl \
    -X POST \
    -H 'Content-Type: application/json' \
    -d '{"title":"Inception","year":2010,"rated":"PG-13","released":"2010-07-16","genre":"Action,Adventure,Sci-Fi"}' \
    localhost:9000/movies
```

Sample Response:
```json
{
    "_id": "5f1634866986206e2fc54f6c",
    "title": "Inception",
    "year": 2010,
    "rated": "PG-13",
    "released": "2010-07-16",
    "genre": [
        "Action",
        "Adventure",
        "Sci-Fi"
    ]
}
```

Returns:  
 
 Code |   Meaning  | Reason  
 ---- |   -------  | ------  
 200  |      OK    | if the movie already existed, but this was a valid request  
 201  |   Created  | if a new movie with the given information was created
 400  | Bad Request| if the form input was invalid
 
**Delete a Movie**
```html
DELETE localhost:9000/movies?id=5f1634866986206e2fc54f6c
```

Sample Request:
```bash
curl -X DELETE localhost:9000/movies?id=5f1634866986206e2fc54f6c
```

Returns:

 Code |   Meaning  | Reason  
 ---- |   -------  | ------  
 200  |      OK    | if the movie with the given Id was deleted  
 404  |  Not Found | if no movie with the given Id was found

