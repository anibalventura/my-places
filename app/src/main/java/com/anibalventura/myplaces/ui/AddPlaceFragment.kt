package com.anibalventura.myplaces.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.list.listItems
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.databinding.FragmentAddPlaceBinding
import com.anibalventura.myplaces.utils.snackBarMsg
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        pickDate()
        addImage()
        savePlace()

        return binding.root
    }

    /** ===================================== Complete fields and save. ===================================== **/

    private fun pickDate() {
        binding.etDate.setOnClickListener {
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                datePicker { _, date ->
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    binding.etDate.setText(sdf.format(date.time).toString())
                }
            }
        }
    }

    private fun addImage() {
        val actionsItems = listOf("Add from gallery", "Capture from camera")

        binding.tvAddImage.setOnClickListener {
            MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                title(R.string.add_place_add_image)
                listItems(items = actionsItems) { _, index, _ ->
                    when (index) {
                        0 -> chooseImageFromGallery()
                        1 -> snackBarMsg(requireView(), "Test 1")
                    }
                }
            }
        }
    }

    private fun chooseImageFromGallery() {

        Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    snackBarMsg(requireView(), getString(R.string.permission_granted_storage))
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>, token: PermissionToken
            ) {
                MaterialDialog(requireContext()).show {
                    title(R.string.dialog_permission_denied)
                    message(R.string.dialog_permission_denied_msg)
                    negativeButton(R.string.dialog_cancel)
                    positiveButton(R.string.dialog_go_to_settings) {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", "packageName", null)
                            startActivity(intent)
                        } catch (e: Exception) {
                            snackBarMsg(requireView(), e.printStackTrace().toString())
                        }
                    }
                }
            }

        }).onSameThread().check()

    }

    private fun savePlace() {
        binding.btnSave.setOnClickListener {
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