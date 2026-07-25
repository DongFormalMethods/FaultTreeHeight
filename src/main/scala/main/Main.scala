package main

import decisiontree.BinaryDecisionTree

import scala.io.Source
import dft.DFT
import minimalcutpathset.FaultTree
import viz.DDTViz

object Main {

    // TODO: convert fault-tree to decision tree.
    // TODO: algorithms: BUDA, CUDA, PADA.

    // BUDA = algorithm8 (previously called 'remind')
    // TODO: others?


    def main(args: Array[String]): Unit = {
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
                // TODO
                ???
            case Algorithm.Pada /*prob variant*/ =>
                // TODO
                ???
        }
    }
}
