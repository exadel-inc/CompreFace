const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const applications = require('./data/application.json');
const organizations = require('./data/organization.json');
const users = require('./data/users.json');
const roles = require('./data/roles.json');

const mockData = {
  applications,
  organizations,
  users,
  roles
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
  password: "password",
  firstName: "string",
  guid: "guid_0",
  lastName: "string"
};

let token = '';

app.locals.basedir = path.join(__dirname, 'mock-backend');

app.post('/login', wait(1000), function(req, res) {
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

app.get('/organizations', auth, function (req, res) {
  const id = req.query.id;
  if (id) {
    res.send(mockData.organizations.filter(item => item.id === id))
  } else {
    res.send(mockData.organizations);
  }
});

app.post('/organization', auth, function (req, res) {
  const org = {
    name: req.body.name,
    id: req.body.name+'_guid',
    userOrganizationRoles: [{role: "OWNER", userId: "guid_0"}]
  };
  mockData.organizations.push(org);
  res.send(org);
});

app.post('/organization/:id', auth, function (req, res) {
  const organization = req.body;
  mockData.organizations.push(organization);
  res.send(organization);
});

app.put('/organization/:id', auth, function (req, res) {
  const newData = req.body;
  const id = req.params.id;
  let organization = mockData.organizations.find(item => item.id === id);
  organization.name = newData.name;
  res.send(organization);
});

app.get('/org/:orgId/apps', auth, wait(), (req, res) => {
  const id = req.params.orgId;

  if (id) {
    const data = mockData.applications
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
    id: mockData.applications.length.toString(),
    name,
    owner: {
      id: 'uniqUserId',
      firstName,
      lastName: 'owner_lastname'
    }
  };

  mockData.applications.push({
    ...app,
    organizationId
  });

  res.status(201).json(app);
});

app.get('/org/:orgId/roles', auth, wait(), (req, res) => {
  const organizationId = req.params.orgId;
  res.status(201).json(mockData.users.filter(user => user.organizationId === organizationId));
});

app.post('/org/:orgId/role', auth, (req, res) => {
  const { id, role } = req.body;

  if (id !== undefined && role) {
    const userIndex = mockData.users.findIndex(user => user.id === id);

    if (~userIndex) {
      mockData.users[userIndex].accessLevel = role;

      res.status(201).json(mockData.users[userIndex]);
    } else {
      res.status(404).json({ message: 'user not found' });
    }
  } else {
    res.sendStatus(400);
  }
});

app.post('/org/:orgId/invite', auth, (req, res) => {
  const { role, userEmail } = req.body;

  if (userEmail && role) {
    mockData.users.push({
      id: mockData.users.length,
      firstName: userEmail,
      lastName: userEmail,
      accessLevel: role
    });

    res.status(201).json({ message: 'created' });
  } else {
    res.sendStatus(400);
  }
});

app.get('/roles', auth, (req, res) => {
  res.send(mockData.roles);
});


app.get('/user/me', auth, (req, res) => {
  res.send(user);
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
