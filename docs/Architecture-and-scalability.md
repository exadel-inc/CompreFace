# Architecture and Scalability

CompreFace is delivered as a docker-compose file by default, so you can
easily start it with one command. However, CompreFace could be scaled up
to distribute computations on different servers and achieve high
availability. This section describes the architecture of CompreFace,
each of its components, and suggestions on how to scale the system.

## CompreFace architecture diagram

![CompreFace architecture diagram](https://user-images.githubusercontent.com/3736126/107855144-5db83580-6e29-11eb-993a-46cdc0c82812.png)

## Balancer + UI

Container name in the docker-compose file: `compreface-fe`

This container runs Nginx that serves CompreFace UI.

In the default config, it's also used as the main gateway - Nginx
proxies user requests to admin and API servers.

## Admin server

Container name in the docker-compose file: `compreface-admin`

Admin server is a Spring Boot application, and it's responsible for all
operations that are done on UI. Admin server connects to PostgreSQL
database to store the data.

## API servers

Container name in the docker-compose file: `compreface-api`

API servers handle all user API calls: face recognition, face detection,
and face verification.

It provides API key validation, proxies images to Embedding servers, and
classifies the face. For face classification, we use the ND4J library.

By default, the number of API servers in the config is 1, but for production
environments to increase possible bandwidth and achieve high
availability, there should be at least two such servers, and they should
be on different machines. In addition, the data synchronization is
implemented via PostgreSQL notifications, so if, for example, you add a
new face to a collection, all other servers know about it and can
recognize this new face.

Classification is not a very heavy operation as embedding calculation
and doesn't require GPU in most cases. API server connects to PostgreSQL
database to store the data.

There is a `PYTHON_UR`L environment variable that tells this container where
to send requests to `compreface-core` containers.
Default value: http://compreface-core:3000.

There is a `PYTHON_URL` environment variable that tells this container where to send requests to `compreface-core` containers. Default value: `http://compreface-core:3000`.

## Embedding Servers

Container name in the docker-compose file: `compreface-core`

The embedding server is responsible for running neural networks. It
calculates embeddings from faces and makes all plugin recognitions like
age and gender detection. These servers are stateless, don't have a
connection to a database, and don't require any synchronization between
them.

By default, the number of embedding servers in the config is 1, but for production
environments to increase possible bandwidth and achieve high
availability, there should be at least two such servers, and they should
be on different machines.

Running neural networks is a very heavy operation. Therefore, the total
performance of the system highly depends on these nodes. That is why we
recommend using highly performant nodes to run Embedding Servers,
ideally with GPU support. To learn more about how to run CompreFace with
GPU, see [custom-builds documentation](Custom-builds.md).

## PostgreSQL

Default docker-compose configuration includes postgreSQL database.

If you want CompreFace to connect to your database, you need to provide
such environment variables for `compreface-admin` and `compreface-api`
containers:
* POSTGRES_PASSWORD
* POSTGRES_URL
* POSTGRES_USER
