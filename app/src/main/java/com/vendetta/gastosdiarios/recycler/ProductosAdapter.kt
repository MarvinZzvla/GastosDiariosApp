package com.vendetta.gastosdiarios.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.vendetta.gastosdiarios.productosProviderList
import gastosdiarios.R

class ProductosAdapter (val productosList:ArrayList<Productos>,val database:String) : RecyclerView.Adapter<ProductosViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductosViewHolder(layoutInflater.inflate(R.layout.producto_card_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        var item = productosProviderList[position]
        holder.render(item,database)
    }

    override fun getItemCount(): Int {
        return productosList.size
    }
}