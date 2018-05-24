package io.surf.wm.iwannasurfapp.model

import android.icu.util.Calendar
import android.icu.util.ULocale
import java.util.Comparator
import io.surf.wm.iwannasurfapp.model.Dtos.Spot
import java.util.function.Supplier

class Settings(val distance: Supplier<Float>, val sortCriteria: Supplier<SortCriteria>) {
    enum class SortCriteria : Comparator<Spot> {
        RATING() {
            override fun compare(o1: Spot, o2: Spot): Int = o1.dbSpot.rating.compareTo(o2.dbSpot.rating)
        }
        ,DISTANCE() {
            override fun compare(o1: Spot, o2: Spot): Int = o1.dbSpot.identification.distance.compareTo(o2.dbSpot.identification.distance)
        }
        ,CROWD() {
            override fun compare(o1: Spot, o2: Spot): Int {
                return if(Calendar.getInstance(ULocale("en_GB@calendar=gregorian")).isWeekend)
                    o1.dbSpot.additionalInfo.crowd.weekEnds.compareTo(o2.dbSpot.additionalInfo.crowd.weekEnds)
                else
                    o1.dbSpot.additionalInfo.crowd.weekDays.compareTo(o2.dbSpot.additionalInfo.crowd.weekDays)
            }
        }
    }
}