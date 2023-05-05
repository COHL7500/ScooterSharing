package dk.itu.moapd.scootersharing.base.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dk.itu.moapd.scootersharing.base.contracts.CameraContract
import dk.itu.moapd.scootersharing.base.databinding.ListRidesBinding
import dk.itu.moapd.scootersharing.base.fragments.ListRidesFragment
import dk.itu.moapd.scootersharing.base.fragments.MainFragment
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.utils.GeoClass

class CustomFirebaseAdapter(options: FirebaseRecyclerOptions<Scooter>) : FirebaseRecyclerAdapter<Scooter, CustomFirebaseAdapter.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListRidesBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, scooter: Scooter) {
        holder.bind(scooter)
    }

    class ViewHolder(private val binding: ListRidesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scooter: Scooter) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("scooters").orderByChild("name").equalTo(scooter.name).get().addOnSuccessListener {
                binding.scooterName.text = scooter.name
                binding.scooterLocation.text = GeoClass.getAddress(binding.root.context,scooter.startLatitude,scooter.startLongitude)

                val storage = Firebase.storage("gs://moapd-2023-6e1fd.appspot.com").reference
                val imageStr = "${it.children.first().key.toString()}.jpg"
                val imageRef = storage.child("last_photo_scooters").child(imageStr)

                imageRef.downloadUrl?.addOnSuccessListener {
                    Glide.with(itemView.context)
                        .load(it)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(binding.scooterImage)
                }?.addOnFailureListener {
                    storage.child("scooter_thumbnail.png").downloadUrl.addOnSuccessListener {
                        Glide.with(itemView.context)
                            .load(it)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .centerCrop()
                            .into(binding.scooterImage)
                    }
                }
            }
        }
    }
}