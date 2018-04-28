const queryString = require('query-string');
const debug = require('debug')('iwannasurfapi:helpers');
const format = require('string-format');
format.extend(String.prototype, {});

function distanceBetweenCoordinates(lat1, lon1, lat2, lon2) {
    function toRad(value)
    {
        return value * Math.PI / 180;
    }

    const EARTH_RADIUS = 6371;

    let dLat = toRad(lat2-lat1);
    let dLon = toRad(lon2-lon1);
    latitude1 = toRad(lat1);
    latitude2 = toRad(lat2);

    let a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(latitude1) * Math.cos(latitude2);
    let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    let dist =  EARTH_RADIUS * c;
    debug("Distance Between [{0},{1}] and [{2},{3}] is {4}".format(lat1, lon1, lat2,lon2,dist));
    return dist;
}

function buildQueryString(urlString, params){
    let url = urlString + queryString.stringify(params);
    debug("Built QS: " + url);
    return url;
}

module.exports = {
    distanceBetweenCoordinates: distanceBetweenCoordinates,
    queryString: buildQueryString
};