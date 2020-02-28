package com.liempo.furniture

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment

class CaptureArFragment : ArFragment() {

    override fun getAdditionalPermissions(): Array<String> {
        val additionalPermissions = super.getAdditionalPermissions()
        val permissionLength =
            additionalPermissions?.size ?: 0
        val permissions = Array(permissionLength + 1) { String() }

        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (permissionLength > 0) {
            System.arraycopy(
                additionalPermissions!!,
                0,
                permissions,
                1,
                additionalPermissions.size
            )
        }

        return permissions
    }
}