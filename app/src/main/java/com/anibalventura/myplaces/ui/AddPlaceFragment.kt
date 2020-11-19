package com.anibalventura.myplaces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.databinding.FragmentAddPlaceBinding
import com.anibalventura.myplaces.utils.snackBarMsg

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        fabPressed()

        return binding.root
    }

    private fun fabPressed() {
        binding.fabAddPlace.setOnClickListener {
            findNavController().navigate(R.id.action_addPlaceFragment_to_placesFragment)
            snackBarMsg(requireView(), "Place added.")
        }
    }

    /** ===================================== Fragment exit/close ===================================== **/

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}