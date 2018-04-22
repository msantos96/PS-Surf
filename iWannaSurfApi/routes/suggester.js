const fetch = require('node-fetch');
const express = require('express');
const config = require('../settings/config');
const helper = require('../helpers/helpers');
const router = express.Router();
const debug = require('debug')('iwannasurfapi:suggester');
const data = require('../data/data')(dataProvider);
const api = require('../data/api');



function dataProvider(options){
    let url = api.apiBuilder(config.api[process.env.API] || "worldweather")(options);
    return fetch(url);
}
function rate(spots){
    return spots;
}

function getResponseBodies(spots){
    let promises = spots.map( s => {
        return new Promise(function (resolve, reject) {
            s.apiSpot.text().then( body => resolve( {dbSpot: s.dbSpot, apiSpot: JSON.parse(body)} ))
        })
    });
    return Promise.all(promises);
}

function mapToEntity(spots) {
    return spots.map( s => {
        let todayData = s.apiSpot.data.weather[1];
        let data = todayData.hourly[todayData.hourly.length - 1]; // to be determined according to current hour
        return {
            dbSpot: s.dbSpot,
            apiSpot: {
                maxTemp: todayData.maxtempC,
                mixTemp: todayData.mintempC,
                data: {
                    swell: {
                        height: data.swellHeight_m,
                        period: data.swellPeriod_secs,
                        direction: data.swellDir16Point,
                        compassDirection: data.swellDir16Point
                    },
                    wind: {
                        speed: data.windspeedKmph,
                        direction: data.winddirDegree,
                        compassDirection: data.winddir16Point
                    },
                    weather: {
                        desc: data.weatherDesc,
                        precipitation: data.precipMM,
                        humidity: data.humidity,
                        feelsLike: data.FeelsLikeC,
                        windChill: data.WindChillC,
                        windGusts: data.WindGustKmph,
                        waterTemp: data.waterTemp_C
                    }
                }
            }
        }
    })
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
