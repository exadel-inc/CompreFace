const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const applications = require('./data/application.json');
const organizations = require('./data/organization.json');
const users = require('./data/users.json');
const roles = require('./data/roles.json');
const models = require('./data/models.json');
const appUsers = require('./data/appUsers.json');
const formidable = require('formidable');

const mockData = {
  applications,
  organizations,
  users,
  roles,
  models,
  appUsers
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
  email: "q@q.com",
  password: "password",
  firstName: "some firstName",
  guid: "guid_0",
  lastName: "some lastName",
  avatar: './assets/img/avatar.jpg'
};

let timeStamp = 0;
const refreshTokenTime = 1 * 10 * 1000; // minute sec ms

let access_token = '';
let refresh_token = '';

app.locals.basedir = path.join(__dirname, 'mock-backend');

app.post('/oauth/token', wait(1000), function(req, res) {
  timeStamp = +new Date();
  const form = formidable.IncomingForm();

  form.parse(req, (err, fields) => {
    if (req && fields.username === user.email && fields.password === user.password && fields.grant_type === 'password') {

      access_token = `${user.email}_${user.password}_${user.guid}_${+new Date()}`;
      refresh_token = `refresh_${user.email}_${user.password}_${user.guid}_${+new Date()}`;
      res.send({ access_token, refresh_token });

    } else if (req && fields.refresh_token && fields.grant_type === 'refresh_token') {

      access_token = `${user.email}_${user.password}_${user.guid}_${+new Date()}`;
      refresh_token = `refresh_${user.email}_${user.password}_${user.guid}_${+new Date()}`;
      res.send({ access_token, refresh_token });
    }
    else {
      res.sendStatus(401);
    }
  })
});

app.post('/user/register', function(req, res) {
  if (req && req.body.firstName && req.body.password && req.body.email) {

    // if user already exists:
    if (req.body.email === user.email) return res.sendStatus(400);

    user = { ...user, ...req.body };
    res.status(201).send({ message: 'Created' });
  }
  else {
    res.sendStatus(400);
  }
});

app.get('/orgs', auth, function (req, res) {
  const id = req.query.id;
  if (id) {
    res.send(mockData.organizations.filter(item => item.id === id))
  } else {
    res.send(mockData.organizations);
  }
});

app.post('/org', auth, function (req, res) {
  const org = {
    name: req.body.name,
    id: req.body.name + '_guid',
    role: 'OWNER'
  };
  mockData.organizations.push(org);
  res.send(org);
});

app.post('/org/:id', auth, function (req, res) {
  const organization = req.body;
  mockData.organizations.push(organization);
  res.send(organization);
});

app.put('/org/:id', auth, function (req, res) {
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
  const [firstName, password, id] = req.headers.authorization.replace('Bearer ', '').split('_');
  const name = req.body.name;

  const app = {
    id: mockData.applications.length.toString(),
    name,
    owner: {
      id,
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

app.put('/org/:orgId/app/:appId', auth, (req, res) => {
  const appId = req.params.appId;
  const newData = req.body;

  let app = mockData.applications.find(app => app.id === appId);
  app.name = newData.name;
  res.send(app);
});

app.get('/org/:orgId/roles', auth, wait(), (req, res) => {
  const organizationId = req.params.orgId;
  res.status(201).json(mockData.users.filter(user => user.organizationId === organizationId));
});

app.put('/org/:orgId/role', auth, wait(), (req, res) => {
  const orgId = req.params.orgId;
  const { id, role } = req.body;

  if (id !== undefined && role) {
    const userIndex = mockData.users.findIndex(user => user.id === id);

    if (~userIndex) {
      if (role === 'OWNER') {
        const currentOrganization = mockData.organizations.find(org => org.id === orgId);
        if (currentOrganization.role === 'OWNER') {
          mockData.users[userIndex].accessLevel = role;
          currentOrganization.role = 'ADMIN';
          res.status(200).json(mockData.users[userIndex]);
        } else {
          res.status(403).json({ message: 'forbidden' });
        }
      } else {
        mockData.users[userIndex].accessLevel = role;
        res.status(200).json(mockData.users[userIndex]);
      }
    } else {
      res.status(404).json({ message: 'user not found' });
    }
  } else {
    res.sendStatus(400);
  }
});

app.put('/org/:orgId/invite', auth, (req, res) => {
  const organizationId = req.params.orgId;
  const { userEmail } = req.body;

  if (userEmail) {
    mockData.users.push({
      id: mockData.users.length,
      organizationId: organizationId,
      firstName: userEmail,
      lastName: userEmail,
      accessLevel: 'USER'
    });

    res.status(201).json({ message: 'created' });
  } else {
    res.sendStatus(400);
  }
});

app.put('/org/:orgId/app/:appId/invite', auth, (req, res) => {
  const appId = req.params.appId;
  const { userEmail } = req.body;

  if (userEmail) {
    mockData.appUsers.push({
      id: mockData.appUsers.length,
      applicationId: appId,
      firstName: userEmail,
      lastName: userEmail,
      accessLevel: 'USER'
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

app.get('/org/:orgId/app/:appId/models', auth, wait(), (req, res) => {
  const appId = req.params.appId;
  const models = mockData.models.filter(model => model.relations.find(rel => rel.id === appId));

  res.send(models);
});

app.post('/org/:orgId/app/:appId/model', auth, wait(), (req, res) => {
  const { name } = req.body;
  const { appId, orgId } = req.params;

  if (isModelAccessAllowed(appId, orgId)) {
    const [firstName, password, id] = req.headers.authorization.replace('Bearer ', '').split('_');
    const newModel = {
      id: mockData.models.length,
      name,
      owner: {
        firstName
      },
      accessLevel: 'OWNER',
      relations: [{
        id: appId,
        shareMode: "READONLY"
      }]
    };

    mockData.models.push(newModel);

    res.status(201).send(newModel);
  } else {
    res.status(404).json({ message: 'Organization or Application wasnt found' });
  }
});

app.put('/org/:orgId/app/:appId/model/:modeId', auth, wait(), (req, res) => {
  const { modeId } = req.params;
  const newData = req.body;

  let model = mockData.models.find(model => model.id === modeId);
  model.name = newData.name;
  res.send(model);
});

app.get('/org/:orgId/app/:appId/roles', auth, wait(), (req, res) => {
  const { appId } = req.params;
  const appUsers = mockData.appUsers.filter(appUser => appUser.applicationId === appId);
  res.send(appUsers);
});

app.put('/org/:orgId/app/:appId/role', auth, wait(), (req, res) => {
  const { id, role } = req.body;
  const { appId } = req.params;
  const appUsers = mockData.appUsers.filter(appUser => appUser.applicationId === appId);
  const user = appUsers.find(appUser => appUser.id === id);

  if (user) {
    if (role === 'OWNER') {
      const currentApplication = mockData.applications.find(app => app.id === appId);
      if (currentApplication.role === 'OWNER') {
        user.accessLevel = role;
        currentApplication.role = 'ADMIN';
        res.status(200).json(user);
      } else {
        res.status(403).json({ message: 'forbidden' });
      }
    } else {
      user.accessLevel = role;
      res.status(200).json(user);
    }
  } else {
    res.status(404).json({ message: 'user not found' });
  }
});

app.get('/org/:orgId/app/:appId/models/:modelId', auth, wait(), (req, res) => {
  const modelId = req.params.modelId;
  const models = mockData.models.filter(model => model.id === modelId);
  res.send(models);
});

app.get('/org/:orgId/app/:appId/model/:modelId/apps', auth, wait(), (req, res) => {
  const modelId = req.params.modelId;
  const model = mockData.models.find(model => model.id === modelId);

  if (model) {
    const applications = mockData.applications
      .map(
        app => {
          const relation = model.relations.find(rel => rel.id === app.id);
          let res = null;

          if (relation) {
            res = { ...app, shareMode: relation.shareMode };
          }

          return res;
        })
      .filter(Boolean);

      res.send(applications);
  } else {
    res.status(404).send({ message: 'model wasnt found' });
  }
});

app.put('/org/:orgId/app/:appId/model/:modelId/app', auth, wait(), (req, res) => {
  const modelId = req.params.modelId;
  const relationId = req.body.id;
  const shareMode = req.body.shareMode;
  const model = mockData.models.find(model => model.id === modelId);
  let relation = null;

  if (model) {
    relation = model.relations.find(rel => rel.id === relationId);

    if(relation) {
      relation.shareMode = shareMode;
    }
  }

  if (relation) {
    res.send(relation);
  } else {
    res.status(404).send({ message: 'relation wasnt found' });
  }
});

app.listen(3000, function() {
  console.log('Listening on port 3000!');
});

function isModelAccessAllowed(appId, orgId) {
  const application = mockData.applications.find(app => app.id === appId);
  const organization = mockData.organizations.find(org => org.id === application.organizationId);

  return application && organization && organization.id === orgId;
}

function auth(req, res, next) {
  if (req && req.headers.authorization === `Bearer ${access_token}`){
    if((+new Date() - refreshTokenTime) > timeStamp) {
      return res.status(401).send({message: 'Error during authentication'});
    } else {
      return next();
    }
  }
  return res.status(401).send({message: 'Invalid basic authentication token'});
}

function wait(time = 1000) {
  return function(req, res, next) {
    setTimeout(() => {
      return next();
    }, time)
  }
}
