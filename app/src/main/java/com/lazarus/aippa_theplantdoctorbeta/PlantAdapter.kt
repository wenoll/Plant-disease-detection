package com.lazarus.aippa_theplantdoctorbeta

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.lazarus.aippa_theplantdoctorbeta.databinding.ItemPlantBinding

class PlantAdapter(private var plants: List<Plant>) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(plants[position])
    }

    override fun getItemCount() = plants.size

    fun updateData(newPlants: List<Plant>) {
        plants = newPlants
        notifyDataSetChanged() // In a real app, use DiffUtil for better performance
    }

    class PlantViewHolder(private val binding: ItemPlantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(plant: Plant) {
            binding.tvPlantName.text = plant.name
            binding.tvPlantVariety.text = plant.variety
            binding.tvPlantLocation.text = plant.location

            plant.imagePath?.let { path ->
                binding.ivPlantIcon.load(path) {
                    crossfade(true)
                    placeholder(R.drawable.ic_plant_placeholder)
                    error(R.drawable.ic_broken_image)
                }
            } ?: binding.ivPlantIcon.setImageResource(R.drawable.ic_plant_placeholder)


            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, PlantDetailActivity::class.java).apply {
                    putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plant.id)
                }
                context.startActivity(intent)
            }
        }
    }
}