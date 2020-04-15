FROM python:3.7

WORKDIR /app/e2e
COPY src src
COPY run-e2e-test.sh .

ENV HOME=/app/e2e

RUN chmod +x run-e2e-test.sh \
&& pip install -U requests pytest pytest-ordering pymongo strenum pylama toolz

CMD ["/srv/run-e2e-test.sh"]
