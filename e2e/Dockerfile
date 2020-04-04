FROM python:3.7
WORKDIR /srv

## Copy sources
COPY src src
COPY run-e2e-test.sh .

## Prepare environment
RUN chmod +x run-e2e-test.sh
RUN pip install -U requests pytest pytest-ordering pymongo strenum pylama toolz

## Runtime
CMD ["/srv/run-e2e-test.sh"]
