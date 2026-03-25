# SmartShop Lab 8 Performance Runbook (Pre vs Post Optimization)

This runbook gives a repeatable process to capture **before** and **after** optimization evidence using:

- IntelliJ IDEA Profiler
- Postman Collection Runner

Use this together with `PERFORMANCE_EVIDENCE_TEMPLATE.md`.

---

## 1) Goal

Produce defensible evidence that performance improved after optimization by keeping test conditions the same in both runs.

You must capture:

1. Postman run summary screenshots (latency, pass/fail counts)
2. IntelliJ profiler screenshots (CPU hotspots + flame graph + allocations)
3. Before/after comparison table

---

## 2) Fixed Test Conditions (Do Not Change Between Runs)

- Same machine and similar background load
- Same Java version and Maven profile
- Same database content
- Same Postman collection and environment variables
- Same endpoint set and iteration counts
- Same warm-up pattern (warm-up run not recorded)

If any condition changes, note it clearly in the evidence template.

---

## 3) Recommended Test Matrix for SmartShop

Use these 4 scenarios because they cover read-heavy and write-heavy flows.

| Scenario ID | Endpoint | Method | Why it matters | Suggested Iterations |
|---|---|---|---|---|
| S1 | `/api/products?page=0&size=20&sortBy=PRICE&ascending=true` | GET | Hot path for catalog listing and sorting | 200 |
| S2 | `/api/products?search=phone&minPrice=50&maxPrice=2000&inStock=true&page=0&size=20` | GET | Search/filter path with query logic | 200 |
| S3 | `/api/orders` | POST | Order creation with inventory + transaction overhead | 100 |
| S4 | `/api/inventory/product/{productId}/decrement` | POST | Stock write path under load | 150 |

Use valid payloads from `SmartShop-API.postman_collection.json` for S3/S4.

---

## 4) Pre-Optimization Capture (Baseline)

### 4.1 Start backend

From `backend/SmartShop`:

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

Wait until app is ready on `http://localhost:8080`.

### 4.2 Prepare Postman

1. Import `SmartShop-API.postman_collection.json`.
2. Create environment with:
   - `baseUrl = http://localhost:8080`
   - `accessToken` (auto-populated by login requests)
3. Run `Auth > Login (ADMIN)` once and confirm token is stored.
4. Make sure all scenario requests use the same environment.

### 4.3 Warm-up (not recorded)

Run each scenario once with small iterations (5–10). Do not screenshot this run.

### 4.4 Record baseline Postman results

Run each scenario with the iteration count in section 3 and capture screenshots of Collection Runner summary:

- Total requests
- Failed requests
- Average response time
- Total duration

Save as:

- `baseline_postman_s1.png`
- `baseline_postman_s2.png`
- `baseline_postman_s3.png`
- `baseline_postman_s4.png`

### 4.5 Record baseline IntelliJ profiler

1. In IntelliJ, start app with **Profile** (CPU + Allocations).
2. While profiler is active, execute S1–S4 in Postman.
3. Stop profiler and capture:
   - Top methods/hotspots (CPU)
   - Flame graph/call tree
   - Allocation hotspots

Save as:

- `baseline_cpu_hotspots.png`
- `baseline_flame_graph.png`
- `baseline_allocations.png`

---

## 5) Optimization Loop

Pick one bottleneck at a time (highest impact first). Typical Lab 8 optimizations:

- Async long-running service work (`CompletableFuture`, `@Async`)
- Dedicated bounded thread pools (`ThreadPoolTaskExecutor`)
- Thread-safe shared structures (`ConcurrentHashMap`, atomic counters)
- Caching repeated reads
- Query/index improvements or reduced N+1 access patterns

After each logical change:

1. Verify build passes (`./mvnw clean compile`)
2. Sanity test affected endpoint
3. Commit only related files using conventional commit format

Examples:

- `feat(cache): cache filtered product queries`
- `refactor(order): run notification asynchronously`
- `fix(inventory): guard concurrent stock decrement`

---

## 6) Post-Optimization Capture

Repeat **exactly** section 4.3 to 4.5 with unchanged test conditions.

Save screenshots as:

- `post_postman_s1.png` ... `post_postman_s4.png`
- `post_cpu_hotspots.png`
- `post_flame_graph.png`
- `post_allocations.png`

---

## 7) What To Show In Final Submission

1. Before/after Postman summaries per scenario
2. Before/after profiler screenshots
3. Comparison table with improvement percentages
4. Brief explanation linking each code change to measured impact

Improvement formula:

- Latency improvement % = `((baselineAvgMs - postAvgMs) / baselineAvgMs) * 100`
- Throughput improvement % = `((postRps - baselineRps) / baselineRps) * 100`

---

## 8) Optional: Newman CLI (if you want scriptable runs)

If Newman is installed:

```bash
newman run SmartShop-API.postman_collection.json \
  --env-var baseUrl=http://localhost:8080 \
  --iteration-count 100
```

Use this only if you keep the same request set and iteration plan for both phases.
