package dk.itu.moapd.scootersharing.base.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class CameraContract : ActivityResultContract<Unit,String>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.data.toString()
        } else {
            return "code does not work"
        }
    }
}