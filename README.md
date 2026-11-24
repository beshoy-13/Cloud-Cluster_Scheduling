# Cloud-Cluster CPU Scheduling

A Java simulation project for the CS241 Operating Systems scheduling assignment. The assignment specification is in [CS241-Assignment2-Scheduling.pdf](CS241-Assignment2-Scheduling.pdf).

## Project structure

```text
Cloud-Cluster_Scheduling/
├── CS241-Assignment2-Scheduling.pdf
├── pom.xml
├── lib/
│   ├── junit-4.13.2.jar
│   └── hamcrest-core-1.3.jar
└── src/
    ├── main/
    │   ├── java/com/scheduling/
    │   │   ├── gui/                 # Reserved for the optional GUI
    │   │   ├── metrics/Metrics.java # Calculates average scheduling metrics
    │   │   ├── model/Process.java   # Process data and per-process metrics
    │   │   ├── report/Report.java   # Prints the execution timeline and results
    │   │   ├── scheduler/
    │   │   │   ├── Scheduler.java   # Shared scheduler interface
    │   │   │   └── SJFScheduler.java# Preemptive SJF (SRTF) implementation
    │   │   └── simulation/Main.java # Runs the three sample SJF cases
    │   └── resources/testcases/     # Reserved for test-case input files
    └── test/
        ├── java/com/scheduling/SJFSchedulerTest.java
        └── resources/               # Reserved for test resources
```

`target/` and `out/` are generated compiler output; do not edit their `.class` files. They can be removed and regenerated.

## Software to install

- **JDK 25**: the Maven compiler release in `pom.xml` is set to 25. Install a JDK (not only a JRE) and configure VS Code to use it.
- **Apache Maven 3.9 or newer**: used to compile and run the test suite. Maven downloads the declared JUnit dependency when needed. The JUnit and Hamcrest jars under `lib/` are also included for manual local testing.
- **Visual Studio Code** (optional) with **Extension Pack for Java** and **Maven for Java** for editor run/test buttons and Maven project support.

Check the installation in a terminal:

```powershell
java -version
javac -version
mvn -version
```

All three commands should report the intended JDK/Maven installation. In VS Code, open the `Cloud-Cluster_Scheduling` folder itself. If prompted that the Java project configuration is out of date, run **Java: Update Project Configuration** from the Command Palette, then reload the Java project. Use **Java: Configure Java Runtime** to select JDK 25 for this project.

## Run the application

From a terminal opened in the `Cloud-Cluster_Scheduling` directory:

```powershell
mvn clean compile
java -cp target/classes com.scheduling.simulation.Main
```

Alternatively, open `src/main/java/com/scheduling/simulation/Main.java` in VS Code and select **Run** above the `main` method. The program currently runs three preemptive SJF examples and prints a tick-by-tick timeline and per-process/average metrics.

## Run the tests

From the project directory:

```powershell
mvn test
```

To run only the SJF test class:

```powershell
mvn -Dtest=SJFSchedulerTest test
```

In VS Code, open `src/test/java/com/scheduling/SJFSchedulerTest.java` and select **Run Test** above the class (or use **Testing: Run All Tests**). A test class is run by the JUnit test runner; it is not a second application `main` method.

If Maven is not installed yet, the bundled jars can be used to compile and test manually in PowerShell:

```powershell
$build = ".\target\manual-run"
New-Item -ItemType Directory -Force $build | Out-Null
$sources = Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp ".\lib\junit-4.13.2.jar;.\lib\hamcrest-core-1.3.jar" -d $build $sources
java -cp "$build;.\lib\junit-4.13.2.jar;.\lib\hamcrest-core-1.3.jar" org.junit.runner.JUnitCore com.scheduling.SJFSchedulerTest
java -cp $build com.scheduling.simulation.Main
```

## What is implemented

- **Preemptive SJF / SRTF**: at each tick, the ready process with the shortest remaining CPU burst runs. Ties prefer the earlier arrival.
- **Context-switch overhead on preemption**: the existing SJF convention charges the configured context-switch ticks when an unfinished running process is preempted. The scheduler chooses the shortest ready process again after the switch, including arrivals during that interval.
- **Execution timeline**: one entry per simulated tick, showing the running process and its remaining burst, or an idle/context-switch tick.
- **Metrics and report**: per-process finish, turnaround, waiting, and response times, plus average turnaround, waiting, and response times.
- **Example cases and tests**: `Main` runs three SJF examples; `SJFSchedulerTest` has six focused JUnit tests covering the metrics, idle CPU, context switching, and re-selection after a switch.

The SJF examples are not the assignment's ACS test-case output. The detailed queue/matrix examples in the PDF describe ACS.

## Work still required by the assignment

The current implementation is **SJF only**. The assignment also requires the Adaptive Container Scheduler (ACS); it is not implemented yet.

1. **Implement ACS Tier 1 (Round Robin).** Add an ACS scheduler implementing `Scheduler`. New processes start in the Tier 1 ready queue, are dispatched using a configurable time quantum, and unfinished processes retain their remaining burst when they return to the queue.
2. **Implement ACS Tier 2 (priority, non-preemptive).** When a Tier 1 process uses its full quantum without completing, demote it to Tier 2. Once a Tier 2 process starts running, let it finish before selecting another process.
3. **Implement Tier 3 aging.** Increase the effective priority of processes waiting in Tier 2 according to the policy agreed by the team. Define whether a larger or smaller number means higher priority and how often aging occurs; apply that definition consistently and document it.
4. **Print the required heuristic demotion matrix for every time step.** Include the current time, running process (or idle/context switch), and the contents/state of each ACS tier so demotions and aging can be followed.
5. **Use the assignment test cases.** The PDF gives these ACS inputs:

   | Case | Process inputs `(ID, arrival, burst, initial priority)` | Quantum | CS |
   | --- | --- | ---: | ---: |
   | 1 | `(P1, 0, 4, 2)`, `(P2, 1, 7, 3)`, `(P3, 3, 2, 1)` | 3 | 0 |
   | 2 | `(P1, 0, 5, 4)`, `(P2, 1, 6, 2)`, `(P3, 4, 4, 3)` | 3 | 1 |

   Test the ACS behavior against the expected states/results in the PDF. Keep the existing SJF tests as separate SJF tests.
6. **Compare settings and write the analysis.** Generate results for multiple context-switch and quantum values; compare average waiting and turnaround time, graph their relationship to CS and Q, and discuss Tier 2 demotion's CPU-utilization/response-time trade-off.
7. **Optional bonus:** build a GUI after the required schedulers and reports work.

Before implementing ACS, agree as a team on queue order, priority direction, aging interval/increment, and whether context-switch overhead applies to each scheduler transition. These choices affect the expected timeline and metrics.

## Suggested team workflow

The assignment requires a team of four or five students from the same lab group/TA. Suggested division of work:

- One teammate owns ACS Tier 1 and its tests.
- One owns Tier 2 demotion and non-preemptive dispatch.
- One owns aging and matrix/timeline reporting.
- One owns test-case validation, CS/Q comparisons, graphs, and report analysis.
- If there is a fifth teammate, they can integrate the schedulers, review edge cases, and/or build the optional GUI.

Agree on interfaces and policy decisions before splitting work. Work on separate Git branches, commit small changes, and integrate/test together. Keep shared changes to `Process`, `Scheduler`, and `Report` coordinated to avoid merge conflicts.

## Submission reminders

The PDF says one team member submits the final source-code package as **one ZIP**. Its requested naming pattern is:

```text
A2_TAname_Group_ID1_ID2_ID3_ID4
```

Check the PDF for the full team/submission rules and deadline. Include source, `pom.xml`, tests, and the required report; do not rely on compiled `target/` or `out/` files as a substitute for source.
