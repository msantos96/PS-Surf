package io.surf.wm.iwannasurfapp.activities

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.location.LocationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import com.google.gson.Gson
import io.surf.wm.iwannasurfapp.fragments.SearchFragment
import io.surf.wm.iwannasurfapp.model.Dtos.*
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import io.surf.wm.iwannasurfapp.IWSApplication
import io.surf.wm.iwannasurfapp.R

import io.surf.wm.iwannasurfapp.fragments.FavoriteFragment
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    companion object {
        const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0

        const val EXTRA_SPOT: String = "EXTRA_SPOT"

        private const val HOST = "https://iwanna-surf.herokuapp.com"
        //private const val LOCALHOST = "http://10.0.2.2:3000"
        private const val SUGGEST = "/api/spots/suggest?lat=%f&lon=%f&range=%f"
        private const val GET_BY_ID = "/api/spot/%s/full"
        private const val SEND_FEEDBACK = "/api/feedback"

        fun suggestUrl(lat: Double, lon: Double, rad: Float) : String = String.format(String.format("%s%s", HOST, SUGGEST), lat, lon, rad).replace(',', '.')
        fun getByIdUrl(id: String) : String = String.format(String.format("%s%s", HOST, GET_BY_ID), id).replace(',', '.')
        fun sendFeedback() = String.format("%s%s", HOST, SEND_FEEDBACK)
    }

    private val appState by lazy { applicationContext as IWSApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(supportFragmentManager::beginTransaction)

        findViewById<BottomNavigationView>(R.id.navigation).setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener { item ->
            appState.activeFragmentId = item.itemId
            return@OnNavigationItemSelectedListener replaceFragment(supportFragmentManager::beginTransaction)
        })

        val searchFragment: SearchFragment = appState.frags[R.id.navigation_search] as SearchFragment

        searchFragment.searchCb = OnClickListener { _ ->
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                searchFragment.progressIsVisible(true)
                searchFragment.buttonIsEnabled(false)
                var location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                while (location == null)
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                searchFragment.clearSpots()

                appState.queue.add(
                        StringRequest(Request.Method.GET, suggestUrl(location.latitude, location.longitude, appState.settings.distance.get()),
                                Response.Listener { response ->
                                    searchFragment.addSpots(Gson().fromJson(response, Array<Spot>::class.java).sortedWith(appState.settings.sortCriteria.get()))
                                    searchFragment.progressIsVisible(false)
                                    searchFragment.buttonIsEnabled(true)
                                },
                                Response.ErrorListener { e ->
                                    Toast.makeText(this, applicationContext.getString(R.string.error), Toast.LENGTH_LONG).show()
                                    searchFragment.progressIsVisible(false)
                                    searchFragment.buttonIsEnabled(true)
                                }))
            } else {
                if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, applicationContext.getText(R.string.start_sharing), Toast.LENGTH_LONG).show()
                }
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }

        searchFragment.itemClickCb = OnItemClickListener { p, _, position, _ ->
            startActivity(
                    Intent(this, SpotInfoActivity::class.java)
                            .putExtra(EXTRA_SPOT, p.getItemAtPosition(position) as Spot))
        }

        val favoriteFragment: FavoriteFragment = appState.frags[R.id.navigation_favorite] as FavoriteFragment

        favoriteFragment.itemClickCb = searchFragment.itemClickCb

        for(id in appState.sharedPreference.getStringSet(IWSApplication.FAVS_PREF, setOf())) {
            favoriteFragment.futureSpots.putIfAbsent(id, CompletableFuture())
            appState.queue.add(
                    StringRequest(Request.Method.GET, getByIdUrl(id),
                            Response.Listener { response ->
                                favoriteFragment.futureSpots[id]!!.complete(Gson().fromJson(response, Spot::class.java))
                            },
                            Response.ErrorListener { e ->
                                when (e) {
                                    is TimeoutError -> Toast.makeText(applicationContext, applicationContext.getString(R.string.timeout), Toast.LENGTH_LONG).show()
                                    is NoConnectionError -> Toast.makeText(applicationContext, applicationContext.getString(R.string.connection), Toast.LENGTH_LONG).show()
                                    else -> {
                                        val favsId: MutableSet<String> = appState.sharedPreference.getStringSet(IWSApplication.FAVS_PREF, mutableSetOf())
                                        favsId.remove(id)

                                        appState.sharedPreference
                                                .edit()
                                                .remove(IWSApplication.FAVS_PREF)
                                                .apply()

                                        appState.sharedPreference
                                                .edit()
                                                .putStringSet(IWSApplication.FAVS_PREF, favsId)
                                                .apply()
                                    }
                                }
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
}