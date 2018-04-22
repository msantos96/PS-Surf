const CONFIG = require('../settings/config');
const HELPER = require('../helpers/helpers');
const DEBUG = require('debug')('iwannasurfapi:suggester');
const API_MAPPINGS = {
    "worldweather": worldWeather
};
module.exports = {
    apiBuilder: apiBuilder
};

function apiBuilder(apiName){
    return function(configs){
        return API_MAPPINGS[apiName](configs);
    }
}

function worldWeather(configs){
    let params = {
        key: CONFIG.api.worldweather.key,
        q: configs.lat + "," + configs.lon,
        format: "json"
    };
    let url = HELPER.queryString(CONFIG.api.worldweather.base_endpoint, params);
    DEBUG("Built URL --> [" + url + "]");
    return url;
}