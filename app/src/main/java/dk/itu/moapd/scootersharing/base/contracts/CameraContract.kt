package dk.itu.moapd.scootersharing.base.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract

class CameraContract : ActivityResultContract<Unit, Bitmap?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }

    // getParcelable is deprecated. We can't find the alternative.
    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        return if (resultCode == Activity.RESULT_OK) {
            return intent?.extras?.getParcelable("data") as Bitmap?
        } else {
            Log.e("CAMERA_PARSE_NULL", "CameraContract parsed null")
            null
        }
    }
}