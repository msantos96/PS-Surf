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
import android.support.design.widget.FloatingActionButton
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
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import io.surf.wm.iwannasurfapp.fragment.FavoriteFragment
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0

        const val EXTRA_SPOT: String = "EXTRA_SPOT"
        const val EXTRA_LAT: String = "EXTRA_LAT"
        const val EXTRA_LON: String = "EXTRA_LON"

        const val FAVS_PREF: String = "favs_list"

        private const val HOST = "https://iwanna-surf.herokuapp.com"
        private const val SUGGEST = "/api/spots/suggest?lat=%f&lon=%f&range=%f"
        private const val GET_BY_ID = "/api/spots/get?id=%s"

        fun suggestUrl(lat: Double, lon: Double, rad: Float) : String = String.format(String.format("%s%s", HOST, SUGGEST), lat, lon, rad).replace(',', '.')
        fun getByIdUrl(id: String) : String = String.format(String.format("%s%s", HOST, GET_BY_ID), id).replace(',', '.')
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

        searchFragment.searchCb = OnClickListener { _ ->
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                findViewById<ProgressBar>(R.id.prog_bar).visibility = ProgressBar.VISIBLE
                findViewById<FloatingActionButton>(R.id.spot_search).isEnabled = false
                val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                appState.sharedPreference.edit()
                        .putString(EXTRA_LAT, location.latitude.toString())
                        .putString(EXTRA_LON, location.longitude.toString())
                        .apply()

                appState.queue.add(
                        OfflineStringRequest(Request.Method.GET, suggestUrl(location.latitude, location.longitude, appState.settings.distance.get()),
                                Response.Listener { response ->
                                    appState.spots = Gson().fromJson(response, Array<Dtos.Spot>::class.java).sortedWith(appState.settings.sortCriteria.get())
                                    val adapter = findViewById<ListView>(R.id.spots_list).adapter as SpotArrayAdapter
                                    adapter.clear()
                                    adapter.addAll(appState.spots)
                                    findViewById<ProgressBar>(R.id.prog_bar).visibility = ProgressBar.INVISIBLE
                                    findViewById<FloatingActionButton>(R.id.spot_search).isEnabled = true
                                },
                                Response.ErrorListener { _ ->
                                    Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_LONG).show()
                                    findViewById<ProgressBar>(R.id.prog_bar).visibility = ProgressBar.INVISIBLE
                                    findViewById<FloatingActionButton>(R.id.spot_search).isEnabled = true
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

        val favoriteFragment: FavoriteFragment = appState.frags[R.id.navigation_favorite] as FavoriteFragment

        favoriteFragment.itemClickCb = searchFragment.itemClickCb

        for(id in appState.sharedPreference.getStringSet(FAVS_PREF, setOf())) {
            favoriteFragment.futureSpots.putIfAbsent(id, CompletableFuture())
            appState.queue.add(
                    OfflineStringRequest(Request.Method.GET, getByIdUrl(id),
                        Response.Listener { response ->
                            favoriteFragment.futureSpots[id]!!.complete(Gson().fromJson(response, Dtos.DbSpot::class.java))
                        },
                            Response.ErrorListener { _ ->
                                //TODO: error
                                //if(error == notFound) remove id from shared preferences.
                            }))
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
    class OfflineStringRequest(method: Int, url: String, listener: Response.Listener<String>, errorListener: Response.ErrorListener) : StringRequest(method, "http://google.com", Response.Listener { _ -> if(Uri.parse(url).getQueryParameter("range") != null) listener.onResponse(suggestResponse) else listener.onResponse(getResponse[Uri.parse(url).getQueryParameter("id")]) }, errorListener) {
        companion object {
            private const val suggestResponse = "[{\"dbSpot\":{\"identification\":{\"lat\":39.364213,\"lon\":-9.36494,\"name\":\"Peniche (Praia do Cerro)\",\"distance\":9059.333203292337},\"additionalInfo\":{\"crowd\":{\"weekDays\":3,\"weekEnds\":3},\"realLat\":39.3617002,\"realLon\":-9.356414,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":2.5},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":240,\"max\":360},\"compassDirection\":[\"N\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":90,\"max\":270},\"compassDirection\":[\"S\",\"SE\",\"SW\"]},\"_id\":\"5ae44faf097725302874b3a3\",\"rating\":\"31.69\"},\"realTimeData\":{\"swell\":{\"height\":\"1.18\",\"period\":\"8.85\",\"direction\":\"301.81\"},\"wind\":{\"speed\":\"5.03\",\"direction\":\"235.16\"}}},{\"dbSpot\":{\"identification\":{\"lat\":39.343935,\"lon\":-9.367785,\"name\":\"Peniche - Supertubos (Medao Grande)\",\"distance\":9060.666755158925},\"additionalInfo\":{\"crowd\":{\"weekDays\":2,\"weekEnds\":4},\"realLat\":39.3488795,\"realLon\":-9.3669723,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"N\",\"NW\",\"SW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":90},\"compassDirection\":[\"NE\"]},\"_id\":\"5b01669455f91534b8449795\",\"rating\":\"41.81\"},\"realTimeData\":{\"swell\":{\"height\":\"1.18\",\"period\":\"8.85\",\"direction\":\"301.81\"},\"wind\":{\"speed\":\"5.16\",\"direction\":\"230.72\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.734039,\"lon\":-9.475591,\"name\":\"Guincho\",\"distance\":9099.332253407892},\"additionalInfo\":{\"crowd\":{\"weekDays\":2,\"weekEnds\":5},\"realLat\":38.7324263,\"realLon\":-9.4722738,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":240,\"max\":350},\"compassDirection\":[\"N\",\"SW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":45,\"max\":120},\"compassDirection\":[\"E\"]},\"_id\":\"5b01678955f91534b8449796\",\"rating\":\"42.61\"},\"realTimeData\":{\"swell\":{\"height\":\"1.16\",\"period\":\"9.51\",\"direction\":\"301.00\"},\"wind\":{\"speed\":\"3.72\",\"direction\":\"229.00\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.632789,\"lon\":-9.235149,\"name\":\"Costa da Caparica - Nova Praia\",\"distance\":9122.347368719562},\"additionalInfo\":{\"crowd\":{\"weekDays\":4,\"weekEnds\":4},\"realLat\":38.6284746,\"realLon\":-9.2305969,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":4},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"S\",\"SW\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":40},\"compassDirection\":[\"NE\"]},\"_id\":\"5b01681f55f91534b8449797\",\"rating\":\"43.37\"},\"realTimeData\":{\"swell\":{\"height\":\"0.96\",\"period\":\"9.36\",\"direction\":\"281.70\"},\"wind\":{\"speed\":\"3.31\",\"direction\":\"184.75\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.701884,\"lon\":-9.399918,\"name\":\"Estoril (Praia do Tamariz)\",\"distance\":9106.59647500682},\"additionalInfo\":{\"crowd\":{\"weekDays\":0,\"weekEnds\":2},\"realLat\":38.7025946,\"realLon\":-9.4006614,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":2,\"max\":5},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"S\",\"SW\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":180},\"compassDirection\":[\"N\",\"NE\",\"E\",\"SE\",\"S\"]},\"_id\":\"5ad207030ada8d1064d163a8\",\"rating\":\"45.77\"},\"realTimeData\":{\"swell\":{\"height\":\"1.00\",\"period\":\"8.48\",\"direction\":\"282.49\"},\"wind\":{\"speed\":\"3.25\",\"direction\":\"206.76\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.678876,\"lon\":-9.340417,\"name\":\"Carcavelos\",\"distance\":9112.138585424826},\"additionalInfo\":{\"crowd\":{\"weekDays\":4,\"weekEnds\":5},\"realLat\":38.6811441,\"realLon\":-9.3397076,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":4},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"S\",\"SW\",\"NW\"]},\"wind\":{\"speed\":{\"min\":27,\"max\":47},\"direction\":{\"min\":120,\"max\":144},\"compassDirection\":[\"SE\"]},\"_id\":\"5ad2067e0ada8d1064d163a7\",\"rating\":\"59.17\"},\"realTimeData\":{\"swell\":{\"height\":\"0.90\",\"period\":\"8.39\",\"direction\":\"274.09\"},\"wind\":{\"speed\":\"3.04\",\"direction\":\"207.17\"}}}]"
            private val getResponse by lazy {
                val map = hashMapOf<String, String>()
                map.put("5b01669455f91534b8449795","{\"identification\":{\"lat\":39.343935,\"lon\":-9.367785,\"name\":\"Peniche - Supertubos (Medão Grande)\"},\"additionalInfo\":{\"crowd\":{\"weekDays\":2,\"weekEnds\":4},\"realLat\":39.3488795,\"realLon\":-9.3669723,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"N\",\"NW\",\"SW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":90},\"compassDirection\":[\"NE\"]},\"_id\":\"5b01669455f91534b8449795\"}")
                map.put("5ae44faf097725302874b3a3","{\"identification\":{\"lat\":39.364213,\"lon\":-9.36494,\"name\":\"Peniche (Praia do Cerro)\"},\"additionalInfo\":{\"crowd\":{\"weekDays\":3,\"weekEnds\":3},\"realLat\":39.3617002,\"realLon\":-9.356414,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":2.5},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":240,\"max\":360},\"compassDirection\":[\"N\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":90,\"max\":270},\"compassDirection\":[\"S\",\"SE\",\"SW\"]},\"_id\":\"5ae44faf097725302874b3a3\"}")
                map
            }
        }
    }
}