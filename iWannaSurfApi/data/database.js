const mongoose = require('mongoose');
const config = require("../settings/config");
const debug = require('debug')('iwannasurfapi:app');
const format = require('string-format');
format.extend(String.prototype, {});

module.exports = {
    connect: connect
};

function connect(){
    mongoose.connect(getConnectionString())
        .then( () => debug('Connection Done'))
        .catch( err => debug('Connection failed!!' + err));
}

function getConnectionString(){
    let connectionString = "";
    switch (process.env.SYSTEM || 'dev'){
        case 'dev': {
            connectionString = config.databases['dev'];
            return connectionString;
        }
        case 'prd': {
            connectionString = config.databases['prd'];
            return connectionString.format(process.env.user, process.env.pw);
        }
    }
}