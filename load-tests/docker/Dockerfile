FROM alpine:3.13.4

ENV K6_HOME="/data/k6" \
#   run all tests by default
    TESTS="" \
    VUS="1" \
    ITERATIONS="1" \
    DURATION="2m" \
    HOSTNAME="http://localhost:8000" \
    INFLUXDB_HOSTNAME="http://localhost:8086" \
    DB_CONNECTION_STRING="user=postgres password=postgres port=5432 dbname=frs host=localhost sslmode=disable"

RUN mkdir -p ${K6_HOME}
WORKDIR	${K6_HOME}
ADD . .
RUN chmod +x ./entrypoint.sh \
    && chmod +x ./k6

ENTRYPOINT ["./entrypoint.sh"]
