const queryString = require('query-string');



function distanceBetweenCoordinates(lat1, lon1, lat2, lon2) {
    function toRad(value)
    {
        return value * Math.PI / 180;
    }

    const EARTH_RADIUS = 6371;

    let dLat = toRad(lat2-lat1);
    let dLon = toRad(lon2-lon1);
    lat1 = toRad(lat1);
    lat2 = toRad(lat2);

    let a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
    let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return EARTH_RADIUS * c;
}

function buildQueryString(urlString, params){
    let retUrl = urlString + queryString.stringify(params);
    return retUrl;
}

module.exports = {
    distanceBetweenCoordinates: distanceBetweenCoordinates,
    queryString: buildQueryString
};