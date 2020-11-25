package com.anibalventura.myplaces.utils

import androidx.recyclerview.widget.ItemTouchHelper

object Constants {

    // Permissions
    const val GALLERY_REQUEST_CODE = 1
    const val CAMERA_REQUEST_CODE = 2

    // SwipeItem.
    const val SWIPE_DELETE = ItemTouchHelper.LEFT
    const val SWIPE_EDIT = ItemTouchHelper.RIGHT

    // Discard.
    const val PLACE_ADDING = "place_adding"
    const val PLACE_EDITING = "place_editing"

    const val IMAGE_DIRECTORY = "MyPlacesImages"
}