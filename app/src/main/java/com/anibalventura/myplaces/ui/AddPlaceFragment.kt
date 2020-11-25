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
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.list.listItems
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.data.model.PlaceModel
import com.anibalventura.myplaces.data.viewmodel.PlaceViewModel
import com.anibalventura.myplaces.databinding.FragmentAddPlaceBinding
import com.anibalventura.myplaces.utils.Constants.CAMERA_REQUEST_CODE
import com.anibalventura.myplaces.utils.Constants.GALLERY_REQUEST_CODE
import com.anibalventura.myplaces.utils.Constants.PLACE_ADDING
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

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by viewModels()

    private var image: Uri? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        binding.addPlaceFragment = this
        binding.lifecycleOwner = this

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
                        val pickedImageBitmap =
                            MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver, dataUri
                            )

                        image = saveImageToInternalStorage(requireContext(), pickedImageBitmap)
                        binding.ivPlaceImage.setImageURI(dataUri)
                    }
                } catch (e: IOException) {
                    snackBarMsg(requireView(), e.printStackTrace().toString())
                }
            }
            requestCode == CAMERA_REQUEST_CODE -> {
                try {
                    val dataBitmap = data!!.extras!!.get("data") as Bitmap

                    image = saveImageToInternalStorage(requireContext(), dataBitmap)
                    binding.ivPlaceImage.setImageBitmap(dataBitmap)
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

    /** ===================================== Add place. ===================================== **/

    fun pickDate() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            datePicker { _, date ->
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.etDate.setText(sdf.format(date.time).toString())
            }
        }
    }

    fun addImage() {
        val actionsItems = listOf("Add from gallery", "Capture from camera")

        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.add_place_add_image)
            listItems(items = actionsItems) { _, index, _ ->
                when (index) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
        }
    }

    fun savePlace() {
        when {
            binding.etTitle.text.isNullOrEmpty() -> snackBarMsg(
                requireView(), "Please enter a Title."
            )
            binding.etDescription.text.isNullOrEmpty() -> snackBarMsg(
                requireView(), "Please enter a Description."
            )
            binding.etDate.text.isNullOrEmpty() -> snackBarMsg(
                requireView(), "Please enter a Date."
            )
            binding.etLocation.text.isNullOrEmpty() -> snackBarMsg(
                requireView(), "Please enter a Location."
            )
            image == null -> snackBarMsg(requireView(), "Please add an Image.")
            else -> {
                val placeModel = PlaceModel(
                    0, binding.etTitle.text.toString(),
                    binding.etDescription.text.toString(),
                    binding.etDate.text.toString(),
                    binding.etLocation.text.toString(),
                    longitude, latitude, image.toString()
                )

                placeViewModel.insertItem(placeModel)
                findNavController().navigate(R.id.action_addPlaceFragment_to_placesFragment)
                snackBarMsg(requireView(), "Place added.")
            }
        }
    }

    /** ===================================== Fragment exit/close ===================================== **/

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    discardDialog(PLACE_ADDING, requireContext(), requireView())
                }
            })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}