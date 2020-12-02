package com.anibalventura.myplaces.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
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
import com.anibalventura.myplaces.databinding.FragmentAddPlaceBinding
import com.anibalventura.myplaces.utils.AddressLatLong
import com.anibalventura.myplaces.utils.Constants.CAMERA_REQUEST_CODE
import com.anibalventura.myplaces.utils.Constants.GALLERY_REQUEST_CODE
import com.anibalventura.myplaces.utils.Constants.PLACE_REQUEST_CODE
import com.anibalventura.myplaces.utils.saveImageToInternalStorage
import com.anibalventura.myplaces.utils.snackBarMsg
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
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
    private lateinit var currentItem: PlaceModel
    private lateinit var placeItem: PlaceModel

    private val args by navArgs<AddPlaceFragmentArgs>()

    private val adapter: PlaceAdapter by lazy { PlaceAdapter() }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var image: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        binding.addPlaceFragment = this
        binding.lifecycleOwner = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (arguments != null) setEditPlace() else setAddPlace()

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key))
        }

        onBackPressed()

        return binding.root
    }

    /** ===================================== Permissions. ===================================== **/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode == Activity.RESULT_OK) {
            requestCode == GALLERY_REQUEST_CODE -> {
                try {
                    if (data!!.data != null) {
                        val dataUri = data.data

                        @Suppress("DEPRECATION")
                        val pickedImageBitmap = MediaStore.Images.Media.getBitmap(
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
            requestCode == PLACE_REQUEST_CODE -> {
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                binding.etLocation.setText(place.address)
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
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
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(pickImageIntent, GALLERY_REQUEST_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>, token: PermissionToken
            ) = permissionDeniedDialog()

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
            ) = permissionDeniedDialog()

        }).onSameThread().check()
    }

    private fun getLocation() {
        Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) requestLocationData()
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>, token: PermissionToken
            ) = permissionDeniedDialog()

        }).onSameThread().check()
    }

    private fun permissionDeniedDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.dialog_permission_denied)
            message(R.string.dialog_permission_denied_msg)
            negativeButton(R.string.dialog_cancel)
            positiveButton(R.string.dialog_go_to_settings) {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", "packageName", null)
                    ContextCompat.startActivity(context, intent, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                    snackBarMsg(view, e.printStackTrace().toString())
                }
            }
        }
    }

    /** ===================================== Get Location. ===================================== **/

    @RequiresApi(Build.VERSION_CODES.P)
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isLocationEnabled
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.numUpdates = 1

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallBack(),
            Looper.myLooper()
        )
    }

    private fun locationCallBack() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val lastLocation: Location = locationResult!!.lastLocation
            latitude = lastLocation.latitude
            longitude = lastLocation.longitude

            val getAddress = AddressLatLong(requireContext(), latitude, longitude)
            getAddress.setAddressListener(object : AddressLatLong.AddressListener {
                override fun onAddressFound(address: String) {
                    binding.etLocation.setText(address)
                }

                override fun onError() {
                    snackBarMsg(requireView(), getString(R.string.snackbar_location_error))
                }
            })

            getAddress.getAddress()
        }
    }

    /** ===================================== Add place. ===================================== **/

    private fun setAddPlace() {
        binding.btnSave.setOnClickListener { savePlace() }
    }

    fun pickDate() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            datePicker { _, date ->
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.etDate.setText(sdf.format(date.time).toString())
            }
        }
    }

    fun addLocation() {
        try {
            val fields =
                listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())

            startActivityForResult(intent, PLACE_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getCurrentLocation() {
        when (!isLocationEnabled()) {
            true -> {
                snackBarMsg(requireView(), getString(R.string.snackbar_location_off))
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            false -> getLocation()
        }
    }

    fun addImage() {
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

    private fun savePlace() {
        when {
            binding.etTitle.text.isNullOrEmpty() ->
                snackBarMsg(requireView(), getString(R.string.snackbar_empty_title))
            binding.etDescription.text.isNullOrEmpty() ->
                snackBarMsg(requireView(), getString(R.string.snackbar_empty_description))
            binding.etDate.text.isNullOrEmpty() ->
                snackBarMsg(requireView(), getString(R.string.snackbar_empty_date))
            binding.etLocation.text.isNullOrEmpty() ->
                snackBarMsg(requireView(), getString(R.string.snackbar_empty_location))
            image == null ->
                snackBarMsg(requireView(), getString(R.string.snackbar_empty_image))
            else -> {
                val item = PlaceModel(
                    0, binding.etTitle.text.toString(),
                    binding.etDescription.text.toString(),
                    binding.etDate.text.toString(),
                    binding.etLocation.text.toString(),
                    latitude, longitude, image.toString()
                )

                placeViewModel.insertItem(item)
                snackBarMsg(requireView(), getString(R.string.snackbar_add_place))
                findNavController().popBackStack()
            }
        }
    }


    /** ===================================== Edit Place. ===================================== **/

    private fun setEditPlace() {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_place)
        binding.etTitle.setText(args.currentItem.title)
        binding.etDescription.setText(args.currentItem.description)
        binding.etDate.setText(args.currentItem.date)
        binding.etLocation.setText(args.currentItem.location)
        binding.ivPlaceImage.setImageURI(Uri.parse(args.currentItem.image))
        binding.btnSave.text = getString(R.string.edit_place_btn_update)
        binding.btnSave.setOnClickListener { updatePlace() }

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
    }

    private fun updatePlace() {
        if (image == null) image = Uri.parse(args.currentItem.image)

        currentItem = PlaceModel(
            args.currentItem.id,
            binding.etTitle.text.toString(),
            binding.etDescription.text.toString(),
            binding.etDate.text.toString(),
            binding.etLocation.text.toString(),
            latitude, longitude, image.toString()
        )

        placeViewModel.updateItem(currentItem)

        snackBarMsg(requireView(), getString(R.string.snackbar_place_update))
        findNavController().popBackStack()
    }

    /** =================================== Fragment exit/close =================================== **/

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                        title(R.string.dialog_discard)
                        message(R.string.dialog_discard_confirmation)
                        positiveButton(R.string.dialog_confirmation) {
                            snackBarMsg(requireView(), getString(R.string.snackbar_place_not_saved))
                            findNavController().popBackStack()
                        }
                        negativeButton(R.string.dialog_negative)
                    }
                }
            })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}