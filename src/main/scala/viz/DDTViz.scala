package viz

import decisiontree.BinaryDecisionTree
import guru.nidi.graphviz.attribute.{Rank, Shape, Style}
import guru.nidi.graphviz.engine.{Format, Graphviz}
import guru.nidi.graphviz.model.{Factory, MutableGraph, MutableNode}

import java.nio.file.Path

/** Logic for converting a Diagnostic Decision Tree to GraphViz. */
object DDTViz {

    def toGraphViz(diagnosticDecisionTree: BinaryDecisionTree): MutableGraph = {
        // TODO: can we make the format similar to Storms?

        val graph = Factory.mutGraph()
        graph.setDirected(true)
        graph.graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM))

        def recurse(tree: BinaryDecisionTree): MutableNode = {
            val v = tree match {
                case BinaryDecisionTree.Zero =>
                    val vertex = Factory.mutNode("0")
                    vertex.add(Shape.BOX)
                    vertex
                case BinaryDecisionTree.One =>
                    val vertex = Factory.mutNode("1")
                    vertex.add(Shape.BOX)
                    vertex
                case BinaryDecisionTree.NonLeaf(id: Event, left: BinaryDecisionTree, right: BinaryDecisionTree) =>
                    val vertex = Factory.mutNode(String.valueOf(id))
                    vertex.add(Shape.ELLIPSE)
                    graph.add(vertex)
                    val leftNode = recurse(left)
                    val rightNode = recurse(right)

                    val leftLink = vertex.linkTo(leftNode).`with`(Style.DASHED)
                    vertex.addLink(leftLink)
                    val rightLink = vertex.linkTo(rightNode).`with`(Style.SOLID)
                    vertex.addLink(rightLink)

                    vertex
            }
            graph.add(v)
            v
        }

        recurse(diagnosticDecisionTree)

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
