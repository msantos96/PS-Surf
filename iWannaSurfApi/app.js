let createError = require('http-errors');
let express = require('express');
let path = require('path');
let cookieParser = require('cookie-parser');
let mongoose = require('mongoose');
let indexRouter = require('./routes/index');
let usersRouter = require('./routes/users');
let debug = require('debug')('iwannasurfapi:app');
let model = require('./model/model');
let app = express();
let connectionString = "mongodb://127.0.0.1:27017/test";


mongoose.connect(connectionString).then( () => debug('Nigga We Made It')).catch( err => debug('Mah, Help Me!!' + err));

let wade = new model.Player({name: "Dwyane Wade 2", team: "Miami Heat", age: 35});
wade.save( () => debug("Wade was saved"));

// let db = mongoose.createConnection('localhost', 'iWannaSurf',27017, {user: "edukng623", pass: "Thecarter@3"});
// db.then( res => console.log("Look mah i connected")).catch( err => console.log('Look mah i failed you'));
// view engine setup
// app.set('views', path.join(__dirname, 'views'));
// app.set('view engine', 'jade');

app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
// app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);
app.use('/users', usersRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.end('error');
});

module.exports = app;
