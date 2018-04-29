const CONFIG = require('../settings/config');
const HELPER = require('../helpers/helpers');
const DEBUG = require('debug')('iwannasurfapi:ww');
const fetch = require('node-fetch');

module.exports = {
    get: get,
    map: mapping,
    processResponse: response,
    rate: rate
};
function get(configs){
    let params = {
        key: CONFIG.api.worldweather.key,
        q: configs.lat + "," + configs.lon,
        format: "json",
        tide: "yes",
        tp: 1
    };
    let url = HELPER.queryString(CONFIG.api.worldweather.base_endpoint, params);
    DEBUG("WorldWeather: Built URL --> [" + url + "]");
    return fetch(url);
}

function mapping(spots){
    return spots.map( s => {
        let todayData = s.apiSpot.data.weather[1];
        let data = todayData.hourly[new Date().getHours()];
        return {
            dbSpot: s.dbSpot,
            apiSpot: {
                maxTemp: todayData.maxtempC,
                mixTemp: todayData.mintempC,
                data: {
                    swell: {
                        height: data.swellHeight_m,
                        period: data.swellPeriod_secs,
                        direction: data.swellDir,
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

function response(s){
    return s.apiSpot.text();
}

function rate(spot){
    let max = getMaxRating(spot.dbSpot), rat = getRating(spot);
    DEBUG("--------- " + spot.dbSpot.identification.name + " ----------------------");
    DEBUG("Max Rating is " + max);
    DEBUG("Rating is " + rat);
    let res = (1 - rat / max) * 100;
    DEBUG("** Rating is: " + res);
    DEBUG("----------------------------------------------------------");
    return res;
}

function getRating(spot){
    let args = [{val: spot.apiSpot.data.swell.height, interval: spot.dbSpot.swell.height},
        {val: spot.apiSpot.data.swell.period, interval: spot.dbSpot.swell.period},
        {val: spot.apiSpot.data.swell.direction, interval: spot.dbSpot.swell.direction},
        {val: spot.apiSpot.data.wind.speed, interval: spot.dbSpot.wind.speed},
        {val: spot.apiSpot.data.wind.direction, interval: spot.dbSpot.wind.direction}];

    return calcRating(args);

}
function calcRating(data, spot){
    return data.map( d => Math.abs( d.val - getMedian(d.interval)))
        .reduce((accumulator, currentValue) => accumulator + currentValue);

}

function getMedian(interval){
    return (interval.min + interval.max)  / 2;
}



function getMaxRating(dbSpot){
    return calcMaxRating([dbSpot.swell.height, dbSpot.swell.period, dbSpot.swell.direction, dbSpot.wind.speed,dbSpot.wind.direction])
}



function calcMaxRating(values){
    return values.map( val => val.max - getMedian(val))
        .reduce( (accumulator, currentValue) => accumulator + currentValue );
}