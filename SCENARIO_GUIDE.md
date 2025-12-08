# Scenario Guide

📋 Document Description

This guide uses practical scenarios and cases to help you understand how to use the Cloud Computing Service Queue Simulation System to solve real-world problems.

Related Documents:

* `USER_GUIDE.md` – Detailed interface and parameter explanation
* `STATISTICS_IMPLEMENTATION.md` – Technical implementation details

---

🎯 Application Scenarios Overview

| Scenario                                 | Objective                            | Key Metrics                     |
| ---------------------------------------- | ------------------------------------ | ------------------------------- |
| Scenario 1: System Capacity Evaluation   | Find maximum load capacity           | Average System Time, Throughput |
| Scenario 2: Resource Optimization        | Determine optimal CPU/GPU ratio      | Utilization, Cost-effectiveness |
| Scenario 3: VIP Priority Verification    | Demonstrate VIP service value        | User Type Statistics            |
| Scenario 4: Cost Optimization            | Reduce hardware cost                 | Utilization, Throughput         |
| Scenario 5: Peak Hour Planning           | Handle traffic surge                 | Queue Length, Response Time     |
| Scenario 6: Business Scenario Adaptation | Match specific business requirements | Task Type Distribution          |

---

🔬 Scenario 1: System Capacity Evaluation

**Business Background**

You are a cloud service provider and need to determine the maximum concurrent users your current system configuration can handle.

**Objective**

Find the critical point where the system shows significant performance degradation (saturation point).

**Experiment Design**

* Fixed configuration:

    * CPU Nodes: 2
    * GPU Nodes: 1
    * CPU Task Probability: 0.7
    * Simulation Time: 2000s
* Variable: Mean Arrival Interval (gradually reduced to increase load)

**Procedure**

1. Set initial parameters:

    * Mean Arrival Interval: 5.0s
    * Click Start
    * Wait for completion
    * Record results

2. Gradually increase load by testing arrival intervals:
   5.0s → 3.0s → 2.0s → 1.5s → 1.0s → 0.5s

3. Record for each interval:

    * Avg System Time
    * Throughput
    * CPU/GPU Utilization
    * Max Queue Length

**Results Analysis**

| Arrival Interval | Task Arrival Rate | Avg System Time | Throughput | CPU Utilization | Conclusion                             |
| ---------------- | ----------------- | --------------- | ---------- | --------------- | -------------------------------------- |
| 5.0s             | 0.20/s            | 8.5s            | 0.19/s     | 45%             | ✅ Light load – resources underutilized |
| 3.0s             | 0.33/s            | 10.2s           | 0.32/s     | 68%             | ✅ Normal – stable operation            |
| 2.0s             | 0.50/s            | 12.3s           | 0.48/s     | 82%             | ✅ High load – acceptable               |
| 1.5s             | 0.67/s            | 18.7s           | 0.55/s     | 91%             | ⚠️ Near saturation                     |
| 1.0s             | 1.00/s            | 35.4s           | 0.61/s     | 96%             | ⚠️ Overload – performance degradation  |
| 0.5s             | 2.00/s            | 125.8s          | 0.58/s     | 98%             | ❌ Severe overload                      |

**Observations**

* CPU Compute Queue:

    * Arrival Interval 5.0s: Max Queue = 2
    * Arrival Interval 2.0s: Max Queue = 8
    * Arrival Interval 1.0s: Max Queue = 25 ⚠️
    * Arrival Interval 0.5s: Max Queue = 78 ❌
* Performance breakpoint identified between 1.5s–1.0s
* Avg System Time jumps from 18.7s → 35.4s
* CPU utilization exceeds 90%

**Conclusion & Recommendations**

* Safe Load: Arrival Interval > 2.0s (Throughput 0.48/s)
* Maximum Load: Arrival Interval ≈ 1.5s (Throughput 0.55/s)
* Danger Zone: Arrival Interval < 1.0s (System close to collapse)

Business Recommendation:

* Current configuration supports ~0.48 tasks/s stable load
* Reserve 20% buffer → plan for 0.40 tasks/s
* To support higher load, increase CPU nodes

Scaling Plan:

* +1 CPU node → support 0.65–0.70 tasks/s
* +2 CPU nodes → support 0.80–0.90 tasks/s

---

💰 Scenario 2: Resource Optimization (Cost-Effectiveness Analysis)

**Business Background**

Target throughput: 0.45 tasks/s. Find the most economical hardware configuration.

**Objective**

Minimize hardware cost while meeting performance requirements.

**Experiment Design**

* Fixed configuration:

    * Mean Arrival Interval: 2.0s (target throughput ~0.45/s)
    * CPU Task Probability: 0.7
    * Simulation Time: 2000s
* Variable: CPU and GPU node combinations

**Cost Assumptions**

* CPU node: $100/month
* GPU node: $300/month
* Performance requirements: Avg System Time < 20s, Throughput > 0.45/s

**Test Configurations**

| Config # | CPU Nodes | GPU Nodes | Monthly Cost | Avg Time | Throughput | CPU Utilization | GPU Utilization | Result               |
| -------- | --------- | --------- | ------------ | -------- | ---------- | --------------- | --------------- | -------------------- |
| 1        | 1         | 1         | $400         | 35.2s    | 0.25/s     | 98%             | 42%             | ❌ Not meeting        |
| 2        | 2         | 1         | $500         | 18.5s    | 0.45/s     | 85%             | 46%             | ✅ Meets requirements |
| 3        | 3         | 1         | $600         | 15.3s    | 0.48/s     | 62%             | 48%             | ✅ Over-provisioned   |
| 4        | 2         | 2         | $800         | 16.2s    | 0.47/s     | 87%             | 23%             | ⚠️ GPU underutilized |
| 5        | 1         | 2         | $700         | 32.1s    | 0.28/s     | 96%             | 25%             | ❌ Not meeting        |

**Analysis**

* Optimal configuration: CPU=2, GPU=1 ($500/month)
* Balanced utilization: CPU 85%, GPU 46%
* Under-provisioning fails, over-provisioning wastes cost

---

⭐ Scenario 3: VIP Priority Strategy Verification

**Business Background**

Three service levels: Normal, Personal VIP, Enterprise VIP. Show VIP service value.

**Objective**

Quantify VIP performance advantage for differential pricing.

**Configuration**

* CPU Nodes: 2
* GPU Nodes: 1
* Mean Arrival Interval: 2.0s
* CPU Task Probability: 0.7
* Simulation Time: 3000s

**Results**

| User Type      | Completed | Avg System Time |
| -------------- | --------- | --------------- |
| NORMAL         | 180 (60%) | 21.45s          |
| PERSONAL VIP   | 75 (25%)  | 14.23s          |
| ENTERPRISE VIP | 45 (15%)  | 9.76s           |

**Priority Effect**

* Enterprise VIP 54.5% faster than Normal
* Personal VIP 33.7% faster than Normal

**Conclusion**

* VIP tasks prioritized in CPU queues
* VIP advantage more pronounced under high load
* Pricing strategy based on performance improvement recommended

---

🚀 Scenario 4: Peak Hour Capacity Planning

**Business Background**

Traffic surge from 8–10 PM; need temporary scaling assessment.

**Recommendation**

* Automatic scaling based on CPU utilization or queue length
* High ROI for temporary scaling vs. degraded performance cost

---

🎯 Scenario 5: AI Training vs Normal Computing Mixed Load

**Objective**

Optimize CPU/GPU allocation based on task distribution.

| CPU Task Ratio | Recommended Config | Monthly Cost | Scenario      |
| -------------- | ------------------ | ------------ | ------------- |
| 90%            | CPU=3, GPU=1       | $600         | Web apps      |
| 60–85%         | CPU=2, GPU=1       | $500         | Mixed apps    |
| 30–60%         | CPU=2, GPU=2       | $800         | AI+Web        |
| <40%           | CPU=2, GPU=2       | $800         | Deep learning |

**Dynamic Adjustment**

* Monitor CPU/GPU ratio and utilization hourly
* Add/remove nodes based on thresholds

---

📊 Scenario 6: SLA Compliance Verification

**Objective**

Validate SLA compliance under different load levels.

| Load   | Arrival Interval | Config       | Normal Users | VIP Users | SLA Met |
| ------ | ---------------- | ------------ | ------------ | --------- | ------- |
| Low    | 4.0s             | 2 CPU, 1 GPU | 10.2s        | 6.8s      | ✅✅      |
| Medium | 2.5s             | 2 CPU, 1 GPU | 14.5s        | 9.3s      | ✅✅      |
| High   | 2.0s             | 2 CPU, 1 GPU | 18.7s        | 12.1s     | ✅✅      |
| Peak   | 1.5s             | 2 CPU, 1 GPU | 24.3s        | 16.2s     | ❌⚠️     |

**Recommendation**

Use temporary scaling to meet SLA at peak load cost-effectively.

---

💡 Comprehensive Project Example

* Client: "CloudFast" startup
* Users: 10,000
* Peak concurrency: 500
* Task distribution: 75% CPU, 25% GPU
* Service tiers: Standard + VIP
* Budget: $2,000/month

**Final Configuration**

* Daily: CPU=1, GPU=1 ($400/month)
* Peak: CPU=2, GPU=1 (automatic scaling)
* Response time: Daily 12s, Peak 18s
* Availability: 99.5%
* Total cost: $408/month

---

🎓 Learning Path

* Beginner: Scenario 1 (Capacity evaluation)
* Intermediate: Scenario 2 (Resource optimization), Scenario 3 (VIP strategy)
* Advanced: Use all scenarios to design flexible scaling and full capacity planning

---

📝 Experiment Record Template

```
## Experiment Record

### Objective
[Describe your goal]

### Configuration
- CPU Nodes:
- GPU Nodes:
- Mean Arrival Interval:
- CPU Task Probability:
- Simulation Time:

### Results
- Tasks Arrived:
- Tasks Completed:
- Avg System Time:
- Throughput:
- CPU Utilization:
- GPU Utilization:

### Queue Statistics
[Paste queue statistics]

### User Type Statistics
[Paste user type statistics]

### Task Type Statistics
[Paste task type statistics]

### Analysis & Conclusion
[Your analysis]

### Improvement Suggestions
[Next optimization steps]
```

---

🎯 Next Steps

Refer to `USER_GUIDE.md` for detailed interface and parameter instructions.

Happy simulating! 🚀
