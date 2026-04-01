package com.menti.workoutTimer

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.menti.workoutTimer.adapter.WorkoutHistoryAdapter
import com.menti.workoutTimer.databinding.ActivityWorkoutHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Activity that displays workout history and statistics.
 */
class WorkoutHistoryActivity : AppCompatActivity() {

    private var _binding: ActivityWorkoutHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: WorkoutHistoryRepository
    private lateinit var adapter: WorkoutHistoryAdapter

    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.applyLocale(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityWorkoutHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize repository
        repository = WorkoutHistoryRepository(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Observe data
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutHistoryAdapter()
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WorkoutHistoryActivity)
            adapter = this@WorkoutHistoryActivity.adapter
        }
    }

    private fun observeData() {
        // Observe statistics
        lifecycleScope.launch {
            repository.getStatisticsFlow().collectLatest { stats ->
                binding.totalWorkoutsText.text = stats.totalWorkouts.toString()
                binding.totalTimeText.text = stats.formatTotalTime()
                binding.completionRateText.text = String.format(
                    Locale.getDefault(),
                    "%.0f%%",
                    stats.averageCompletionRate * 100
                )
            }
        }

        // Observe history entries
        lifecycleScope.launch {
            repository.getAllEntries().collectLatest { entries ->
                adapter.submitList(entries)

                // Show/hide empty state
                if (entries.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.historyRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                    binding.historyRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
