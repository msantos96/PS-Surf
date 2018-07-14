package io.surf.wm.iwannasurfapp;

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.surf.wm.iwannasurfapp.fragments.FavoriteFragment
import io.surf.wm.iwannasurfapp.fragments.SearchFragment
import io.surf.wm.iwannasurfapp.Settings.SortCriteria
import java.util.function.Supplier

class IWSApplication : Application() {

    val queue: RequestQueue by lazy { Volley.newRequestQueue(applicationContext) }

    var activeFragmentId: Int = R.id.navigation_search

    val sharedPreference: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(applicationContext) }

    companion object {
        val FAVS_PREF: String = "favs_list"
    }

    val settings: Settings by lazy {
        Settings(
                Supplier { sharedPreference.getString("distance", "80f").toFloat() }
                , Supplier { SortCriteria.values()[sharedPreference.getString("sort_list", "0").toInt()] }
        )
    }

    val frags: Map<Int, Fragment> by lazy {
        mapOf(
                R.id.navigation_search to SearchFragment()
                , R.id.navigation_favorite to FavoriteFragment()
        )
    }
}