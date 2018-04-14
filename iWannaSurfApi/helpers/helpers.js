

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
module.exports = {
    distanceBetweenCoordinates: distanceBetweenCoordinates
};