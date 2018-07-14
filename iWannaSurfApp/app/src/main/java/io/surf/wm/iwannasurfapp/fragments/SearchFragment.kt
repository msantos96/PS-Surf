package io.surf.wm.iwannasurfapp.fragments

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.ProgressBar
import io.surf.wm.iwannasurfapp.R
import io.surf.wm.iwannasurfapp.adapters.SpotArrayAdapter
import io.surf.wm.iwannasurfapp.model.Dtos.Spot

class SearchFragment : Fragment() {

    var searchCb: OnClickListener = OnClickListener { }
    var itemClickCb: OnItemClickListener = OnItemClickListener { _, _, _, _ -> }
    private val spots: ArrayList<Spot> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.search_fragment, container, false)

        if(savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState)

        view.findViewById<ListView>(R.id.spots_list).adapter = SpotArrayAdapter(context, spots)

        view.findViewById<ListView>(R.id.spots_list).onItemClickListener = itemClickCb

        view.findViewById<FloatingActionButton>(R.id.spot_search).setOnClickListener(searchCb)

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("SPOTS", spots)
    }

    private fun onRestoreInstanceState(inState: Bundle) {
        spots.addAll(inState.getSerializable("SPOTS") as ArrayList<Spot>)
    }

    fun progressIsVisible(visibility: Boolean) {
        this.view!!.findViewById<ProgressBar>(R.id.prog_bar).visibility = if(visibility) ProgressBar.VISIBLE else ProgressBar.INVISIBLE
    }

    fun buttonIsEnabled(enabled: Boolean) {
        this.view!!.findViewById<FloatingActionButton>(R.id.spot_search).isEnabled = enabled
    }

    fun clearSpots() = (this.view!!.findViewById<ListView>(R.id.spots_list).adapter as SpotArrayAdapter).clear()

    fun addSpots(spots: List<Spot>) = (this.view!!.findViewById<ListView>(R.id.spots_list).adapter as SpotArrayAdapter).addAll(spots)
}