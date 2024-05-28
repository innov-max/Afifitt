package com.example.afifit.data

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.afifit.R
import com.bumptech.glide.Glide

class DrugAdapter(private val drugs: MutableList<Drug>) : RecyclerView.Adapter<DrugAdapter.DrugViewHolder>() {

    inner class DrugViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drugName: TextView = view.findViewById(R.id.drug_name)
        val drugTime: TextView = view.findViewById(R.id.drug_time)
        val drugImage: ImageView = view.findViewById(R.id.drug_image)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
        val cardView: CardView = view as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drug, parent, false)
        return DrugViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrugViewHolder, position: Int) {
        val drug = drugs[position]
        holder.drugName.text = drug.name
        holder.drugTime.text = drug.time
        Glide.with(holder.drugImage.context)
            .load(Uri.parse(drug.photoUrl))
            .into(holder.drugImage)
    }

    override fun getItemCount(): Int = drugs.size

    fun addDrug(drug: Drug) {
        drugs.add(drug)
        notifyItemInserted(drugs.size - 1)
    }

    fun removeDrug(position: Int) {
        drugs.removeAt(position)
        notifyItemRemoved(position)
    }
}
