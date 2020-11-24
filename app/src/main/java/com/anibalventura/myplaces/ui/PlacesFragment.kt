package com.anibalventura.myplaces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anibalventura.myplaces.adapters.PlaceAdapter
import com.anibalventura.myplaces.data.viewmodel.PlaceViewModel
import com.anibalventura.myplaces.databinding.FragmentPlacesBinding
import jp.wasabeef.recyclerview.animators.LandingAnimator

class PlacesFragment : Fragment() {

    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by viewModels()

    private val adapter: PlaceAdapter by lazy { PlaceAdapter() }
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)
        binding.placeViewModel = placeViewModel
        binding.lifecycleOwner = this

        setupRecyclerView()

        placeViewModel.getDatabase.observe(viewLifecycleOwner, { data ->
            placeViewModel.checkIfPlacesIsEmpty(data)
            adapter.setData(data)
        })

        return binding.root
    }

    private fun setupRecyclerView() {
        recyclerView = binding.placeRecyclerView
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        recyclerView.itemAnimator = LandingAnimator().apply {
            addDuration = 300 // Milliseconds
        }
    }

    /** ===================================== Fragment exit/close ===================================== **/

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}