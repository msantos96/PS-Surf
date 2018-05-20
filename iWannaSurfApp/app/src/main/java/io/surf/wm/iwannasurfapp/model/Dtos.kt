package io.surf.wm.iwannasurfapp.model

import java.io.Serializable

class Dtos {
    class Spot(val dbSpot: DbSpot, val apiSpot: ApiSpot) : Serializable

    class DbSpot(val _id: String, val rating: Float, val identification: Identification, val swell: SwellInterval, val wind: WindInterval) : Serializable
    class Identification(val lat: Double, val lon: Double, val name: String, val thumbsUp: Double, val thumbsDown: Double) : Serializable
    class SwellInterval(val height: Interval<Double>, val period: Interval<Double>, val direction: Interval<Int>, val compassDirection: Array<String>) : Serializable
    class WindInterval(val speed: Interval<Int>, val direction: Interval<Int>, val compassDirection: Array<String>) : Serializable
    class Interval<T>(val min: T, val max: T) : Serializable

    class ApiSpot(val maxTemp: Int, val minTemp: Int, val data: Data) : Serializable
    class Data(val swell: Swell, val wind: Wind, val weather: Weather) : Serializable
    class Swell(val height: Double, val period: Double, val direction: Int, val compassDirection: String) : Serializable
    class Wind(val speed: Int, val direction: Int, val compassDirection: String) : Serializable
    class Weather(val desc: Array<Desc>, val precipitation: Float, val humidity: Int, val feelsLike: Int, val windChill: Int, val windGusts: Int, val waterTemp: Int) : Serializable
    class Desc(val value: String) : Serializable
}