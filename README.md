# Optimal Decision Tree Algorithms

This repository hosts algorithms designed to find optimal diagnostic decision trees for helping engineers to do fault diagnosis quickly.<br>
These algorithms were designed by Yanni Dong, Milan Lopuhaä-Zwakenberg and Mariëlle Stoelinga in 2025.

#### Exact optimal Diagnostic decision tree Algorithm (EDA)
By using this algorithm you can find the optimal decision tree with the minimum height. Due to its exponential time complexity it is only suitable for small fault trees. Use of this algorithm is not recommended for fault trees with #basic-events ≥ 10.

The following three algorithms are approximation algorithms.

#### Bottom-Up Decision tree heuristic Algorithm (BUDA)
This algorithm works from the leaves of the fault tree upwards to the root. It is the quickest among the 3 approximation algorithms, especially for tree-like fault trees, i.e., the underlying undirected graph is a tree.
If you have a tree-like fault tree, we suggest you to use this algorithm.

#### Cut set Diagnostic decision tree heuristic Algorithm (CuDA)
This algorithm is based on the set of minimal cut sets of the fault tree. If you already know the minimal cut sets, then we recommend to use this.
If the number of minimal cut sets is too big, you can try the BUDA or PaDA algorithms.

#### Path set Diagnostic decision tree heuristic Algorithm (PaDA)
This algorithm is based on the set of minimal path sets of the fault tree. If you already know the minimal path sets, then we recommend to use this.
If the number of minimal path sets is too big, you can try the BUDA or CuDA algorithms.

## IDE setup

Import this project into [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [VSCode](https://code.visualstudio.com/).
For IntelliJ, ensure the [Scala plugin](https://plugins.jetbrains.com/plugin/1347-scala) is installed.
For VSCode, ensure the [Scala/Metals](https://scalameta.org/metals/docs/editors/vscode/) extension is installed.

Your IDE should now recognise this project as an [SBT](https://www.scala-sbt.org/) project.
Depending on your IDE/Editor setup, you should also install SBT itself on your operating system.

## Convert Fault Trees to Diagnostic Decision Trees

1. Obtain the `FaultTreeHeight.jar` artefact; either by downloading it from [GitHub Releases](https://github.com/DongFormalMethods/FaultTreeHeight/releases),
   or by compiling it locally using `sbt assembly`. If compiling locally, the output file will be created at `./target/scala-3.3.8/FaultTreeHeight.jar`.
2. Execute it at your leisure; `java -jar FaultTreeHeight.jar --input MyFaultTree.dft --output MyFaultTree.dot --algorithm Buda`.
   This will create a GraphViz .dot file, which can be rendered into an image, or analysed by other tooling.
   The input file must be a .dft file in Galileo format.
   See `java -jar FaultTreeHeight --help` for commandline options help.

## Project setup

- DecisionTree.scala: contains the exact height calculation for boolean formulae (Eminent/EDA).
- RecursiveAlgorithm2.scala: contains the 'recursive' height approximation algorithm for tree-like fault trees (Remind/BUDA).
- BinaryDecisionTree.scala: contains the 'recursive' height approximation algorithm for dag-like fault trees (Remind/BUDA).
- CutSetAlgorithm4.scala: contains the 'MCS-based' height approximation algorithm for dag-like fault trees (Mince/CuDA).
  - MinceOrderedSet.scala: contains the 'size' variation of the CuDA algorithm.
- PathSetAlgorithm5.scala: contains the 'MPS-based' height approximation algorithm for dag-like fault trees (Pase/PaDA).
  - PaseOrderedSet.scala: contains the 'size' variation of the PaDA algorithm.
- RandomBDTs.scala: contains the 'random binary decision tree' height approximation algorithm for dag-like fault trees (Ranger).
- Benchmark.scala: contains a [JMH](https://openjdk.org/projects/code-tools/jmh/) benchmark for comparing the running times of all algorithms mentioned in the paper.
- Conversion.scala: code which converts between Tree-Like FaultTree, Dag-Like FaultTree and BooleanFormula representations of fault trees.
- TreesInPaper.scala: calculates height approximations for some real-world fault trees.
- DFT.scala: simple parser and printer for galileo-formatted .dft files.
- Main.scala: a main class that is used to generate Diagnostic Decision Tree files given .dft file inputs.

## Running the benchmarks

Set your JAVA_HOME environment variable to a JDK 23 installation.
<br>
Windows example: `set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-23.0.1+11-hotspot`
<br>
Linux example: `export JAVA_HOME=/opt/jdk23`

Execute `sbt jmh:run` from a terminal.
<br>
Note that this operation can take up to 10 hours.
To reduce the benchmark execution time, uncomment the @Fork annotation on the RealWorldFaultTreesBenchmark class in Benchmark.scala.
