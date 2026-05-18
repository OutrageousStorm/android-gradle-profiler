package com.outrageousstorm.gradle

import java.io.File
import kotlin.math.roundToInt

data class GradleTask(
    val name: String,
    val duration: Long,  // ms
    val plugin: String,
    val deps: List<String> = emptyList()
)

class GradleProfiler(val profileHtml: String) {
    fun parse(): List<GradleTask> {
        val tasks = mutableListOf<GradleTask>()
        val content = File(profileHtml).readText()
        
        // Parse HTML table with tasks
        // Format: each row is <tr><td>taskname</td><td>duration</td></tr>
        val taskRegex = """<tr><td>([^<]+)</td><td>([\d.]+)s</td><td>([^<]*)</td></tr>""".toRegex()
        taskRegex.findAll(content).forEach { match ->
            val (name, duration, plugin) = match.destructured
            tasks.add(GradleTask(
                name = name.trim(),
                duration = (duration.toFloat() * 1000).toLong(),
                plugin = plugin.trim()
            ))
        }
        
        return tasks.sortedByDescending { it.duration }
    }
    
    fun criticalPath(): List<GradleTask> {
        val tasks = parse()
        return tasks.take((tasks.size * 0.2).roundToInt().coerceAtLeast(5))
    }
    
    fun report(): String {
        val tasks = parse()
        val total = tasks.sumOf { it.duration }
        
        return buildString {
            appendLine("=== Gradle Build Profile ===\n")
            appendLine("Total time: ${total / 1000}s\n")
            
            appendLine("Top 10 slowest tasks:")
            tasks.take(10).forEachIndexed { i, task ->
                val pct = (task.duration * 100 / total).toInt()
                appendLine("  ${i+1}. ${task.name.padEnd(40)} ${task.duration}ms ($pct%)")
            }
            
            appendLine("\nCritical path (blocking tasks):")
            criticalPath().forEach { task ->
                appendLine("  • ${task.name} (${task.duration}ms)")
            }
            
            appendLine("\nOptimization tips:")
            appendLine("  1. Use KSP instead of Kapt if using annotation processors")
            appendLine("  2. Enable --parallel in gradle.properties")
            appendLine("  3. Set org.gradle.parallel=true")
            appendLine("  4. Increase org.gradle.workers.max=8 (or CPU count)")
            appendLine("  5. Enable build cache: org.gradle.caching=true")
        }
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar profiler.jar <build-profile.html>")
        return
    }
    
    val profiler = GradleProfiler(args[0])
    println(profiler.report())
}
