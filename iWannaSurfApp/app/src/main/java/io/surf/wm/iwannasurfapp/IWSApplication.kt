package io.surf.wm.iwannasurfapp;

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.Response.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.surf.wm.iwannasurfapp.fragment.FavoriteFragment
import io.surf.wm.iwannasurfapp.fragment.SearchFragment
import io.surf.wm.iwannasurfapp.model.Dtos
import io.surf.wm.iwannasurfapp.model.Settings
import io.surf.wm.iwannasurfapp.model.Settings.SortCriteria
import java.util.function.Supplier
import kotlin.collections.ArrayList

class IWSApplication : Application() {

    val queue: RequestQueue by lazy { Volley.newRequestQueue(applicationContext) }

    var activeFragmentId: Int = R.id.navigation_search

    var spots: List<Dtos.Spot> = ArrayList()

    val sharedPreference: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(applicationContext) }

    val settings: Settings by lazy {
        Settings(
                Supplier{ sharedPreference.getString("distance", "80f").toFloat() }
                , Supplier{ SortCriteria.values()[sharedPreference.getString("sort_list", "0").toInt()] }
        )
    }

    val frags: Map<Int, Fragment> by lazy {
        mapOf(
                R.id.navigation_search to SearchFragment()
                //, R.id.navigation_favorite to FavoriteFragment()
        )
    }
}