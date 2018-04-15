const fetch = require('node-fetch');
const express = require('express');
const config = require('../settings/config');
const helper = require('../helpers/helpers');
const router = express.Router();
const debug = require('debug')('iwannasurfapi:suggester');
const data = require('../data/data')(dataProvider);


function dataProvider(options){
    return fetch(helper.queryString(config.api.worldweather, options));
}
function rate(spots){

    return spots;
}

router.get('/', function(req, res, next) {
    data.getSpotsWithinRange(req.query.lat, req.query.lon, req.query.range)
        .then(data.rateSpots)
        .then( spots => res.end(JSON.stringify(spots)))
        .catch(err => {
            res.end(err.toString());
        })
});

module.exports = router;
