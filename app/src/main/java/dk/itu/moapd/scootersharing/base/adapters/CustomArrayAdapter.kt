/*

package dk.itu.moapd.scootersharing.base.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.databinding.ListRidesBinding

class CustomArrayAdapter(val data: ArrayList<Scooter>) :
    RecyclerView.Adapter<CustomArrayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListRidesBinding.inflate(
            inflater, parent, false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = data.size

    fun addItem(item: Scooter) {
        data.add(item)
        notifyItemInserted(data.size)
    }

    fun removeAt(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scooter = data[position]
        holder.bind(scooter)
    }

    class ViewHolder(private val binding: ListRidesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Scooter) {
            binding.scooterName.text = data.name
            binding.scooterLocation.text = data.where
            binding.scooterTimestamp.text = data.timestamp.toString()
        }
    }
}

 */