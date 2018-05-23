package io.surf.wm.iwannasurfapp.model

import java.io.Serializable

class Dtos {
    class Spot(val dbSpot: DbSpot, val realTimeData: RealTimeData) : Serializable

    class DbSpot(val _id: String, val rating: Float, val identification: Identification, val swell: SwellInterval, val wind: WindInterval) : Serializable
    class Identification(val lat: Double, val lon: Double, val name: String, val distance: Double, val additionalInfo: AdditionalInfo) : Serializable
    class AdditionalInfo(val crowd: Crowd, val realLat: Double, val realLong: Double, val thumbsUp: Double, val thumbsDown: Double, val rank: Int) : Serializable
    class Crowd(val weekDays: Int, val weekEnds: Int) : Serializable
    class SwellInterval(val height: Interval<Double>, val period: Interval<Double>, val direction: Interval<Int>, val compassDirection: Array<String>) : Serializable
    class WindInterval(val speed: Interval<Int>, val direction: Interval<Int>, val compassDirection: Array<String>) : Serializable
    class Interval<T>(val min: T, val max: T) : Serializable

    class RealTimeData(val swell: Swell, val wind: Wind) : Serializable
    class Swell(val height: Double, val period: Double, val direction: Double) : Serializable
    class Wind(val speed: Double, val direction: Double) : Serializable
}