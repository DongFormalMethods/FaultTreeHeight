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
        val etasByLength = etas.toSeq.sortBy((p, eta) => p.size)
        println()

        import minimalcutpathset.{Eta, Etas, Path, Decision, Event}
        class MutNode {
            var value: Eta | Null = null
            var falseBranch: 0 | MutNode = 0
            var trueBranch: 1 | MutNode = 1

            override def toString: String = s"MutNode(value=${value}, falseBranch=${falseBranch}, trueBranch=${trueBranch})"

            def prettyPrint(): Unit = {
                prettyPrint(0)
            }

            private def prettyPrint(indent: Int): Unit = {
                println(" ".repeat(indent).concat(String.valueOf(value)))
                falseBranch match {
                    case 0: 0 => println(" ".repeat(indent).concat("0"))
                    case node: MutNode => node.prettyPrint(indent + 2)
                }
                trueBranch match {
                    case 1: 1 => println(" ".repeat(indent).concat("1"))
                    case node: MutNode => node.prettyPrint(indent + 2)
                }
            }
        }

        val root: MutNode = new MutNode()

        @tailrec
        def insert(node: MutNode, path: Path, eta: Eta): Unit = {
            if (path.isEmpty) {
                assert(node.value == null, s"overwriting existing node value, ${node.value} with ${eta}.")
                node.value = eta
            } else {
                val head +: tail = path: @unchecked // safe because of !path.isEmpty
                head match {
                    case Decision.Zero =>
                        node.value match {
                            case null | Decision.One | Decision.Zero => // do nothing
                            case event: Event =>
                                val falseBranch = new MutNode()
                                node.falseBranch = falseBranch
                                insert(falseBranch, tail, eta)
                        }
                    case Decision.One =>
                        node.value match {
                            case null | Decision.One | Decision.Zero => // do nothing
                            case event: Event =>
                                val trueBranch = new MutNode()
                                node.trueBranch = trueBranch
                                insert(trueBranch, tail, eta)
                        }
                }
            }
        }

//        var rounds = 0          // TODO remove debug
//        val maxRounds = 10      // TODO remove debug

        for ((path, eta) <- etasByLength) {
//            rounds += 1         // TODO remove debug
            insert(root, path, eta)

//            println((path, eta))
//            root.prettyPrint()
//            println()
//            println()

//            if (rounds > maxRounds) throw new RuntimeException("Boom!") // TODO remove debug
        }

        println(root)   // TODO why do we have intermediate nodes with value == null ?
//        root.prettyPrint()

        def toDiagnosticDecisionTree(node: MutNode): BinaryDecisionTree = {
            node.value match
                case Decision.Zero => BinaryDecisionTree.Zero
                case Decision.One => BinaryDecisionTree.One
                case event: Event =>
                    val left = node.falseBranch match
                        case 0: 0 => BinaryDecisionTree.Zero
                        case leftChild: MutNode => toDiagnosticDecisionTree(leftChild)
                    val right = node.trueBranch match
                        case 1: 1 => BinaryDecisionTree.One
                        case rightChild: MutNode => toDiagnosticDecisionTree(rightChild)
                    BinaryDecisionTree.NonLeaf(event, left, right)
        }

        toDiagnosticDecisionTree(root)
    }
}
