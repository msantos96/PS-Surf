package io.surf.wm.iwannasurfapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.TextView
import io.surf.wm.iwannasurfapp.R
import io.surf.wm.iwannasurfapp.model.Dtos.*

class SpotArrayAdapter(context: Context?) : ArrayAdapter<Spot>(context, R.layout.spot_preview) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = if(convertView == null) LayoutInflater.from(context).inflate(R.layout.spot_preview, null) else convertView

        val spot: Spot = getItem(position)

        view.findViewById<TextView>(R.id.fld_name_value).text = spot.dbSpot.identification.name
        view.findViewById<TextView>(R.id.fld_temperature_value).text = String.format("[ %d - %d ]", spot.apiSpot.minTemp, spot.apiSpot.maxTemp)
        view.findViewById<TextView>(R.id.fld_height_value).text = spot.apiSpot.data.swell.height.toString()
        view.findViewById<TextView>(R.id.fld_period_value).text = spot.apiSpot.data.swell.period.toString()
        view.findViewById<RatingBar>(R.id.fld_rating_bar).numStars = 5
        view.findViewById<RatingBar>(R.id.fld_rating_bar).rating = 4f //TODO: Algorithm value

        return view
    }

    override fun addAll(spots: Array<Spot>) {
        spots.forEach(this::add)
    }
}