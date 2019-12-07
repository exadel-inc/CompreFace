const express = require('express');
const path = require('path');
const fs = require('fs');
const bodyParser = require('body-parser');
const dataPath = './mock-backend/data/';
let mockData = {};


const app = express();
app.use(express.urlencoded());
// Add headers
app.use(function (req, res, next) {
  // Website you wish to allow to connect
  res.setHeader('Access-Control-Allow-Origin', 'http://localhost:4201');
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
app.use( bodyParser.json() );       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
  extended: true
}));

// user with some login:
let user = {
  email: "email",
  username: "username",
  password: "password"
};


getJSONData();
let token = '';

// view engine setup
app.set('views', 'src/pages');
app.locals.basedir = path.join(__dirname, 'mock-backend');
app.use('/static', express.static('static'));
app.use('/', express.static('public'));


app.get('/', function (req, res) {
  res.redirect('/home');
});

app.post('/admin/oauth/token', wait(2000), function (req, res) {
  console.log(req.body);
  if (req && req.body.username === user.username && req.body.password === user.password) {
    token = `${user.username}${user.password}${+new Date()}`;
    res.send({token});
  }
  else{
    res.sendStatus(401);
  }
});

app.post('/admin/client/register', function (req, res) {
  console.log(req.body);
  if (req && req.body.username && req.body.password && req.body.email) {

    // if user already exists:
    if(req.body.username === user.username) return res.sendStatus(400);

    user = { ...user, ...req.body};
    res.status(201).send({message: 'Created'});
  }
  else{
    res.sendStatus(400);
  }
});

app.get('/organization', auth, function (req, res) {
  const id = +req.query.id;
  if(id) {
    res.send(mockData.organization.filter(item => item.id === id))
  } else {
    res.send(mockData.organization);
  }
});

app.listen(3000, function () {
  console.log('Listening on port 3000!');

});

function getJSONData() {
  let  organization;
  let  apps;
  try {
    organization = JSON.parse(fs.readFileSync(`${dataPath}organization.json`, 'utf8'));
    apps = JSON.parse(fs.readFileSync(`${dataPath}apps.json`, 'utf8'));
  } catch (e) {
    organization = [];
    apps = [];
  }

  mockData = {
    organization,
    apps
  }
}

function auth(req, res, next) {
  if (req && req.headers.token === token)
    return next();
  else
    return res.sendStatus(401);
}

function wait(time = 1000) {
  return function (req, res, next) {
    setTimeout(() => {
      return next();
    }, time)
  }
}
