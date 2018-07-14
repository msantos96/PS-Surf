package io.surf.wm.iwannasurfapp.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import io.surf.wm.iwannasurfapp.R
import io.surf.wm.iwannasurfapp.adapters.SpotArrayAdapter
import io.surf.wm.iwannasurfapp.model.Dtos.*
import java.util.concurrent.CompletableFuture

class FavoriteFragment : Fragment() {

    var itemClickCb: OnItemClickListener = OnItemClickListener { _, _, _, _ -> }
    var futureSpots: HashMap<String, CompletableFuture<Spot>> = hashMapOf()
    private val spots: ArrayList<Spot> by lazy {
        val spots = ArrayList<Spot>()
        for(spot in futureSpots) spots.add(spot.value.get())
        spots
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.favorite_fragment, container, false)

        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState)

        view.findViewById<ListView>(R.id.favs_list).adapter = SpotArrayAdapter(context, spots)

        view.findViewById<ListView>(R.id.favs_list).onItemClickListener = itemClickCb

        return view
    }

    fun addSpot(spot: Spot) {
        spots.add(spot)
    }

    fun removeSpot(spot: Spot) {
        spots.removeIf { s -> s.dbSpot._id.equals(spot.dbSpot._id) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("FAVS", spots)
    }

    private fun onRestoreInstanceState(inState: Bundle) {
        spots.addAll(inState.getSerializable("FAVS") as ArrayList<Spot>)
    }
}