package com.menti.workoutTimer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.menti.workoutTimer.R
import com.menti.workoutTimer.databinding.ItemWorkoutHistoryBinding
import com.menti.workoutTimer.model.WorkoutHistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter for displaying workout history entries.
 */
class WorkoutHistoryAdapter : ListAdapter<WorkoutHistoryEntry, WorkoutHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemWorkoutHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        fun bind(entry: WorkoutHistoryEntry) {
            // Date
            binding.historyDateText.text = dateFormat.format(Date(entry.date))

            // Status
            if (entry.completed) {
                binding.historyStatusText.text = binding.root.context.getString(R.string.completed)
                binding.historyStatusText.setTextColor(
                    binding.root.context.getColor(R.color.workout_color)
                )
            } else {
                binding.historyStatusText.text = binding.root.context.getString(R.string.incomplete)
                binding.historyStatusText.setTextColor(
                    binding.root.context.getColor(R.color.rest_color)
                )
            }

            // Rounds
            binding.historyRoundsText.text = "${entry.completedRounds}/${entry.rounds}"

            // Workout and rest duration
            binding.historyWorkoutTimeText.text = entry.formatWorkoutDuration()
            binding.historyRestTimeText.text = entry.formatRestDuration()

            // Total time
            binding.historyTotalTimeText.text = entry.formatTotalTime()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WorkoutHistoryEntry>() {
        override fun areItemsTheSame(
            oldItem: WorkoutHistoryEntry,
            newItem: WorkoutHistoryEntry
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: WorkoutHistoryEntry,
            newItem: WorkoutHistoryEntry
        ): Boolean {
            return oldItem == newItem
        }
    }
}
