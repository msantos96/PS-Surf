package io.surf.wm.iwannasurfapp.model

import java.util.Comparator
import io.surf.wm.iwannasurfapp.model.Dtos.Spot
import java.util.function.Supplier

class Settings(val distance: Supplier<Float>, val sortCriteria: Supplier<SortCriteria>) {
    enum class SortCriteria : Comparator<Spot> {
        RATING() {
            override fun compare(o1: Spot, o2: Spot): Int = o1.dbSpot.rating.compareTo(o2.dbSpot.rating)
        }
        /*,DISTANCE() {
            override fun compare(o1: Spot, o2: Spot): Int =
        }*/
        /*,CROWD() {
            override fun compare(o1: Spot, o2: Spot): Int =
        }*/
    }
}