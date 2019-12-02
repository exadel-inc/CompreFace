const express = require('express');
const path = require('path');
const fs = require('fs');
const dataPath = './mock-backend/data/';
let mockData = {};


const app = express();
app.use(express.urlencoded());
// user with some login:
const user = {
  email: "email",
  username: "username",
  password: "password"
};


collectData();
let token = '';

// view engine setup
app.set('views', 'src/pages');
app.locals.basedir = path.join(__dirname, 'mock-backend');
app.use('/static', express.static('static'));
app.use('/', express.static('public'));


app.get('/', function (req, res) {
  res.redirect('/home');
});

app.post('/admin/oauth/token', function (req, res) {
  if (req && req.query.email === user.email && req.query.password) {
    token = `${user.email}${user.password}${+new Date()}`;
    res.send({token});
  }
  else{
    res.sendStatus(401);
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

function collectData() {
  let  organization;
  let  apps;
  try {
    organization = JSON.parse(fs.readFileSync(`${dataPath}organization.json`, 'utf8'));
    apps = JSON.parse(fs.readFileSync(`${dataPath}apps.json`, 'utf8'));
  } catch (e) {
    organization = [];
    apps = [];
  }
  //...
console.log(organization);
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
};
