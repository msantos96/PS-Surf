const CONFIG = require('../settings/config');
const HELPER = require('../helpers/helpers');
const DEBUG = require('debug')('iwannasurfapi:suggester');
const fetch = require('node-fetch');

module.exports = {
    get: get,
    map: mapping,
    processResponse: response
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