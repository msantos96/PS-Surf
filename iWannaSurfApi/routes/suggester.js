const fetch = require('node-fetch');
const config = require('../settings/config');
const express = require('express');
const router = express.Router();
const debug = require('debug')('iwannasurfapi:suggester');
const data = require('../data/data')("Not Yet");


router.get('/', function(req, res, next) {
    data.getSpotsWithinRange(req.query.lat, req.query.lon, req.query.range)
        .then( spots => {
            res.end(JSON.stringify(spots));
        })
        .catch(err => {
            res.end(err);
        })
});

module.exports = router;
