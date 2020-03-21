FROM python:3.7
WORKDIR /srv

## Copy sources
COPY src src
COPY requirements.txt .
COPY run-e2e-test.sh .

## Prepare environment
RUN chmod +x run-e2e-test.sh
RUN pip3 --no-cache-dir install -r requirements.txt

## Entrypoint
ENTRYPOINT ["/srv/run-e2e-test.sh", "http://localhost:3000"]

COPY src src
COPY requirements.txt .
COPY run-e2e-test.sh .

## Prepare environment
RUN chmod +x run-e2e-test.sh
RUN pip3 --no-cache-dir install -r requirements.txt

## Entrypoint
ENTRYPOINT ["/srv/run-e2e-test.sh", "http://ml:3000"]
