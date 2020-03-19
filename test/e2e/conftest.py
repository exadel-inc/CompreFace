def pytest_addoption(parser):
    # Run E2E against this host, default value: http://localhost:3000
    parser.addoption('--host', action='store', dest='host', default='http://localhost:3000')
    # Drop and reinitialize database before E2E test
    parser.addoption('--drop-db', action="store_true", dest='drop-db', default=False)
