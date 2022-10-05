package com.vendetta.gastosdiarios.recycler

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import gastosdiarios.R
import kotlinx.android.synthetic.main.activity_nueva_venta.*

class VentasViewHolder(view: View):RecyclerView.ViewHolder(view) {

    val fireData = Firebase.firestore

    var ventaName = view.findViewById<TextView>(R.id.item_title)
    var ventaPrecio = view.findViewById<TextView>(R.id.item_precio)
    var ventasDate = view.findViewById<TextView>(R.id.item_date)
    var ventasCantidad = view.findViewById<TextView>(R.id.item_cantidad)
    var deleteImage = view.findViewById<ImageButton>(R.id.deleteVentaBtn)


    fun render(ventas:Ventas,database:String){
        ventaName.text = ventas.venta_name
        ventaPrecio.text = (ventas.venta_precio.toInt() * ventas.venta_cantidad.toInt()).toString()
        ventasDate.text = ventas.venta_date
        if(ventas.venta_cantidad != "null")
        {
        ventasCantidad.text = ventas.venta_cantidad
        }else {ventasCantidad.text = "1"}




        deleteImage.setOnClickListener(object : View.OnClickListener{
            override fun onClick(view: View?) {
                deleteFromVentas(ventas,database)
                } //End of Click

        })
    }

    fun deleteFromVentas(ventas: Ventas, database: String) {
        var date = ventas.venta_date
        var dateToday = ""
        var timeVenta = ""
        var month = ""
        var year = ""

        //4/09/2022 - 2:57:33
        date.apply {
            this.split(" - ").apply {
                //2:57:33
                timeVenta = this[1]
                //4/09/2022
                this[0].apply {
                    //2022/09/4
                    this.split("/").apply {
                        dateToday = "${this[2]}/${this[1]}/${this[0]}"
                        year = this[2]
                        month = this[1]
                    }
                }
            }
        }

        val alertDialog: AlertDialog? = ventaName.context?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Si",
                    DialogInterface.OnClickListener { dialog, id ->
                    /******Nueva Base de datos ****/
                    fireData.collection("db1").document(database).collection("Ventas").document(database).collection(dateToday).document(timeVenta).delete()

                    /* Firebase.database.getReference(database).child("Ventas").child(dateToday).child(timeVenta).removeValue().addOnSuccessListener {
                            Toast.makeText(itemView.context,"Eliminado!",Toast.LENGTH_SHORT).show()
                        }*/
                        //updateFinanzas(database,dateToday,month,year,ventas)
                        //updateGanancias(database,dateToday,month,year,ventas)
                        updateFinanzasFire(database,dateToday,month,year,ventas)
                        updateGananciasFire(database,dateToday,month,year,ventas)
                    })
                setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
                setTitle("Eliminar Venta")
                setMessage("¿Desea realmente eliminar esta venta?")
                show()
            }
            builder.create()
        }
        //END

    }

    fun updateGananciasFire(
        database: String,
        dateToday: String,
        month: String,
        year: String,
        ventas: Ventas
    ) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var precioProduccion = 0
        var dateMonth = "${year}/${month}"
        var dateYear = "${year}"
        var cantidad = ventasCantidad.text.toString().toInt()

        //Obtener el precio produccion del producto
        fireData.collection("db1").document(database).collection("Productos").document(ventas.venta_name).get().addOnSuccessListener {
            precioProduccion = it.data?.get("precio").toString().toInt()
        }
        //Obtener ganancias y sumarles las nuevas ganancias del dia de hoy
        fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).get().addOnSuccessListener {
            if(!it.exists()){countDay = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad }
            else{ countDay = it.data?.get("ganancias").toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)}
            fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).set(hashMapOf("ganancias" to countDay.toString()),
                SetOptions.merge())

            //Obtener las ganancias del mes y sumarles las nuevas ganancias del dia de hoy
            fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ganancias").get().addOnSuccessListener {
                if(!it.exists()){countMonth = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad}
                else{countMonth= it.data?.get("ganancias").toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)}
                fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ganancias").set(hashMapOf("ganancias" to countMonth.toString()))

                fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).get().addOnSuccessListener {
                    if(!it.exists()){countYear = (ventas.venta_precio.toString().toInt() + precioProduccion) * cantidad}
                    else{countYear = it.data?.get("ganancias").toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)}
                    println("Esta es la cantidad: " + countYear)
                    fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).set(hashMapOf("ganancias" to countYear.toString()), SetOptions.merge())
                }
            }
        }
    }



    fun updateGanancias(
        database: String,
        dateToday: String,
        month: String,
        year: String,
        ventas: Ventas
    ) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var precioProduccion = 0
        var dateMonth = "${year}/${month}"
        var dateYear = "${year}"
        var cantidad = ventasCantidad.text.toString().toInt()

        //Obtener el precio produccion del producto
        Firebase.database.getReference(database).child("Productos").child(ventas.venta_name).child("precio").get().addOnSuccessListener {
            precioProduccion = it.value.toString().toInt()
            //Obtener las ganancias y sumarles las nuevas ganancias del dia de hoy
            Firebase.database.getReference(database).child("Finanzas").child(dateToday).child("ganancias").get().addOnSuccessListener {
                if(!it.exists()){
                    countDay = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad
                }
                else{
                    countDay = it.value.toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)
                }
                Firebase.database.getReference(database).child("Finanzas").child(dateToday).child("ganancias").setValue(countDay)

                //Obtener las ganancias del mes y sumarles las nuevas ganacias del dia de hoy
                Firebase.database.getReference(database).child("Finanzas").child(dateMonth).child("ganancias").get().addOnSuccessListener {
                    if(!it.exists()){
                        countMonth = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad
                    }
                    else{
                        countMonth = it.value.toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)
                    }
                    Firebase.database.getReference(database).child("Finanzas").child(dateMonth).child("ganancias").setValue(countMonth)

                    //Obtener las ganacias del año y sumarles las nuevas ganancias de hoy
                    Firebase.database.getReference(database).child("Finanzas").child(dateYear).child("ganancias").get().addOnSuccessListener {
                        if(!it.exists()){
                            countYear = (ventas.venta_precio.toString().toInt() + precioProduccion)*cantidad
                        }
                        else{
                            countYear= it.value.toString().toInt() - ((ventas.venta_precio.toString().toInt() - precioProduccion)*cantidad)
                        }
                        Firebase.database.getReference(database).child("Finanzas").child(dateYear).child("ganancias").setValue(countYear)
                    }
                }
            }
        }
    }

    fun updateFinanzasFire(
        database: String,
        dateToday: String,
        month: String,
        year: String,
        ventas: Ventas
    ) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var dateMonth = "${year}/${month}"
        var dateYear = "${year}"
        var cantidad = ventasCantidad.text.toString().toInt()

        //Actualizar la cantidad del producto vendido
        fireData.collection("db1").document(database).collection("Productos").document(ventas.venta_name).get().addOnSuccessListener {
            var count = it.data?.get("cantidad").toString().toInt()
            count += cantidad
            fireData.collection("db1").document(database).collection("Productos").document(ventas.venta_name).update("cantidad",count.toString())
        }

        //Get ventas del dia
        fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).get().addOnSuccessListener {
            if(!it.exists()){ countDay = ventas.venta_precio.toInt()*cantidad }
            else{ countDay = it.data?.get("ventas").toString().toInt() - ventas.venta_precio.toInt() * cantidad }
            //Finanzas del dia -Set Ventas del dia
            fireData.collection("db1").document(database).collection("Finanzas").document(dateToday).set(
                hashMapOf("ventas" to countDay.toString()))

            //Get finanzas del mes
            fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ventas").get().addOnSuccessListener {
                if(!it.exists()){countMonth = ventas.venta_precio.toInt() * cantidad}
                else{countMonth = it.data?.get("ventas").toString().toInt() - ventas.venta_precio.toInt() * cantidad}
                fireData.collection("db1").document(database).collection("Finanzas").document(dateMonth+"/ventas").set(
                    hashMapOf("ventas" to countMonth.toString()))

                //Get finanzas del año
                fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).get().addOnSuccessListener {
                    if(!it.exists()){countYear = ventas.venta_precio.toInt()* cantidad}
                    else{ countYear = it.data?.get("ventas").toString().toInt() - (ventas.venta_precio.toInt() * cantidad)}
                    fireData.collection("db1").document(database).collection("Finanzas").document(dateYear).set(
                        hashMapOf("ventas" to countYear.toString()))

                }
            }
        }

    }

    fun updateFinanzas(
        database: String,
        dateToday: String,
        month: String,
        year: String,
        ventas: Ventas
    ) {
        var countDay = 0; var countMonth = 0; var countYear = 0;
        var dateMonth = "${year}/${month}"
        var dateYear = "${year}"
        var cantidad = ventasCantidad.text.toString().toInt()

        //Update Cantidad
        //Actualizar la cantidad actual de producto vendido
        Firebase.database.getReference(database).child("Productos").child(ventas.venta_name).child("cantidad").get().addOnSuccessListener {
            var count = it.value.toString().toInt()
            count+=cantidad
            Firebase.database.getReference(database).child("Productos").child(ventas.venta_name).child("cantidad").setValue(count.toString())

        }

        //Get ventas del dia
        Firebase.database.getReference(database).child("Finanzas").child(dateToday)
            .child("ventas").get().addOnSuccessListener {
                if (!it.exists()){ countDay = ventas.venta_precio.toInt()*cantidad}
                else{
                    countDay = it.value.toString().toInt() - ventas.venta_precio.toString().toInt()*cantidad}
                //Finanzas del dia - Set ventas del dia
                Firebase.database.getReference(database).child("Finanzas").child(dateToday)
                    .child("ventas").setValue(countDay.toString())

                //Get Finanzas del mes
                Firebase.database.getReference(database).child("Finanzas").child(dateMonth).
                child("ventas").get().addOnSuccessListener {
                    if (!it.exists()){
                        countMonth = ventas.venta_precio.toString().toInt()*cantidad
                    }
                    else{
                        countMonth = it.value.toString().toInt() - ventas.venta_precio.toString().toInt()*cantidad
                    }
                    Firebase.database.getReference(database).child("Finanzas").child(dateMonth)
                        .child("ventas").setValue(countMonth.toString())

                    //Get Finanzas del año
                    Firebase.database.getReference(database).child("Finanzas").child(dateYear)
                        .child("ventas").get().addOnSuccessListener {
                            if(!it.exists()){
                                countYear = ventas.venta_precio.toString().toInt()*cantidad
                            }
                            else{
                                countYear = it.value.toString().toInt() - ventas.venta_precio.toString().toInt()*cantidad
                            }

                            Firebase.database.getReference(database).child("Finanzas").child(dateYear).
                            child("ventas").setValue(countYear)
                        }
                }
            }


    }
}