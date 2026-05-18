package com.outrageousstorm.gradle.profiler

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class BuildMetrics(
    val totalTime: Long,
    val tasks: List<TaskMetric>,
    val parallelWorkers: Int,
    val timestamp: String
)

data class TaskMetric(
    val name: String,
    val duration: Long,
    val percentage: Double,
    val status: String
)

class GradleProfiler(private val buildDir: String = ".") {
    
    fun parseGradleBuild(profilePath: String): BuildMetrics {
        val profileFile = File(profilePath)
        if (!profileFile.exists()) throw IllegalArgumentException("Profile file not found: $profilePath")
        
        val content = profileFile.readText()
        
        // Parse build-profile.json (Gradle 7.0+)
        val totalTime = extractTotalTime(content)
        val tasks = extractTasks(content)
        val workers = extractWorkerCount(content)
        
        return BuildMetrics(
            totalTime = totalTime,
            tasks = tasks,
            parallelWorkers = workers,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        )
    }
    
    private fun extractTotalTime(json: String): Long {
        val regex = """"totalBuildTime":\s*(\d+)""".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    }
    
    private fun extractTasks(json: String): List<TaskMetric> {
        val tasks = mutableListOf<TaskMetric>()
        val taskPattern = """"taskName":\s*"([^"]+)".*?"duration":\s*(\d+)""".toRegex()
        
        taskPattern.findAll(json).forEach { match ->
            val name = match.groupValues[1]
            val duration = match.groupValues[2].toLong()
            if (duration > 0) {
                tasks.add(TaskMetric(
                    name = name,
                    duration = duration,
                    percentage = 0.0,
                    status = "completed"
                ))
            }
        }
        
        // Calculate percentages
        val total = tasks.sumOf { it.duration }
        return tasks.map { it.copy(percentage = ((it.duration.toDouble() / total) * 100).roundToInt() / 100.0) }
            .sortedByDescending { it.duration }
    }
    
    private fun extractWorkerCount(json: String): Int {
        val regex = """"parallelWorkers":\s*(\d+)""".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }
    
    fun printSummary(metrics: BuildMetrics) {
        println("\n📊 Gradle Build Profile")
        println("${"─".repeat(50)}")
        println("Total build time: ${formatTime(metrics.totalTime)}")
        println("Parallel workers: ${metrics.parallelWorkers}")
        println("Timestamp: ${metrics.timestamp}")
        println("\nTop 10 slowest tasks:")
        metrics.tasks.take(10).forEach { task ->
            val bar = "█".repeat((task.percentage / 5).toInt()) + "░".repeat(20 - (task.percentage / 5).toInt())
            println("  $bar ${task.name.padEnd(30)} ${formatTime(task.duration)} (${task.percentage}%)")
        }
    }
    
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val mins = seconds / 60
        val secs = seconds % 60
        return when {
            mins > 0 -> "$mins min $secs sec"
            else -> "$secs sec"
        }
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: gradle-profiler <build-profile.json>")
        return
    }
    
    try {
        val profiler = GradleProfiler()
        val metrics = profiler.parseGradleBuild(args[0])
        profiler.printSummary(metrics)
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
