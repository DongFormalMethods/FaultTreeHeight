package main

import decisiontree.BinaryDecisionTree

import scala.io.Source
import dft.DFT
import minimalcutpathset.FaultTree
import viz.DDTViz

import scala.annotation.tailrec

object Main {

    // TODO: convert fault-tree to decision tree.
    // TODO: algorithms: BUDA, CUDA, PADA.

    // BUDA = algorithm8 (previously called 'remind')
    // TODO: others?


    def main(args: Array[String]): Unit = {
        // TODO: fix slf4j warning.

        // 0. Gather commandline options.
        val options: CommandLineOptions = CommandLineOptions.parse(args) match {
            case Some(options) => options
            case None =>
                // scopt has already printed an error message with usage help.
                // So we can just exit here.
                System.exit(1)
                throw new RuntimeException("Cannot occur")
        }

        // 1. Read input file with DFTReader
        // 2. Convert DFT to DAG-like fault-tree.
        val faultTree = DFT.readDagLikeFaultTree(Source.fromFile(options.inputFile.toUri))

        // 3. Perform algorithm X to convert dag-like fault-tree to diagnostic decision tree.
        val diagnosticDecisionTree = convert(options.algorithim, faultTree)

        // 4. write output of diagnostic decision tree via GraphViz.
        DDTViz.writeDotFile(diagnosticDecisionTree, options.outputFile)

        // 5. Done!
        println(s"Wrote GraphViz dot file: ${options.outputFile}.")
    }

    private def convert(algorithm: Algorithm, tree: FaultTree): BinaryDecisionTree = {
        algorithm match {
            case Algorithm.Buda =>
                decisiontree.algorithm8(tree)._1
            case Algorithm.Cuda /*prob variant*/ =>
                val (etas, height) = minimalcutpathset.algorithm4(tree)
                etasToBinaryDecisionTree(etas)
            case Algorithm.Pada /*prob variant*/ =>
                val (etas, height) = minimalcutpathset.algorithm5(tree)
                etasToBinaryDecisionTree(etas)
        }
    }

    private def etasToBinaryDecisionTree(etas: minimalcutpathset.Etas): BinaryDecisionTree = {
        import minimalcutpathset.{Eta, Etas, Path, Decision, Event}

        val etasByLength = etas.toSeq.sortBy((p, eta) => p.size)
        etasByLength.foreach(println)
        println()

        def etaToDiagnosticDecisionTree(eta: Eta): BinaryDecisionTree = eta match {
            case Decision.One => BinaryDecisionTree.One
            case Decision.Zero => BinaryDecisionTree.Zero
            case event: Event => BinaryDecisionTree.NonLeaf(event, BinaryDecisionTree.Zero, BinaryDecisionTree.One)
        }

        def insertLeaf(binaryDecisionTree: BinaryDecisionTree, path: Path, eta: Eta): BinaryDecisionTree = path match {
            case Nil => etaToDiagnosticDecisionTree(eta)
            case Decision.Zero +: tail => binaryDecisionTree match {
                case BinaryDecisionTree.Zero | BinaryDecisionTree.One => binaryDecisionTree // cannot insert after Zero or One.
                case BinaryDecisionTree.NonLeaf(id, left, right) => BinaryDecisionTree.NonLeaf(id, insertLeaf(left, tail, eta), right)
            }
            case Decision.One +: tail => binaryDecisionTree match {
                case BinaryDecisionTree.Zero | BinaryDecisionTree.One => binaryDecisionTree // cannot insert after Zero or One.
                case BinaryDecisionTree.NonLeaf(id, left, right) => BinaryDecisionTree.NonLeaf(id, left, insertLeaf(right, tail, eta))
            }
        }

        var diagnosticDecisionTree: BinaryDecisionTree = null;

        for ((path, eta) <- etasByLength) {
            diagnosticDecisionTree = insertLeaf(diagnosticDecisionTree, path, eta)
        }

        decisiontree.algorithm6(diagnosticDecisionTree)
    }
}
