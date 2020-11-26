package com.anibalventura.myplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.*

class AddressLatLong(
    context: Context,
    private val latitude: Double,
    private val longitude: Double
) {
    private lateinit var addressListener: AddressListener

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    private var result: String = ""

    fun getAddress(): String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addressList.isNullOrEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()

                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                sb.deleteCharAt(sb.length - 1)

                result = sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        when (result) {
            "" -> addressListener.onError()
            else -> addressListener.onAddressFound(result)
        }

        return result
    }

    fun setAddressListener(addressListenerSet: AddressListener) {
        addressListener = addressListenerSet
    }

    interface AddressListener {
        fun onAddressFound(address: String)
        fun onError()
    }
}