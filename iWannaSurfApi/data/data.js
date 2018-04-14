const fetch = require('node-fetch');
const config = require('../settings/config');
const debug = require('debug')('iwannasurfapi:data');
const mongoose = require('mongoose');
const model = require('../model/model');
const helpers = require('../helpers/helpers');

module.exports = function(dataSource){

    function spotsWithinRange(lat, lon, range){
        return new Promise( function (resolve, reject) {
            model.Spot.find({$where: () => true})
                .then(resolve)
                .catch(reject);
        });
    }

    return {
        getSpotsWithinRange: spotsWithinRange
    }
};
function comparator(options){

    return function(){
        const EARTH_RADIUS = 6371;

        function distanceBetweenCoordinates(lat1, lon1, lat2, lon2) {

            let dLat = toRad(lat2-lat1);
            let dLon = toRad(lon2-lon1);
            lat1 = toRad(lat1);
            lat2 = toRad(lat2);

            let a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            return EARTH_RADIUS * c;
        }
        function toRad(value)
        {
            return value * Math.PI / 180;
        }
        return distanceBetweenCoordinates(options.lat, options.lon, this.identification.lat, this.identification.lon) <= options.radius;
    }
}
/*
fetch('http://api.worldweatheronline.com/premium/v1/weather.ashx?key=0016a118c771436ea66131639180904&q=38.7,-9.4&format=json')
    .then((response) =>  response.text())
    .then( body => res.end(JSON.stringify(body)))
    .catch(function(err){
        debug("Error fetching data: " + err);
    });*/
