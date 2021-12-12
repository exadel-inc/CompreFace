# Installation (Deployment) options

Exadel CompreFace consists of several services and a database. 
Full architecture description and scaling tips you can find [here](Architecture-and-scalability.md). 
Each service is put to docker image for simpler usage, and they can be run separately. 
However, for a better user experience, CompreFace provides three distribution options that help install CompreFace easier. 
By default, CompreFace is delivered as a docker-compose configuration. But there are more options to install and run CompreFace. 
Each of them has its benefits and disadvantages.

|       Distribution       |                          Advantages                          |                   Disadvantages                    |                       Best for                        |
|:------------------------:|:------------------------------------------------------------:|:--------------------------------------------------:|:-----------------------------------------------------:|
| Docker Compose (Default) | Simple configuration<br/>Simple run<br/>Passed QA regression | Requires Docker Compose<br/>Runs on single machine |                  Local installation                   |
|        Kubernetes        |                       Simple to scale                        |            Requires Kubernetes cluster             |                Production installation                |
| Single docker container  |             Simple configuration<br/>Simple run              |  Least reliable option<br/>Runs on single machine  | Local installation if Docker Compose is not supported |

## Docker Compose

Docker-compose configuration allows simply run, configure, stop and restart CompreFace.
To install CompreFace using docker-compose just follow instructions in [getting started](../README.md#getting-started-with-compreface)

### Maintaining tips

1. After you run CompreFace, wait at least 30 seconds until it starts. 
   Do not stop it during this time, as it may corrupt database data during data migration.
2. You can run `docker-compose ps` to see all CompreFace services. 
   There should be 5 CompreFace services: compreface-core, compreface-api, compreface-admin, compreface-ui, compreface-postgres-db. 
   If at least one of the services is not in `Up` status - CompreFace failed to start.
3. To see the logs of service, run `docker-compose logs -f <service>`, e.g. `docker-compose logs -f compreface-api`. 
   You also can run `docker-compose logs -f` to see the logs of all CompreFace services.
4. Docker-compose automatically restarts all services if they fail. It also automatically starts them after you restart your machine.
5. If you want to stop CompreFace, run `docker-compose stop`. 
   You can also stop each container one by one, e.g. `docker-compose stop compreface-core`.
6. To start stopped Compreface, run `docker-compose start`. 
   You can also start each container one by one, e.g. `docker-compose start compreface-core`.
7. If you want to restart CompreFace, run `docker-compose restart`. 
   You can also restart each container one by one, e.g. `docker-compose restart compreface-core`.
8. All the data is stored locally on your machine. It is stored in a named docker volume. 
   This guarantees that if you stop or delete CompreFace docker containers, you won’t lose the data. 
   To find the volume name, run `docker volume ls`, the name should be `<CompreFace folder>_postgres-data`, e.g. `compreface_061_postgres-data`.
9. If you want to clear CompreFace installation, first stop it with `docker-compose stop`. 
   Then delete the volume, e.g. `docker volume rm compreface_061_postgres-data`. Then run CompreFace again `docker-compose up -d`.
10. To update the CompreFace version or change custom build, download new `docker-compose.yml` and `.env` files.
   Stop CompreFace with `docker-compose down`. Copy new files into the old CompreFace folder. Then run CompreFace with `docker-compose up -d`.

### Troubleshooting

1. Problem: `compreface-core` doesn’t run.

   Probable solution: please check if you have supported CPU or GPU. The default version of CompreFace requires an x86 processor and AVX support.

2. Problem: `compreface-admin` doesn’t start and there are logs like `Waiting for changelog lock....`

   Solution: clear CompreFace installation (see #Maintaining-tips)

## Kubernetes

You can find all Kubernetes scripts in CompreFace [Kubernetes repository](https://github.com/exadel-inc/compreface-kubernetes).

## Single docker container

Except for other distribution options, here all services and the database are placed in one docker image. 
The obvious advantage of this approach is that it is the simplest way to start CompreFace. 
However, it has some limitations in maintaining and troubleshooting. 
E.g. it’s very difficult to stop or restart services one by one. 
[Supervisord](http://supervisord.org/) was used to maintain several services in one Docker container.

**Requirements:**

1. Docker Engine for Linux or Docker Desktop for Windows and macOS
2. CompreFace could be run on most modern computers with x86 processor and AVX support. 
   To check AVX support on Linux run `lscpu | grep avx` command

To install CompreFace single docker container run command (you don’t need anything to download manually):

```commandline
docker run -d --name=CompreFace -v compreface-db:/var/lib/postgresql/data -p 8000:80 exadel/compreface
```

To use your own database for storing the data, specify these environment variables: POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_URL, EXTERNAL_DB, e.g.:

```commandline
docker run -d --name=CompreFace -e "POSTGRES_URL=jdbc:postgresql://url:port/db_name" -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -e EXTERNAL_DB=true -p 8000:80 exadel/compreface
```

To run the custom version of CompreFace, specify it in the end, e.g.:
```commandline
docker run -d --name=CompreFace -v compreface-db:/var/lib/postgresql/data -p 8000:80 exadel/compreface:0.6.0
```

To run custom builds you can use corresponding tags, e.g.:
```commandline
docker run -d --name=CompreFace -v compreface-db:/var/lib/postgresql/data -p 8000:80 exadel/compreface:0.6.1-mobilenet
```

To run version with GPU, you need to specify `--runtime=nvidia` and corresponding tag, e.g.:
```commandline
docker run -d --name=CompreFace -v compreface-db:/var/lib/postgresql/data --runtime=nvidia -p 8000:80 exadel/compreface:0.6.1-arcface-r100-gpu 
```

### Maintaining tips

1. Start CompreFace in a single docker container takes at least 45 seconds. 
   So long start is because of manual timings that help to start services in the right order.
2. There is a possibility that the database starts too slow, then service `compreface-admin` will fail. 
   Supervisord will restart it automatically and CompreFace should start properly.
3. To check if the run is finished, you can check the logs `docker logs CompreFace -f`. 
   If you see `exited: startup (exit status 0; expected)` log, it is finished.
4. To check if CompreFace is run, run `docker ps`. It should be a container with the name `CompreFace`. 
   You set the name of the container in run command `name=CompreFace`
5. `compreface-db` in the run command is the name of the volume, all your data is stored locally in this volume. 
   This guarantees that if you stop or delete CompreFace docker containers, you won’t lose the data.  
6. You can use environment variables from `docker-compose` version, e.g.  to set API server limit you can run:
```commandline
docker run -d -e "API_JAVA_OPTS=-Xmx8g" --name=CompreFace -v compreface-db:/var/lib/postgresql/data -p 8000:80 exadel/compreface`
```
7. By default, docker won’t restart CompreFace if it fails or after your restart your machine. 
   You can add this by adding `--restart=always` in run command:
```commandline
docker run -d --name=CompreFace -v compreface-db:/var/lib/postgresql/data -p 8000:80 --restart=always exadel/compreface
```
8. If you want to stop CompreFace, run `docker stop CompreFace`.
9. To start stopped Compreface, run `docker start CompreFace`.
10. If you want to restart CompreFace, run `docker restart CompreFace`.
11. If you want to clear CompreFace installation, first stop it with `docker stop CompreFace`.  Remove container with `docker rm CompreFace`. Then delete the volume `docker volume rm compreface-db`. Then run CompreFace again.
12. To update the CompreFace version or change custom build, stop CompreFace with `docker stop CompreFace`. Remove container with `docker rm CompreFace`. Then run the new CompreFace version.
