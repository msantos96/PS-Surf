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
                        OfflineStringRequest(Request.Method.GET, getSuggestUrl(location.latitude, location.longitude, appState.settings.distance.get()),
                                Response.Listener { response ->
                                    appState.spots = Gson().fromJson(response, Array<Dtos.Spot>::class.java).sortedWith(appState.settings.sortCriteria.get())
                                    val adapter = findViewById<ListView>(R.id.spot_list).adapter as SpotArrayAdapter
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
            private const val response = "[{\"dbSpot\":{\"identification\":{\"lat\":39.364213,\"lon\":-9.36494,\"name\":\"Peniche (Praia do Cerro)\",\"distance\":64.44297135903935},\"additionalInfo\":{\"crowd\":{\"weekDays\":3,\"weekEnds\":3},\"realLat\":39.3617002,\"realLon\":-9.356414,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":2.5},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":240,\"max\":360},\"compassDirection\":[\"N\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":90,\"max\":270},\"compassDirection\":[\"S\",\"SE\",\"SW\"]},\"_id\":\"5ae44faf097725302874b3a3\",\"rating\":\"32.96\"},\"realTimeData\":{\"swell\":{\"height\":\"1.32\",\"period\":\"9.93\",\"direction\":\"315.87\"},\"wind\":{\"speed\":\"2.54\",\"direction\":\"132.48\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.701884,\"lon\":-9.399918,\"name\":\"Estoril (Praia do Tamariz)\",\"distance\":24.024061547018686},\"additionalInfo\":{\"crowd\":{\"weekDays\":0,\"weekEnds\":2},\"realLat\":38.7025946,\"realLon\":-9.4006614,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":2,\"max\":5},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"S\",\"SW\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":180},\"compassDirection\":[\"N\",\"NE\",\"E\",\"SE\",\"S\"]},\"_id\":\"5ad207030ada8d1064d163a8\",\"rating\":\"35.71\"},\"realTimeData\":{\"swell\":{\"height\":\"0.72\",\"period\":\"9.45\",\"direction\":\"290.98\"},\"wind\":{\"speed\":\"2.62\",\"direction\":\"135.32\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.734039,\"lon\":-9.475591,\"name\":\"Guincho\",\"distance\":28.705001494124346},\"additionalInfo\":{\"crowd\":{\"weekDays\":2,\"weekEnds\":5},\"realLat\":38.7324263,\"realLon\":-9.4722738,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":240,\"max\":350},\"compassDirection\":[\"N\",\"SW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":45,\"max\":120},\"compassDirection\":[\"E\"]},\"_id\":\"5b01678955f91534b8449796\",\"rating\":\"42.79\"},\"realTimeData\":{\"swell\":{\"height\":\"0.86\",\"period\":\"9.58\",\"direction\":\"303.59\"},\"wind\":{\"speed\":\"3.77\",\"direction\":\"152.40\"}}},{\"dbSpot\":{\"identification\":{\"lat\":39.343935,\"lon\":-9.367785,\"name\":\"Peniche - Supertubos (Medão Grande)\",\"distance\":62.35111044835323},\"additionalInfo\":{\"crowd\":{\"weekDays\":2,\"weekEnds\":4},\"realLat\":39.3488795,\"realLon\":-9.3669723,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":3},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"N\",\"NW\",\"SW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":90},\"compassDirection\":[\"NE\"]},\"_id\":\"5b01669455f91534b8449795\",\"rating\":\"42.95\"},\"realTimeData\":{\"swell\":{\"height\":\"1.32\",\"period\":\"9.93\",\"direction\":\"315.87\"},\"wind\":{\"speed\":\"2.80\",\"direction\":\"128.73\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.632789,\"lon\":-9.235149,\"name\":\"Costa da Caparica - Nova Praia\",\"distance\":20.52782309960081},\"additionalInfo\":{\"crowd\":{\"weekDays\":4,\"weekEnds\":4},\"realLat\":38.6284746,\"realLon\":-9.2305969,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":4},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"S\",\"SW\",\"NW\"]},\"wind\":{\"speed\":{\"min\":15,\"max\":30},\"direction\":{\"min\":0,\"max\":40},\"compassDirection\":[\"NE\"]},\"_id\":\"5b01681f55f91534b8449797\",\"rating\":\"44.68\"},\"realTimeData\":{\"swell\":{\"height\":\"0.53\",\"period\":\"9.64\",\"direction\":\"275.68\"},\"wind\":{\"speed\":\"2.15\",\"direction\":\"117.30\"}}},{\"dbSpot\":{\"identification\":{\"lat\":38.678876,\"lon\":-9.340417,\"name\":\"Carcavelos\",\"distance\":21.3080979130899},\"additionalInfo\":{\"crowd\":{\"weekDays\":4,\"weekEnds\":5},\"realLat\":38.6811441,\"realLon\":-9.3397076,\"thumbsUp\":0,\"thumbsDown\":0,\"rank\":4},\"swell\":{\"height\":{\"min\":1,\"max\":3},\"period\":{\"min\":15,\"max\":30},\"direction\":{\"min\":180,\"max\":360},\"compassDirection\":[\"S\",\"SW\",\"NW\"]},\"wind\":{\"speed\":{\"min\":27,\"max\":47},\"direction\":{\"min\":120,\"max\":144},\"compassDirection\":[\"SE\"]},\"_id\":\"5ad2067e0ada8d1064d163a7\",\"rating\":\"49.43\"},\"realTimeData\":{\"swell\":{\"height\":\"0.57\",\"period\":\"9.52\",\"direction\":\"279.86\"},\"wind\":{\"speed\":\"1.98\",\"direction\":\"128.23\"}}}]";
        }
    }
}