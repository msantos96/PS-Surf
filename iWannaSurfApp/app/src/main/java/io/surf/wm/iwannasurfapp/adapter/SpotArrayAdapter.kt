package io.surf.wm.iwannasurfapp.adapter

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import io.surf.wm.iwannasurfapp.R
import io.surf.wm.iwannasurfapp.model.Dtos.*

class SpotArrayAdapter(context: Context?, spots: ArrayList<Spot>) : ArrayAdapter<Spot>(context, R.layout.spot_preview, spots) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView?:LayoutInflater.from(context).inflate(R.layout.spot_preview, null)

        val spot: Spot = getItem(position)

        view.findViewById<TextView>(R.id.spot_name).text = spot.dbSpot.identification.name

        view.findViewById<TextView>(R.id.spot_distance).text = String.format("%.2f", spot.dbSpot.identification.distance)

        view.findViewById<TextView>(R.id.spot_crowd).text =
                if(Calendar.getInstance(ULocale("en_GB@calendar=gregorian")).isWeekend)
                    spot.dbSpot.additionalInfo.crowd.weekEnds.toString()
                else spot.dbSpot.additionalInfo.crowd.weekDays.toString()

        view.findViewById<TextView>(R.id.spot_rating).text = spot.dbSpot.rating.toString()

        return view
    }
}