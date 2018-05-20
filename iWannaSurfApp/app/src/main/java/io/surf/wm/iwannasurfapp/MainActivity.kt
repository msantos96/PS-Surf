package io.surf.wm.iwannasurfapp

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.location.LocationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.widget.ListView
import com.android.volley.Request
import com.android.volley.Response
import com.google.gson.Gson
import io.surf.wm.iwannasurfapp.adapter.SpotArrayAdapter
import io.surf.wm.iwannasurfapp.fragment.SearchFragment
import io.surf.wm.iwannasurfapp.model.Dtos
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import com.android.volley.toolbox.StringRequest

class MainActivity : AppCompatActivity() {

    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0

        const val EXTRA_SPOT: String = "EXTRA_SPOT"
        const val EXTRA_LAT: String = "EXTRA_LAT"
        const val EXTRA_LON: String = "EXTRA_LON"

        private const val HOST = "https://iwanna-surf.herokuapp.com"
        private const val SUGGEST = "/spots/suggest?lat=%f&lon=%f&range=%f"

        fun getSuggestUrl(lat: Double, lon: Double, rad: Float) : String = String.format(String.format("%s%s", HOST, SUGGEST), lat, lon, rad).replace(',', '.')
    }

    private val appState by lazy { applicationContext as IWSApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(supportFragmentManager::beginTransaction)

        if(savedInstanceState != null) return

        findViewById<BottomNavigationView>(R.id.navigation).setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener { item ->
            appState.activeFragmentId = item.itemId
            return@OnNavigationItemSelectedListener replaceFragment(supportFragmentManager::beginTransaction)
        })

        val searchFragment: SearchFragment = appState.frags[R.id.navigation_search] as SearchFragment

        searchFragment.searchCb = OnClickListener { view ->
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                appState.sharedPreference.edit()
                        .putString(EXTRA_LAT, location.latitude.toString())
                        .putString(EXTRA_LON, location.longitude.toString())
                        .apply()

                appState.queue.add(
                        OfflineStringRequest(Request.Method.GET, getSuggestUrl(location.latitude, location.longitude, appState.settings.distance.get()),
                                Response.Listener { response ->
                                    appState.spots = Gson().fromJson(response, Array<Dtos.Spot>::class.java).sortedWith(appState.settings.sortCriteria.get())
                                    val adapter = findViewById<ListView>(R.id.spot_list).adapter as SpotArrayAdapter
                                    adapter.clear()
                                    adapter.addAll(appState.spots)
                                },
                                Response.ErrorListener { error ->
                                    //TODO: error
                                }))
            } else {
                if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "You Need To Start Sharing Your Location!", Toast.LENGTH_LONG).show()
                }
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }

        searchFragment.itemClickCb = OnItemClickListener { _, _, position, _ ->
            startActivity(
                    Intent(this, SpotInfoActivity::class.java)
                            .putExtra(EXTRA_SPOT, appState.spots[position])
                            .putExtra(EXTRA_LAT, appState.sharedPreference.getString(EXTRA_LAT, "").toDouble())
                            .putExtra(EXTRA_LON, appState.sharedPreference.getString(EXTRA_LON, "").toDouble()))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.btn_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun replaceFragment(beginTransaction: () -> FragmentTransaction) : Boolean {
        val transaction = beginTransaction()
        transaction.replace(R.id.fragment, appState.frags[appState.activeFragmentId])
        transaction.commit()
        return true
    }

    //TODO: remove
    class OfflineStringRequest(method: Int, url: String, listener: Response.Listener<String>, errorListener: Response.ErrorListener) : StringRequest(method, "http://google.com", Response.Listener { _ -> if(Uri.parse(url).getQueryParameter("range").toDouble() > 600) listener.onResponse(response) else listener.onResponse("[]") }, errorListener) {
        companion object {
            private const val response = "[{\"dbSpot\":{\"identification\":{\"lat\":\"38.7\",\"lon\":\"-9.4\",\"name\":\"Praia de Estoril\",\"thumbsUp\":0,\"thumbsDown\":0},\"additionalInfo\":{},\"swell\":{\"height\":{\"min\":1,\"max\":4},\"period\":{\"min\":8,\"max\":13},\"compassDirection\":[\"NW\",\"NNW\"],\"direction\":{\"min\":280,\"max\":350}},\"wind\":{\"speed\":{\"min\":30,\"max\":43},\"compassDirection\":[\"NW\"],\"direction\":{\"min\":287,\"max\":315}},\"tides\":[],\"_id\":\"5ad207030ada8d1064d163a8\",\"rating\":48.690000000000005},\"realTimeData\":{\"swell\":{\"height\":\"0.63\",\"period\":\"9.17\",\"direction\":\"298.84\"},\"wind\":{\"speed\":\"1.01\",\"direction\":\"166.72\"}}},{\"dbSpot\":{\"identification\":{\"lat\":\"39.67\",\"lon\":\"-9.34\",\"name\":\"Praia de Carcavelos\",\"thumbsUp\":2,\"thumbsDown\":0},\"additionalInfo\":{},\"swell\":{\"height\":{\"min\":2,\"max\":6.5},\"period\":{\"min\":8,\"max\":16.7},\"compassDirection\":[\"W\",\"WNW\",\"WSW\"],\"direction\":{\"min\":180,\"max\":290}},\"wind\":{\"speed\":{\"min\":27,\"max\":47},\"compassDirection\":[\"WNW\"],\"direction\":{\"min\":267,\"max\":310}},\"tides\":[],\"_id\":\"5ad2067e0ada8d1064d163a7\",\"rating\":62.14},\"realTimeData\":{\"swell\":{\"height\":\"1.42\",\"period\":\"8.54\",\"direction\":\"329.28\"},\"wind\":{\"speed\":\"1.50\",\"direction\":\"324.32\"}}},{\"dbSpot\":{\"identification\":{\"lat\":\"39.14\",\"lon\":\"-9.58\",\"name\":\"Praia de Peniche\",\"thumbsUp\":14,\"thumbsDown\":4},\"additionalInfo\":{},\"swell\":{\"height\":{\"min\":2.3,\"max\":5},\"period\":{\"min\":8,\"max\":13.8},\"compassDirection\":[\"W\",\"WNW\",\"WNW\",\"NW\"],\"direction\":{\"min\":210,\"max\":290}},\"wind\":{\"speed\":{\"min\":40,\"max\":57},\"compassDirection\":[\"WNW\"],\"direction\":{\"min\":260,\"max\":297}},\"tides\":[],\"_id\":\"5ae44faf097725302874b3a3\",\"rating\":72.25999999999999},\"realTimeData\":{\"swell\":{\"height\":\"1.47\",\"period\":\"8.47\",\"direction\":\"330.48\"},\"wind\":{\"speed\":\"0.85\",\"direction\":\"314.53\"}}}]"
        }
    }
}