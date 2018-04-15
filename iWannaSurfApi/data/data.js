const fetch = require('node-fetch');
const config = require('../settings/config');
const debug = require('debug')('iwannasurfapi:data');
const mongoose = require('mongoose');
const model = require('../model/model');
const helpers = require('../helpers/helpers');

module.exports = function(dataSource){
    /**
     * This function retrieves all documents of the Spot collection, and only then filters them to restrict to a certain radius,
     * that is it will only return the documents that are within a radius in KM, based on the received latitude, longitude and range values.
     * This could be inefficient, and the find method of Model supports a $where, that is a function, but it would be the same, since
     * this function would be applied to every document in the MongoDB collection.
     * @param {Number} lat Latitude value from the origin location.
     * @param {Number} lon Longitude value from the origin location.
     * @param {Number} range Value in KM to be used as the radius.
     * @returns {Promise<any>} Promise with every spot conforming to the restrictions.
     */
    function spotsWithinRange(lat, lon, range){
        return new Promise( function (resolve, reject) {
            model.Spot.find({})
                .then( allSpots => spotsWithin(allSpots, {lat: lat, lon: lon, radius: range}))
                .then(resolve)
                .catch(reject);
        });
    }

    function rate(spots){
        return new Promise(function(resolve,reject){
            Promise.all(spotsToPromise(spots, dataSource)).then(resolve)
                .catch(reject)
        });
    }
    return {
        getSpotsWithinRange: spotsWithinRange,
        rateSpots: rate
    }
};

function spotsToPromise(spots, dataSource) {
    return spots.map( spot => new Promise(function (resolve, reject) {
        dataSource({lat: spot.lat, lon: spot.lon}).then(resolve).catch(reject)
    }))
}
function spotsWithin(allSpots, options,resolver){
    return allSpots.map( spot => spot._doc).filter( spot => radiusFilter(options, spot));
}
function radiusFilter(options, spot){
    return helpers.distanceBetweenCoordinates(options.lat, options.lon, spot.identification.lat, spot.identification.lon) <= options.radius;
}

/*
fetch('http://api.worldweatheronline.com/premium/v1/weather.ashx?key=0016a118c771436ea66131639180904&q=38.7,-9.4&format=json')
    .then((response) =>  response.text())
    .then( body => res.end(JSON.stringify(body)))
    .catch(function(err){
        debug("Error fetching data: " + err);
    });*/
