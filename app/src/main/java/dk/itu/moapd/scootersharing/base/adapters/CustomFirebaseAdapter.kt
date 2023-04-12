package dk.itu.moapd.scootersharing.base.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.base.databinding.ListRidesBinding
import dk.itu.moapd.scootersharing.base.models.Scooter

class CustomFirebaseAdapter(options: FirebaseRecyclerOptions<Scooter>)
    : FirebaseRecyclerAdapter<Scooter, CustomFirebaseAdapter.ViewHolder>
    (options) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListRidesBinding.inflate(inflater, parent, false)

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, scooter: Scooter)
        {
            holder.bind(scooter)
        }

    class ViewHolder(private val binding: ListRidesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scooter: Scooter) {
            binding.scooterName.text = scooter.name
            binding.scooterLocation.text = scooter.where
            binding.scooterTimestamp.text = scooter.timestamp.toString()
        }
    }
}