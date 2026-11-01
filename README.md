# Precision Agriculture: Observer + Visitor (Assignment 6, variant 9)

Java 17, JUnit 5, no other libraries.
Run: `mvn test` or run `farm.Main` (optional args: `producerThreads eventsPerProducer`, default `4 1000`).

## 1. Architecture

```
Observer side                                      Visitor side
SoilSensorSource ──► IrrigationController          Farm ─► Field ─► Plot ─► Crop / Pump / Sensor / Valve
                 ├─► FieldAggregator ─► FarmerApp
                 └─► StatisticsListener            WaterUsageVisitor    (numeric)
WeatherStationSource ─► FrostAlertListener ─┐      CostVisitor          (accumulates state)
                                            ▼      NeedsAttentionVisitor (filter -> List)
                                  InspectionService ◄── integration ──► JsonExportVisitor (nested JSON)
                                   (Observer + Subject)
```

Integration: `FrostWarning(field)` → `InspectionService` finds the field subtree in `FarmIndex`
→ `NeedsAttentionVisitor` runs on **that subtree only** → `InspectionReport` (+ `MaintenanceTicket`) are published as new events.

TODO: UML class diagram (docs/uml.png) — one diagram for the whole project.

## 2. Design decisions (TODO: rewrite in your own words, you must defend them)

- **Push vs pull:** push. Events are immutable records that carry their data, so listeners never call back into the
  subject and cannot see half-updated state.
- **Who traverses the tree:** the visitor (`FoldingVisitor`). Elements hold data only; every container is folded in one place.
- **Thread safety without locking the subject:** `ConcurrentHashMap<EventType, CopyOnWriteArrayList<Observer>>`;
  `publish` iterates a snapshot, so (un)subscribing during publishing, even from inside a handler, is safe.
  The only lock is a tiny `synchronized` window per field inside `FieldAggregator`, and listeners are never called while it is held.
- **Graceful shutdown:** `ProducerPool.shutdownAndAwait` = `shutdown()` + `awaitTermination()` + `future.get()` for every task.
  Delivery is synchronous in the producer thread, so after shutdown no event is lost and no thread hangs.
- **Clean Code:** one composition root (`FarmSystem`), constructor injection, no lazy initialization, no singletons.

## 3. Trade-off proof (TODO: fill in after you make the two commits)

| Commit | Change | Files changed | Cost |
|---|---|---|---|
| A | new visitor `HarvestEstimateVisitor` | TODO (only the new visitor + its test) | TODO |
| B | new element type (e.g. `WaterTank`) | TODO (new element, `Visitor`, every visitor, tests) | TODO |

## 4. Tests

TODO: list of test classes and what they prove (stress test: 4 threads x 1000 events = 4000 exact; visitor values are hand-calculated in comments).
