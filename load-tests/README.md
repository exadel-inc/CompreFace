# K6 load tests for CompreFace services  
Each folder inside `tests` folder contains a separate loading test.
To run tests, first you need to build an image:
```
cd ./docker
docker build -t k6tests .
```

Then you can run all tests or define a list of tests (as TESTS env variable):
```
# run only face_verify and recognize tests
docker run \
    -e TESTS="face_verify;recognize" \ 
    -e HOSTNAME="http://myhost:8082" \
    -e INFLUXDB_HOSTNAME="http://myinfluxdbhost:8086"
    -e DB_CONNECTION_STRING="user=postgres password=postgres port=5432 dbname=frs host=mydbhost sslmode=disable" \
    k6tests
```
```
# run all tests
docker run \ 
    -e HOSTNAME="http://myhost:8082" \
    -e INFLUXDB_HOSTNAME="http://myinfluxdbhost:8086"
    -e DB_CONNECTION_STRING="user=postgres password=postgres port=5432 dbname=frs host=mydbhost sslmode=disable" \
    k6tests
```

Any test from `tests` folder follows those steps:
1. Apply db_init.sql to database
2. Run recognition test according to `scenarios` defined in the script
3. Apply db_truncate.sql to database


### Run command details
```
docker run 
    --env IMAGES="./faces/FACE_512KB.jpg;./faces/FACE_1024KB.jpg"
    --env HOSTNAME="<host>"
    --env INFLUXDB_HOSTNAME="<influxdb_host>"
    --env DB_CONNECTION_STRING="user=postgres password=<password> port=5432 dbname=frs host=<db_host> sslmode=disable" 
    <image_id>
``` 
`IMAGES` list of images fot test (if images are needed for the test)   
`HOSTNAME` hostname of test server  
`INFLUXDB_HOSTNAME` hostname of influxdb  
`DB_CONNECTION_STRING` DB connection string, template is *"user=mydbuser password=mydbpass port=5432 dbname=mydbname host=mydbhost sslmode=disable"*
