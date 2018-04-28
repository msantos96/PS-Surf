package io.surf.wm.iwannasurfapp.model

class Dtos {
    class Spot(val dbSpot: DbSpot, val apiSpot: ApiSpot)

    class DbSpot(val id: String, val identification: Identification)
    class Identification(val lat: Float, val lon: Float, val name: String)

    class ApiSpot(val maxTemp: Int, val minTemp: Int, val data: Data)
    class Data(val swell: Swell, val wind: Wind, val weather: Weather)
    class Swell(val height: Float, val period: Float, val direction: String, val compassDirection: String)
    class Wind(val speed: Int, val direction: Int, val compassDirection: String)
    class Weather(val desc: Array<Desc>, val precipitation: Float, val humidity: Int, val feelsLike: Int, val windChill: Int, val windGusts: Int, val waterTemp: Int)
    class Desc(val value: String)
}