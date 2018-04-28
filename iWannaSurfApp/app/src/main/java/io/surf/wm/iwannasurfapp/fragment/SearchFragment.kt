package io.surf.wm.iwannasurfapp.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import io.surf.wm.iwannasurfapp.R
import io.surf.wm.iwannasurfapp.adapter.SpotArrayAdapter
import io.surf.wm.iwannasurfapp.model.Dtos.*

class SearchFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.search_fragment, container, false)

        val adapter  = SpotArrayAdapter(context)
        view.findViewById<ListView>(R.id.spot_list).adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.spot_search).setOnClickListener {
            val gson = Gson()
            val queue = Volley.newRequestQueue(context)
            val url = "/suggest?lat={latitue}&lon={longitude}&radius={radius_in_km}"

            /*queue.add(
                StringRequest(Request.Method.GET, url,
                    Response.Listener { response ->
                        adapter.addAll(gson.fromJson(test_response, Array<Spot>::class.java))
                    },
                    Response.ErrorListener { error ->
                        //TODO: error
                    }
                )
            )*/

            //TODO: Remove
            val test_response = "[{\"dbSpot\":{\"_id\":\"5ad2067e0ada8d1064d163a7\",\"identification\":{\"lat\":\"39.67\",\"lon\":\"-9.34\",\"name\":\"Praia de Carcavelos\"}},\"apiSpot\":{\"maxTemp\":\"24\",\"mixTemp\":\"12\",\"data\":{\"swell\":{\"height\":\"1.2\",\"period\":\"7.0\",\"direction\":\"WNW\",\"compassDirection\":\"WNW\"},\"wind\":{\"speed\":\"23\",\"direction\":\"347\",\"compassDirection\":\"NNW\"},\"weather\":{\"desc\":[{\"value\":\"Moderate or heavy rain shower\"}],\"precipitation\":\"4.2\",\"humidity\":\"89\",\"feelsLike\":\"13\",\"windChill\":\"13\",\"windGusts\":\"32\",\"waterTemp\":\"15\"}}}},{\"dbSpot\":{\"_id\":\"5ad207030ada8d1064d163a8\",\"identification\":{\"lat\":\"38.8\",\"lon\":\"-9.4\",\"name\":\"Test 2\"}},\"apiSpot\":{\"maxTemp\":\"17\",\"mixTemp\":\"15\",\"data\":{\"swell\":{\"height\":\"1.3\",\"period\":\"10.4\",\"direction\":\"NNW\",\"compassDirection\":\"NNW\"},\"wind\":{\"speed\":\"32\",\"direction\":\"350\",\"compassDirection\":\"N\"},\"weather\":{\"desc\":[{\"value\":\"Torrential rain shower\"}],\"precipitation\":\"9.7\",\"humidity\":\"87\",\"feelsLike\":\"15\",\"windChill\":\"15\",\"windGusts\":\"47\",\"waterTemp\":\"15\"}}}}]"
            adapter.addAll(gson.fromJson(test_response, Array<Spot>::class.java))

        }

        return view
    }
}