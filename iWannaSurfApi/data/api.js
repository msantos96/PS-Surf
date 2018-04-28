const worldWeatherApi = require("./worldweather");

const API_MAPPINGS = {
    "worldweather": worldWeatherApi
};
module.exports = function(name){
    return API_MAPPINGS[name];
};

