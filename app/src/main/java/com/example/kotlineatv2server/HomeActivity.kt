package com.example.kotlineatv2server

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavController
import com.example.kotlineatv2server.common.Common
import com.example.kotlineatv2server.eventbus.CategoryClick
import com.example.kotlineatv2server.eventbus.ChangeMenuClick
import com.example.kotlineatv2server.eventbus.ToasEvent
import com.google.firebase.auth.FirebaseAuth
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView:NavigationView
    private var menuClick:Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)
        navView= findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_category,R.id.nav_food_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener(object :NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(menu: MenuItem): Boolean {
                menu.isChecked = true
                drawerLayout!!.closeDrawers()
                if (menu.itemId == R.id.nav_sign_out){
                    signOut()
                }
                else if(menu.itemId == R.id.nav_category)
                {
                    if (menuClick != menu.itemId)
                        navController.navigate(R.id.nav_category)

                }

                menuClick = menu.itemId


                return true
            }

        })

    }

    private fun signOut(){
        //ABRIMOS DIALOGO PARA PREGUNTAR SI DESEA CERRAR LA SESION
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Cerrar Sesion")
            .setMessage("¿Estas seguro de cerrar la sesion?")
            .setNegativeButton("cancelar",{dialogInterface,_ ->dialogInterface.dismiss()})
            .setPositiveButton("OK"){dialogInterface,_ ->
                Common.foodModelSelected = null
                Common.category_selected = null
                Common.currentServerUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@HomeActivity,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }
        val dialog = builder.create()
        dialog.show()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onCategoryClick(event:CategoryClick){
        if (event.isSucces){

            if (menuClick != R.id.nav_food_list)
            {
                navController.navigate(R.id.nav_food_list)
                menuClick = R.id.nav_food_list

            }

        }

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onChangeMenuEvent(event:ChangeMenuClick){
        if (!event.isFromFoodList){

            //clear
            navController.popBackStack(R.id.nav_category,true)
            navController.navigate(R.id.nav_category)
        }
        menuClick = -1

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onToastEvent(event:ToasEvent){
        if (event.isUpdate){

            Toast.makeText(this@HomeActivity,"Actualizado con exito",Toast.LENGTH_SHORT).show()

        }else{
            Toast.makeText(this@HomeActivity,"Eliminado con exito",Toast.LENGTH_SHORT).show()
        }
        EventBus.getDefault().postSticky(ChangeMenuClick(event.isBackFromFoodList))


    }



}
