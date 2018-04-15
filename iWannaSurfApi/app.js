const createError = require('http-errors');
const express = require('express');
const cookieParser = require('cookie-parser');
const mongoose = require('mongoose');
const config = require('./settings/config');
const spotSuggester = require('./routes/suggester');

const debug = require('debug')('iwannasurfapi:app');
const model = require('./model/model');
const app = express();
const connectionString = config.databases[process.env.SYSTEM || 'dev'];

mongoose.connect(connectionString).then( () => debug('Nigga We Made It')).catch( err => debug('Mah, Help Me!!' + err));
//
let wade = new model.Player({name: "Dwyane Wade 2", team: "Miami Heat", age: 35});
wade.save( () => debug("Wade was saved"));

// mongoose.set('debug', function (coll, method, query, doc,opts) {
//     //do your thing
//     debugger;
// });

app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.use('/spots/suggest', spotSuggester);

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
