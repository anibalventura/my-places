package com.anibalventura.myplaces.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anibalventura.myplaces.data.model.PlaceModel
import com.anibalventura.myplaces.databinding.RecyclerviewPlaceBinding

class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.MyViewHolder>() {

    private var dataList = emptyList<PlaceModel>()

    class MyViewHolder(private val binding: RecyclerviewPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(placeModel: PlaceModel) {
            binding.placeModel = placeModel
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerviewPlaceBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.bind(currentItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(placeModel: List<PlaceModel>) {
        val placeDiffUtil = PlaceDiffUtil(dataList, placeModel)
        val placeDiffUtilResult = DiffUtil.calculateDiff(placeDiffUtil)
        this.dataList = placeModel
        placeDiffUtilResult.dispatchUpdatesTo(this)
    }
}

private class PlaceDiffUtil(
    private val oldList: List<PlaceModel>,
    private val newList: List<PlaceModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
                && oldList[oldItemPosition].image == newList[newItemPosition].image
                && oldList[oldItemPosition].title == newList[newItemPosition].title
                && oldList[oldItemPosition].description == newList[newItemPosition].description
    }
}