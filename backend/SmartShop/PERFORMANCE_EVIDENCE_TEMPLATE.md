# SmartShop Lab 8 Performance Evidence Template

Fill this template after completing baseline and post-optimization runs.

---

## A) Test Environment

- Date:
- Machine/OS:
- Java version:
- Maven version:
- Spring profile:
- Database:
- Notes on environment stability:

---

## B) Test Conditions (must match both phases)

- Collection file:
- Postman environment:
- Warm-up method:
- Iterations per scenario:
- Dataset notes:

---

## C) Scenario Results (Baseline vs Post)

| Scenario | Baseline Avg (ms) | Post Avg (ms) | Improvement % | Baseline Failures | Post Failures | Baseline Duration | Post Duration |
|---|---:|---:|---:|---:|---:|---:|---:|
| S1 Product list/sort |  |  |  |  |  |  |  |
| S2 Product search/filter |  |  |  |  |  |  |  |
| S3 Create order |  |  |  |  |  |  |  |
| S4 Inventory decrement |  |  |  |  |  |  |  |

---

## D) Profiler Comparison

### D1 CPU Hotspots

- Baseline top method(s):
- Post top method(s):
- Observed change:

### D2 Flame Graph / Call Tree

- Baseline dominant path:
- Post dominant path:
- Observed change:

### D3 Memory / Allocations

- Baseline high-allocation class/method:
- Post high-allocation class/method:
- Observed change:

---

## E) Optimization Changes and Impact Mapping

| Change ID | Commit | What changed | Expected effect | Observed metric effect |
|---|---|---|---|---|
| C1 |  |  |  |  |
| C2 |  |  |  |  |
| C3 |  |  |  |  |

---

## F) Screenshot Checklist

### Postman

- [ ] baseline_postman_s1.png
- [ ] baseline_postman_s2.png
- [ ] baseline_postman_s3.png
- [ ] baseline_postman_s4.png
- [ ] post_postman_s1.png
- [ ] post_postman_s2.png
- [ ] post_postman_s3.png
- [ ] post_postman_s4.png

### IntelliJ Profiler

- [ ] baseline_cpu_hotspots.png
- [ ] baseline_flame_graph.png
- [ ] baseline_allocations.png
- [ ] post_cpu_hotspots.png
- [ ] post_flame_graph.png
- [ ] post_allocations.png

---

## G) Final Summary

- Most improved scenario:
- Biggest bottleneck removed:
- Remaining bottleneck:
- Next optimization candidate:
