#!/usr/bin/env python3
"""
profiler.py -- Parse Gradle build logs and show slowest tasks
Usage: ./gradlew build 2>&1 | python3 profiler.py
       Or: python3 profiler.py build.log
"""
import sys, re
from collections import defaultdict
from datetime import timedelta

def parse_gradle_log(lines):
    tasks = []
    for line in lines:
        # Pattern: > Task :app:compileDebugJavaWithJavac FAILED in 2s 345ms
        m = re.search(r'> Task (:[\w:]+)\s+(\w+)\s+in\s+([\d.]+)(ms|s)', line)
        if m:
            task_name = m.group(1)
            status = m.group(2)
            duration = float(m.group(3))
            unit = m.group(4)
            if unit == 's':
                duration *= 1000
            tasks.append({'name': task_name, 'duration': duration, 'status': status})
    return tasks

def format_duration(ms):
    if ms < 1000:
        return f"{ms:.0f}ms"
    return f"{ms/1000:.2f}s"

def main():
    if len(sys.argv) > 1:
        with open(sys.argv[1]) as f:
            lines = f.readlines()
    else:
        lines = sys.stdin.readlines()

    tasks = parse_gradle_log(lines)
    if not tasks:
        print("No Gradle tasks found in log.")
        return

    tasks.sort(key=lambda t: t['duration'], reverse=True)
    total_time = sum(t['duration'] for t in tasks)

    print(f"\n⚙️  Gradle Build Profile\n")
    print(f"{'Task':<45} {'Time':<10} {'% of Total'}")
    print("─" * 65)
    for t in tasks[:20]:
        pct = (t['duration'] / total_time * 100) if total_time else 0
        print(f"{t['name']:<45} {format_duration(t['duration']):<10} {pct:>5.1f}%")

    print(f"\n{'Total build time:':<45} {format_duration(total_time)}")
    print(f"{'Tasks profiled:':<45} {len(tasks)}")

if __name__ == "__main__":
    main()
