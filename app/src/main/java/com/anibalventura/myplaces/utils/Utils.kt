package com.anibalventura.myplaces.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.anibalventura.myplaces.App
import com.anibalventura.myplaces.R
import com.anibalventura.myplaces.utils.Constants.PLACE_ADDING
import com.anibalventura.myplaces.utils.Constants.PLACE_EDITING
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

val res = App.resourses!!

fun snackBarMsg(view: View, message: String) {
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.show()
}

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap): Uri {
    val wrapper = ContextWrapper(context)
    var file = wrapper.getDir(Constants.IMAGE_DIRECTORY, Context.MODE_PRIVATE)
    file = File(file, "${UUID.randomUUID()}.jpg")

    try {
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return Uri.parse(file.absolutePath)
}

fun permissionDeniedDialog(context: Context, view: View) {
    MaterialDialog(context).show {
        title(R.string.dialog_permission_denied)
        message(R.string.dialog_permission_denied_msg)
        negativeButton(R.string.dialog_cancel)
        positiveButton(R.string.dialog_go_to_settings) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", "packageName", null)
                startActivity(context, intent, null)
            } catch (e: Exception) {
                e.printStackTrace()
                snackBarMsg(view, e.printStackTrace().toString())
            }
        }
    }
}

fun discardDialog(mode: String, context: Context, view: View) {
    MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
        title(R.string.dialog_discard)
        message(R.string.dialog_discard_confirmation)
        positiveButton(R.string.dialog_confirmation) {
            when (mode) {
                PLACE_EDITING -> {
                    view.findNavController()
                        .navigate(R.id.action_editPlaceFragment_to_placesFragment)
                    snackBarMsg(view, res.getString(R.string.dialog_discard_successful))
                }
                PLACE_ADDING -> {
                    view.findNavController()
                        .navigate(R.id.action_addPlaceFragment_to_placesFragment)
                    snackBarMsg(view, res.getString(R.string.dialog_place_not_saved))
                }
            }
        }
        negativeButton(R.string.dialog_negative)
    }
}