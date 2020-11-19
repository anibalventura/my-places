package com.anibalventura.myplaces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.databinding.FragmentPlacesBinding

class PlacesFragment : Fragment() {

    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        fabPressed()

        return binding.root
    }

    private fun fabPressed() {
        binding.fabPlaces.setOnClickListener {
            findNavController().navigate(R.id.action_placesFragment_to_addPlacesFragment)
        }
    }

    /** ===================================== Fragment exit/close ===================================== **/

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}