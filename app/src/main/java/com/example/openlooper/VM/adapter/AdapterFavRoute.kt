package com.example.openlooper.VM.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.openlooper.R
import com.example.openlooper.model.Favorite
import kotlinx.android.synthetic.main.favorite_one_row.view.*
import java.lang.Exception

class AdapterFavRoute(
    val itemOnClickCallback : (Favorite) -> Unit = {}
) : RecyclerView.Adapter<AdapterFavRoute.MyViewHolder>() {

    private var favList = emptyList<Favorite>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.favorite_one_row,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return favList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = favList[position]
        holder.itemView.route_name_one_row.text = currentItem.name
        holder.itemView.route_distance_one_row.text = "${currentItem.distance} km"

        holder.itemView.favorite_one_row_id.setOnClickListener {
            //Load route from database
            try {
                itemOnClickCallback(favList.get(position))
            }
            catch (e : Exception) {}
        }
    }

    fun setData(fav: List<Favorite>) {
        this.favList = fav
        notifyDataSetChanged()
    }
}