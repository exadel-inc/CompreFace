def pytest_addoption(parser):
    parser.addoption('--host', action='store', dest='host', default='http://localhost:5001')
