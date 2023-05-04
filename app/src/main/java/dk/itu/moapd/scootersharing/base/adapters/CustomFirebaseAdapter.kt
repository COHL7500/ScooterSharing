package dk.itu.moapd.scootersharing.base.adapters
import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.base.databinding.ListRidesBinding
import dk.itu.moapd.scootersharing.base.models.Scooter

class CustomFirebaseAdapter(options: FirebaseRecyclerOptions<Scooter>)
    : FirebaseRecyclerAdapter<Scooter, CustomFirebaseAdapter.ViewHolder>
    (options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListRidesBinding.inflate(inflater, parent, false)

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, scooter: Scooter) {
            holder.bind(scooter)
        }

    class ViewHolder(private val binding: ListRidesBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(scooter: Scooter) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("scooters").orderByChild("name").equalTo(scooter.name).get().addOnSuccessListener {
                binding.scooterName.text = scooter.name
                binding.scooterLocation.text = scooter.startLatitude.toString() + ", " + scooter.startLongitude.toString()
                binding.scooterTimestamp.text = scooter.timestamp.toString()

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