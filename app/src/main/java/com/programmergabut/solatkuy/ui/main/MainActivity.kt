package com.programmergabut.solatkuy.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.programmergabut.solatkuy.R
import com.programmergabut.solatkuy.base.BaseActivity
import com.programmergabut.solatkuy.databinding.ActivityMainBinding
import com.programmergabut.solatkuy.ui.SolatKuyFragmentFactory
import com.programmergabut.solatkuy.util.LogConfig
import com.programmergabut.solatkuy.util.LogConfig.Companion.ERROR
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
 * Created by Katili Jiwo Adi Wiyono on 25/03/20.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainActivityViewModel>(
    R.layout.activity_main,
    MainActivityViewModel::class.java
) {

    @Inject
    lateinit var fragmentFactory: SolatKuyFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomNav()
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    override fun onDestroy() {
        setIsHasOpenAnimation(false)
        super.onDestroy()
    }

    private fun initBottomNav() {
        try{
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            val navController = navHostFragment.navController
            binding.bottomNavigation.setupWithNavController(navController)
            navHostFragment.findNavController()
                .addOnDestinationChangedListener { _, destination, _ ->
                    when(destination.id){
                        R.id.fragmentHome,
                        R.id.fragmentCompass,
                        R.id.fragmentQuran,
                        R.id.fragmentSetting
                            -> binding.bottomNavigation.visibility = View.VISIBLE
                        else
                            -> binding.bottomNavigation.visibility = View.GONE
                    }
                }
            binding.bottomNavigation.setOnNavigationItemReselectedListener {/* NO-OP */ }
        }
        catch (ex: Exception){
            Log.d(ERROR, ex.message.toString())
        }
    }

}
