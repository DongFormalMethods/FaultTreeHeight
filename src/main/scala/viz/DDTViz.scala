package viz

import decisiontree.BinaryDecisionTree
import guru.nidi.graphviz.attribute.{Label, Rank, Shape, Style}
import guru.nidi.graphviz.engine.{Format, Graphviz}
import guru.nidi.graphviz.model.{Factory, MutableGraph, MutableNode}

import java.nio.file.Path

/** Logic for converting a Diagnostic Decision Tree to GraphViz. */
object DDTViz {

    def toGraphViz(diagnosticDecisionTree: BinaryDecisionTree): MutableGraph = {
        // TODO: can we make the format similar to Storm's? Perhaps our own output is better.

        val graph = Factory.mutGraph()
        graph.setDirected(true)
        graph.graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM))

        def recurse(previousIds: Seq[Event | Boolean], tree: BinaryDecisionTree): MutableNode = {
            val v = tree match {
                case BinaryDecisionTree.Zero =>
                    val nodeId = previousIds.mkString("", "__", "__0")
                    val vertex = Factory.mutNode(nodeId)
                    vertex.add(Label.of("0"))
                    vertex.add(Shape.BOX)
                    vertex
                case BinaryDecisionTree.One =>
                    val nodeId = previousIds.mkString("", "__", "__1")
                    val vertex = Factory.mutNode(nodeId)
                    vertex.add(Label.of("1"))
                    vertex.add(Shape.BOX)
                    vertex
                case BinaryDecisionTree.NonLeaf(id: Event, left: BinaryDecisionTree, right: BinaryDecisionTree) =>
                    val nodeId = (previousIds :+ id).mkString("__")
                    val vertex = Factory.mutNode(nodeId)
                    vertex.add(Label.of(id))
                    vertex.add(Shape.ELLIPSE)
                    graph.add(vertex)
                    val leftNode = recurse(previousIds :+ id :+ false, left)
                    val rightNode = recurse(previousIds :+ id :+ true, right)

                    val leftLink = vertex.linkTo(leftNode).`with`(Style.DASHED)     // basic event with 'id' is operating
                    vertex.addLink(leftLink)
                    val rightLink = vertex.linkTo(rightNode).`with`(Style.SOLID)    // basic event with 'id' failed
                    vertex.addLink(rightLink)

                    vertex
            }
            graph.add(v)
            v
        }

        recurse(IndexedSeq(), diagnosticDecisionTree)

        graph
    }

    def writeDotFile(graph: MutableGraph, outputFile: Path): Unit = {
        val output = Graphviz.fromGraph(graph)
            .render(Format.DOT)
            .toFile(outputFile.toFile)
    }

    def writeDotFile(diagnosticDecisionTree: BinaryDecisionTree, outputFile: Path): Unit =
        writeDotFile(toGraphViz(diagnosticDecisionTree), outputFile)
}
