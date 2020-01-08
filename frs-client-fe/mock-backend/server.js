const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const application = require('./data/application.json');
const organization = require('./data/organization.json');
let mockData = {
  application,
  organization
};

const app = express();
app.use(express.urlencoded());
// Add headers
app.use(function(req, res, next) {
  // Website you wish to allow to connect
  res.setHeader('Access-Control-Allow-Origin', '*');
  // Request methods you wish to allow
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE');
  // Request headers you wish to allow
  res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type, authorization');
  // Set to true if you need the website to include cookies in the requests sent
  // to the API (e.g. in case you use sessions)
  res.setHeader('Access-Control-Allow-Credentials', true);
  // Pass to next layer of middleware
  next();
});
app.use(bodyParser.json());       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
  extended: true
}));

// user with some login:
let user = {
  email: "email",
  username: "username",
  password: "password"
};


// getJSONData();
let token = '';

// view engine setup
app.set('views', 'src/pages');
app.locals.basedir = path.join(__dirname, 'mock-backend');
app.use('/static', express.static('static'));
app.use('/', express.static('public'));


app.get('/', function(req, res) {
  res.redirect('/home');
});

app.post('/login', wait(1000), function(req, res) {
  console.log(req.body, req.query);
  if (req && req.body.username === user.username && req.body.password === user.password) {
    token = `${user.username}_${user.password}_${+new Date()}`;
    res.send({ token });
  }
  else {
    res.sendStatus(401);
  }
});

app.post('/client/register', function(req, res) {
  if (req && req.body.username && req.body.password && req.body.email) {

    // if user already exists:
    if (req.body.username === user.username) return res.sendStatus(400);

    user = { ...user, ...req.body };
    res.status(201).send({ message: 'Created' });
  }
  else {
    res.sendStatus(400);
  }
});

app.get('/organization', auth, function (req, res) {
  const id = req.query.id;
  if (id) {
    res.send(mockData.organization.filter(item => item.id === id))
  } else {
    res.send(mockData.organization);
  }
});

app.post('/organization/:id', auth, function (req, res) {
  const organization = req.body;
  mockData.organization.push(organization);
  res.send(organization);
});

app.put('/organization/:id', auth, function (req, res) {
  const newData = req.body;
  const id = req.params.id;
  let organization = mockData.organization.find(item => item.id === id);
  organization.name = newData.name;
  res.send(organization);
});


app.get('/org/:orgId/apps', auth, (req, res) => {
  const id = req.params.orgId;

  if (id) {
    const data = mockData.application
      .filter(app => app.organizationId === id)
      .map(app => {
        const { organizationId, ...sendData } = app;
        return sendData;
      });
    res.send(data);
  } else {
    res.sendStatus(400);
  }
});

app.post('/org/:orgId/app', auth, (req, res) => {
  const organizationId = req.params.orgId;
  const firstName = req.headers.authorization.split('_')[0];
  const name = req.body.name;

  const app = {
    id: mockData.application.length.toString(),
    name,
    owner: {
      id: 'uniqUserId',
      firstName,
      lastName: 'owner_lastname'
    }
  };

  mockData.application.push({
    ...app,
    organizationId
  });

  res.status(201).json(app);
});

app.listen(3000, function() {
  console.log('Listening on port 3000!');
});

function auth(req, res, next) {
  if (req && req.headers.authorization === token)
    return next();
  else
    return res.sendStatus(401);
}

function wait(time = 1000) {
  return function(req, res, next) {
    setTimeout(() => {
      return next();
    }, time)
  }
}
