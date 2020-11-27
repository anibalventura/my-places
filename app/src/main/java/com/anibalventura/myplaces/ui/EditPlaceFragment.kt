package com.anibalventura.myplaces.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.list.listItems
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.adapters.PlaceAdapter
import com.anibalventura.myplaces.data.model.PlaceModel
import com.anibalventura.myplaces.data.viewmodel.PlaceViewModel
import com.anibalventura.myplaces.databinding.FragmentEditPlaceBinding
import com.anibalventura.myplaces.utils.Constants.CAMERA_REQUEST_CODE
import com.anibalventura.myplaces.utils.Constants.GALLERY_REQUEST_CODE
import com.anibalventura.myplaces.utils.Constants.PLACE_EDITING
import com.anibalventura.myplaces.utils.discardDialog
import com.anibalventura.myplaces.utils.permissionDeniedDialog
import com.anibalventura.myplaces.utils.saveImageToInternalStorage
import com.anibalventura.myplaces.utils.snackBarMsg
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditPlaceFragment : Fragment() {

    private var _binding: FragmentEditPlaceBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by viewModels()

    private lateinit var currentItem: PlaceModel
    private lateinit var placeItem: PlaceModel
    private var image: Uri? = null

    private val args by navArgs<EditPlaceFragmentArgs>()

    private val adapter: PlaceAdapter by lazy { PlaceAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditPlaceBinding.inflate(inflater, container, false)
        binding.args = args
        binding.editPlaceFragment = this
        binding.lifecycleOwner = this

        placeViewModel.getDatabase.observe(viewLifecycleOwner, { data ->
            placeViewModel.checkIfPlacesIsEmpty(data)
            adapter.setData(data)
        })

        placeItem = PlaceModel(
            args.currentItem.id,
            args.currentItem.title,
            args.currentItem.description,
            args.currentItem.date,
            args.currentItem.location,
            args.currentItem.latitude,
            args.currentItem.longitude,
            args.currentItem.image
        )

        onBackPressed()

        return binding.root
    }

    /** ===================================== Handle Permissions. ===================================== **/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode == Activity.RESULT_OK) {
            requestCode == GALLERY_REQUEST_CODE -> {
                try {
                    if (data!!.data != null) {
                        val dataUri = data.data
                        @Suppress("DEPRECATION") val pickedImageBitmap =
                            MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver, dataUri
                            )

                        image = saveImageToInternalStorage(requireContext(), pickedImageBitmap)
                        binding.ivEditImage.setImageURI(dataUri)
                    }
                } catch (e: IOException) {
                    snackBarMsg(requireView(), e.printStackTrace().toString())
                }
            }
            requestCode == CAMERA_REQUEST_CODE -> {
                try {
                    val dataBitmap = data!!.extras!!.get("data") as Bitmap

                    image = saveImageToInternalStorage(requireContext(), dataBitmap)
                    binding.ivEditImage.setImageBitmap(dataBitmap)
                } catch (e: IOException) {
                    snackBarMsg(requireView(), e.printStackTrace().toString())
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val pickImageIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(pickImageIntent, GALLERY_REQUEST_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>, token: PermissionToken
            ) {
                permissionDeniedDialog(requireContext(), requireView())
            }

        }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePhotoIntent, CAMERA_REQUEST_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>, token: PermissionToken
            ) {
                permissionDeniedDialog(requireContext(), requireView())
            }

        }).onSameThread().check()
    }

    /** ===================================== Edit place. ===================================== **/

    fun pickDate() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            datePicker { _, date ->
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.etEditDate.setText(sdf.format(date.time).toString())
            }
        }
    }

    fun editImage() {
        val actionsItems =
            listOf(getString(R.string.image_from_gallery), getString(R.string.image_from_camera))

        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.place_add_image)
            listItems(items = actionsItems) { _, index, _ ->
                when (index) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
        }
    }

    fun updatePlace() {
        if (image == null) {
            image = Uri.parse(args.currentItem.image)
        }

        currentItem = PlaceModel(
            args.currentItem.id,
            binding.etEditTitle.text.toString(),
            binding.etEditDescription.text.toString(),
            binding.etEditDate.text.toString(),
            binding.etEditLocation.text.toString(),
            args.currentItem.latitude,
            args.currentItem.longitude,
            image.toString()
        )

        placeViewModel.updateItem(currentItem)

        snackBarMsg(requireView(), getString(R.string.snackbar_place_update))
        findNavController().navigate(R.id.action_editPlaceFragment_to_placesFragment)
    }

    /** ===================================== Fragment exit/close ===================================== **/

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    currentItem = PlaceModel(
                        args.currentItem.id,
                        binding.etEditTitle.text.toString(),
                        binding.etEditDescription.text.toString(),
                        binding.etEditDate.text.toString(),
                        binding.etEditLocation.text.toString(),
                        args.currentItem.latitude,
                        args.currentItem.longitude,
                        image.toString()
                    )
                    when {
                        currentItem != placeItem -> {
                            discardDialog(PLACE_EDITING, requireContext(), requireView())
                        }
                        else -> findNavController().navigate(R.id.action_editPlaceFragment_to_placesFragment)
                    }
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}