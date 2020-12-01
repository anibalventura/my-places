package com.anibalventura.myplaces.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.adapters.PlaceAdapter
import com.anibalventura.myplaces.data.model.PlaceModel
import com.anibalventura.myplaces.data.viewmodel.PlaceViewModel
import com.anibalventura.myplaces.databinding.FragmentPlacesBinding
import com.anibalventura.myplaces.utils.Constants.SWIPE_DELETE
import com.anibalventura.myplaces.utils.Constants.SWIPE_EDIT
import com.anibalventura.myplaces.utils.SwipeItem
import com.anibalventura.myplaces.utils.snackBarMsg
import jp.wasabeef.recyclerview.animators.LandingAnimator

class PlacesFragment : Fragment(), SearchView.OnQueryTextListener {

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

        setHasOptionsMenu(true)

        return binding.root
    }

    /** ===================================== Setup view. ===================================== **/

    private fun setupRecyclerView() {
        recyclerView = binding.placeRecyclerView
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        recyclerView.itemAnimator = LandingAnimator().apply {
            addDuration = 300 // Milliseconds
        }

        setupSwipeItem(SWIPE_DELETE, recyclerView)
        setupSwipeItem(SWIPE_EDIT, recyclerView)
    }

    private fun setupSwipeItem(action: Int, recyclerView: RecyclerView) {
        lateinit var background: Drawable
        lateinit var icon: Drawable

        when (action) {
            SWIPE_DELETE -> {
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_swipe_delete, null)!!
                icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_trash, null)!!
            }
            SWIPE_EDIT -> {
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_swipe_archive, null)!!
                icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_location, null)!!
            }
        }

        val swipeToDeleteCallBack = object : SwipeItem(action, background, icon, requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, action: Int) {

                val placeItem = adapter.dataList[viewHolder.adapterPosition]
                when (action) {
                    SWIPE_EDIT -> swipeEdit(placeItem)
                    SWIPE_DELETE -> swipeDelete(placeItem)
                }

                adapter.notifyDataSetChanged()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun swipeEdit(placeItem: PlaceModel) {
        findNavController().navigate(
            PlacesFragmentDirections.actionPlacesFragmentToAddPlacesFragment(placeItem)
        )
    }

    private fun swipeDelete(placeItem: PlaceModel) {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            icon(R.drawable.ic_delete_forever)
            title(R.string.dialog_delete_forever)
            message(R.string.dialog_delete_confirmation)
            positiveButton(R.string.dialog_confirmation) {
                placeViewModel.deleteItem(placeItem)
                snackBarMsg(requireView(), getString(R.string.snackbar_deleted_forever))
            }
            negativeButton(R.string.dialog_negative)
        }
    }

    /** ===================================== Options Menu. ===================================== **/

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_places, menu)

        val search = menu.findItem(R.id.menu_main_search).actionView as? SearchView
        search?.isSubmitButtonEnabled = true
        search?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_delete_all -> deleteAllPlaces()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllPlaces() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            icon(R.drawable.ic_delete_forever)
            title(R.string.dialog_delete_all)
            message(R.string.dialog_delete_confirmation)
            positiveButton(R.string.dialog_confirmation) {
                placeViewModel.deleteDatabase()
                snackBarMsg(requireView(), getString(R.string.snackbar_deleted_all))
            }
            negativeButton(R.string.dialog_negative)
        }
        recyclerView.itemAnimator = LandingAnimator().apply {
            addDuration = 300 // Milliseconds
        }
    }

    // Methods for search option.
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) searchTroughDatabase(query)
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) searchTroughDatabase(query)
        return true
    }

    private fun searchTroughDatabase(query: String) {
        placeViewModel.searchDatabase("%$query%").observe(this, { list ->
            list?.let { adapter.setData(it) }
        })
    }

    /** ===================================== Fragment exit/close ===================================== **/

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}