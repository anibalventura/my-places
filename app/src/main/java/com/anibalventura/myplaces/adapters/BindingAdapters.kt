package com.anibalventura.myplaces.adapters

import android.view.View
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.data.model.PlaceModel
import com.anibalventura.myplaces.ui.PlacesFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BindingAdapters {

    companion object {

        @JvmStatic
        @BindingAdapter("android:emptyDatabase")
        fun emptyDatabase(view: View, emptyDatabase: MutableLiveData<Boolean>) {
            when (emptyDatabase.value) {
                true -> view.visibility = View.VISIBLE
                false -> view.visibility = View.INVISIBLE
            }
        }

        @JvmStatic
        @BindingAdapter("android:navigateToPlaceAddFragment")
        fun navigateToPlaceAddFragment(view: FloatingActionButton, navigate: Boolean) {
            view.setOnClickListener {
                if (navigate) {
                    view.findNavController()
                        .navigate(R.id.action_placesFragment_to_addPlacesFragment)
                }
            }
        }

        @JvmStatic
        @BindingAdapter("android:sendDataToPlaceDetailFragment")
        fun sendDataToPlaceDetailFragment(view: CardView, currentItem: PlaceModel) {
            view.setOnClickListener {
                val action =
                    PlacesFragmentDirections.actionPlacesFragmentToPlaceDetailFragment(currentItem)
                view.findNavController().navigate(action)
            }
        }
    }
}