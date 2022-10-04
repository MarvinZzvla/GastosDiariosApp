package com.vendetta.gastosdiarios

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vendetta.gastosdiarios.recycler.Productos
import com.vendetta.gastosdiarios.recycler.ProductosAdapter
import gastosdiarios.R
import kotlinx.android.synthetic.main.activity_productos_home.*

private var database = ""
private lateinit var auth: FirebaseAuth
private var list = arrayListOf<QuerySnapshot>()

var productosProviderList = arrayListOf<Productos>()

class ProductosHome : AppCompatActivity() {
    private val fireData = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos_home)
        auth = Firebase.auth
        banner_productos.loadAd(AdRequest.Builder().build())
        loadPreferences()
        getProductos()

        btnCrearProducto.setOnClickListener {
            Intent(this,NuevoProducto::class.java).apply { startActivity(this) }
        }
    }

    fun getProductos(){
        fireData.collection("db1").document(database).collection("Productos").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                list.clear()
                list.add(snapshot)
                providerProductos()
            }
        }
    }

    fun providerProductos(){
        productosProviderList.clear()
        for(producto in list[0].documents){
            val p = producto.data
            productosProviderList.add(Productos(p?.get("name").toString(),p?.get("precio").toString(),
                "Cantidad: ${p?.get("cantidad").toString()}"))

        }
        productosProviderList.sortBy {
            it.cantidad.reversed()
        }

        initRecycleView()
    }

    /*
    fun getProductos(){

        Firebase.database.getReference(database).child("Productos").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    list.add(snapshot)
                    providerProductos()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }*/

    /*
    fun providerProductos() {
        productosProviderList.clear()

        list[0].children.forEach {
            productosProviderList.add(
                Productos(it.child("name").value.toString(),it.child("precio").value.toString(),
                "Cantidad: ${it.child("cantidad").value.toString()}")
            )
        }

        productosProviderList.sortBy {
            it.cantidad.reversed()
        }

        initRecycleView()

    }*/

    fun initRecycleView(){
        val recyclerView = recycleProductos
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductosAdapter(productosProviderList,database)
    }


    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this,HomePage::class.java).apply { startActivity(this) }
    }
}