package io.surf.wm.iwannasurfapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import io.surf.wm.iwannasurfapp.fragment.FavoriteFragment
import io.surf.wm.iwannasurfapp.fragment.HomeFragment
import io.surf.wm.iwannasurfapp.fragment.SearchFragment
import io.surf.wm.iwannasurfapp.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val frags: Map<Int, Fragment> = mapOf(
                R.id.navigation_home to HomeFragment(),
                R.id.navigation_search to SearchFragment(),
                R.id.navigation_favorite to FavoriteFragment(),
                R.id.navigation_settings to SettingsFragment())

        var transaction: FragmentTransaction
        var fragment: Fragment? = frags[R.id.navigation_home]
        val update: () -> Unit = {
            transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment, fragment)
            transaction.commit()
        }

        update()

        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            fragment = frags[item.itemId]
            if (fragment != null) {
                update()
                return@OnNavigationItemSelectedListener true
            }
            false
        })
    }
}

/*
TODO:
    1. Settings
    2. Find coordinates
    3. Object/Sequence Diagram
    4. Comments and explanations(DP)
    5. Merge Parts
OPT:
    6. VolleyWrapper for test purpose
    7. Tests
*/

/* Open maps
    //Creates an Intent that will load a map of San Francisco
    val gmmIntentUri: Uri = Uri.parse("geo:37.7749,-122.4194")
    val mapIntent: Intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    startActivity(mapIntent)
*/