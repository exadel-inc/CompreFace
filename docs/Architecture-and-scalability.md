# Architecture and Scalability

By default, CompreFace is delivered as docker-compose file, so you can easily start it with one command. However, CompreFace could be scaled up to distribute computations on different servers and achieve high availability.
This section describes the architecture of CompreFace, each of its components, and suggestions on how to scale the system.

## CompreFace architecture diagram

![CompreFace architecture diagram](https://user-images.githubusercontent.com/3736126/107855144-5db83580-6e29-11eb-993a-46cdc0c82812.png)

## Balancer + UI
Container name in docker-compose file: compreface-fe

This container runs Nginx that serves CompreFace UI.

In the default config, it’s also used as the main gateway - Nginx proxies user requests to admin and api servers.

## Admin server
Container name in docker-compose file: compreface-admin

Admin server is a Spring Boot application and it’s responsible for all operations that are done on UI. Admin server connects to PostgreSQL database to store the data.

## Api servers
Container name in docker-compose file: compreface-api

Api servers handle all user API calls: face recognition, face detection, and face verification.

It provides API key validation, proxies images to Embedding servers, and classifies the face. For face classification, we use the ND4J library.

In the default config number of API servers is 1, but for production environments to increase possible bandwidth and to achieve high availability, there should be at least two such servers and they should be on different machines. The data synchronization is implemented via postgreSQL notifications, so if for example, you add a new face to a collection, all other servers will know about it and will be able to recognize this new face.

Classification is not a very heavy operation as embedding calculation and in most cases doesn’t require GPU. API server connects to PostgreSQL database to store the data.

There is a `PYTHON_URL` environment variable that tells this container where to send requests to `compreface-core` containers. Default value: `http://compreface-core:3000`.

## Embedding Servers
Container name in docker-compose file: compreface-core

Embedding server is responsible for running neural networks. It calculates embeddings from faces and makes all plugin recognitions like age and gender detection. These servers are stateless, they don't have a connection to a database and they don't require any synchronization between each other.

In the default config number of API servers is 1, but for production environments to increase possible bandwidth and to achieve high availability, there should be at least two such servers and they should be on different machines.

Running neural networks is a very heavy operation. The total performance of the system highly depends on these nodes. This is why we recommend using highly performant nodes to run Embedding Servers, ideally with GPU support. To learn more about how to run CompreFace with GPU, see custom builds documentation.

## PostgreSQL
Default docker-compose configuration includes postgreSQL database.

If you want CompreFace to connect to your database, you need to provide such environment variables for compreface-admin and compreface-api containers:
* POSTGRES_PASSWORD
* POSTGRES_URL
* POSTGRES_USER
