const express = require('express');
const config = require('../settings/config');
const router = express.Router();
const debug = require('debug')('iwannasurfapi:suggester');
const data = require('../data/data')(dataProvider);
const api = require('../data/api')(config.api[process.env.API] || config.api.default);


function dataProvider(options){
    return api.get(options);
}
function rate(spots){
    return spots;
}

function getResponseBodies(spots){
    let promises = spots.map( s => {
        return new Promise(function (resolve, reject) {
            api.processResponse(s)
                .then( body => {
                    resolve( {dbSpot: s.dbSpot, apiSpot: JSON.parse(body)})
                })
                .catch(reject)
        })
    });
    return Promise.all(promises);
}

function mapToEntity(spots) {
    return api.map(spots);
}

router.get('/', function(req, res, next) {
    data.getSpotsWithinRange(req.query.lat, req.query.lon, req.query.range)
        .then(data.getSpots)
        .then(getResponseBodies)
        .then(mapToEntity)
        .then(rate)
        .then( spots => {
            res.end(JSON.stringify(spots))
        })
        .catch(err => {
            res.end(err.toString());
        })
});

module.exports = router;
