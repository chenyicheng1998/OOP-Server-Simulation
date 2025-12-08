User Guide

📋 Documentation Description

This guide provides detailed explanations of the cloud computing service queue simulation system, including interface elements, parameter meanings, and usage instructions.

Related Documents:

SCENARIO_GUIDE.md - Scenario-based application examples guide

STATISTICS_IMPLEMENTATION.md - Technical implementation details (for developers)

📊 Interface Layout Overview

Top Control Panel

▶️ Start / ⏸ Pause / ▶️ Resume / ⏹ Stop / 🔄 Reset Buttons

⏱ Simulation Time Display

🚀 Speed Control Slider (0.1x - 100x)

Central Visualization Area

System process visualization canvas

Real-time display of task flow and service point status

Right Statistics Panel

Overall Statistics

Queue Statistics

User Type Statistics

Task Type Statistics

Bottom Configuration Panel

Simulation parameter configuration

Database connection status

Configuration save/load functionality

Historical simulation records table

📊 Right Statistics Panel Detailed Description

1. Overall Statistics

Tasks Arrived

Meaning: Total number of tasks that have arrived in the system

Purpose: Monitor system load and understand how many tasks have entered the system

Tasks Completed

Meaning: Total number of tasks completed by the system

Purpose: Compare with tasks arrived to understand system processing progress

If completed tasks are much fewer than arrived tasks, the system may have backlog

Avg System Time

Meaning: Average total time each task spends from arrival to completion (seconds)

Formula:

Average System Time = Total system time of all completed tasks ÷ Number of completed tasks

System time includes:

Waiting time in all queues

Service time at each service point

Total process duration

Purpose:

Measure system response speed

Smaller values indicate faster system processing and better user experience

Larger values may indicate system bottlenecks (long queues, insufficient servers)

Example:

15.30s → Average task completion time 15.3 seconds

2.50s → Average task completion time 2.5 seconds (high efficiency)

45.00s → Average task completion time 45 seconds (possible overload)

Throughput

Meaning: Number of tasks completed per second (tasks/sec)

Formula:

Throughput = Total completed tasks ÷ Simulation run time

Purpose:

Measure system processing capacity

Higher value indicates more tasks processed per unit time

Example:

0.500/s → 0.5 tasks per second (1 task every 2s)

2.000/s → 2 tasks per second

0.050/s → 0.05 tasks per second (1 task every 20s, low efficiency)

Factors affecting throughput:

Number of CPU/GPU nodes

Task arrival rate

Processing speed of service points

Queue management strategy

2. Queue Statistics

Meaning: Real-time display of queue status and utilization at all service points

Displays:

📦 Data Storage

Queue: Current number of waiting tasks

Busy: Number of busy servers / total servers (1 server)

Max: Maximum queue length during simulation

Utilization: Server utilization percentage

🔍 Classification Service

Queue, Busy, Max, Utilization same as above

💻 CPU Queue

Waiting: Tasks waiting for CPU node assignment

Max: Historical max waiting queue length

💻 CPU Compute

Queue: Tasks in CPU queue

Busy: Busy nodes / total nodes

Max: Historical max queue length

Utilization: CPU node utilization %

Served: Total tasks completed

🎮 GPU Queue

Waiting: Tasks waiting for GPU node assignment

Max: Historical max waiting queue length

🎮 GPU Compute

Queue, Busy, Max, Utilization same as CPU Compute

Served: Total tasks completed

💾 Result Storage

Queue, Busy, Max, Utilization same as above

Purpose:

Identify bottlenecks

Optimize resources

Monitor system in real-time

Utilization interpretation:

< 30% → 💡 Resources idle

30%-90% → ✅ Good utilization, system balanced

> 90% → ⚠️ Overloaded, consider adding servers

3. User Type Statistics

Meaning: Analyze task completion and service quality by user type

User types:

👤 NORMAL - lowest priority

⭐ PERSONAL_VIP - medium priority

⭐⭐ ENTERPRISE_VIP - highest priority

Displays:

Completed: Tasks completed

Percentage: % of total completed tasks

Avg System Time: Average system time for user type

Priority Effect:

Shows speed improvement for Enterprise VIP vs Normal Users

Purpose:

Verify VIP priority effectiveness

Analyze service quality differences between user groups

Assess differentiated pricing strategy

Example:

👤 NORMAL Users: Completed: 150 (50%), Avg System Time: 18.45s

⭐ PERSONAL VIP: Completed: 90 (30%), Avg System Time: 12.23s

⭐⭐ ENTERPRISE VIP: Completed: 60 (20%), Avg System Time: 8.76s

Priority Effect: Enterprise VIP is 52.5% faster

4. Task Type Statistics

Meaning: Analyze processing and resource usage by task type

Task types:

💻 CPU Tasks - computation tasks requiring CPU

🎮 GPU Tasks - graphics/deep learning tasks requiring GPU

Displays:

💻 CPU Tasks

Completed, Percentage, Avg System Time, Node Utilization

🎮 GPU Tasks

Completed, Percentage, Avg System Time, Node Utilization

Resource Efficiency:

✅ Balanced nodes (30%-90% utilization)

⚠️ Overloaded nodes (>90% utilization) - consider adding nodes

💡 Idle nodes (<30% utilization) - consider reducing nodes or increasing load

Purpose:

Evaluate CPU/GPU resource efficiency

Optimize hardware cost

Guide resource adjustment

Example:

💻 CPU Tasks: Completed: 210 (70%), Avg System Time: 15.23s, Node Utilization: 85.3%

🎮 GPU Tasks: Completed: 90 (30%), Avg System Time: 16.45s, Node Utilization: 45.7%

Resource Efficiency: ✅ CPU nodes balanced, 💡 GPU nodes underutilized

⚙️ Bottom Configuration Panel

Mean Arrival Interval

Range: 0.1 - 10.0s

Meaning: Average time interval between task arrivals

Effect:

Decrease → Tasks arrive more frequently, system load increases, queues longer

Increase → Tasks arrive less frequently, system load decreases, queues shorter

Simulation Time

Range: 100 - 10000s

Meaning: Total simulation duration

Effect:

Short → Quick results, but unstable statistics

Long → More accurate statistics, observe long-term system behavior

CPU Nodes

Range: 1 - 10

Meaning: Number of CPU servers

Effect:

Increase → Higher CPU capacity, shorter CPU queue, higher throughput, higher cost

Decrease → Lower CPU capacity, longer CPU queue, potential bottleneck

GPU Nodes

Range: 1 - 5

Similar to CPU nodes, consider GPU proportion and cost

CPU Task Probability

Range: 0.0 - 1.0

Meaning: Probability a new task is CPU task

Effect:

High → CPU queue pressure, possible idle GPU

Low → GPU queue pressure, possible idle CPU

Database Connection and Functions

Database Status

✅ Connected / ❌ Not Connected

Test Button: Test MariaDB connection

Successful → Green label, usable database functions

Failure → Red label, error popup, database functions unavailable

Database Functions:

💾 Save Config

📁 Load Config

📊 View History

🎮 Control Buttons

▶️ Start, ⏸ Pause, ▶️ Resume, ⏹ Stop, 🔄 Reset

🚀 Speed Control: 0.1x - 100x

📝 Basic Usage Flow

1. Configure parameters

2. Start simulation

3. Control simulation

4. Analyze results

5. Optimize configuration

6. Save results (optional)

💡 Tips

Quick test: Short Simulation Time, higher Speed

Detailed analysis: Longer Simulation Time, normal Speed

Identify bottlenecks, optimize one parameter at a time, save and compare configurations

⚠️ Common Issues

No data: Start simulation first

Database failure: Check database.properties and MariaDB service

Slow simulation: Increase Speed or reduce Simulation Time

Performance evaluation: Avg System Time small is good, Throughput large is good

Reasonable utilization: 30%-90%

🎯 Next Step

Check SCENARIO_GUIDE.md for application scenarios and case studies
