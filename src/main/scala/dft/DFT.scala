package dft

import benchmark.Conversion

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.immutable.IntMap
import scala.collection.mutable
import scala.io.Source

enum DFTNode:
    case BasicEvent(id: String, probability: Double)
    case OrEvent(id: String, children: Seq[String])
    case AndEvent(id: String, children: Seq[String])
    case TopLevel(id: String)

object Printing {

    def print(dftNode: DFTNode): String = dftNode match {
        case DFTNode.TopLevel(id) => s"""toplevel "${id}";"""
        case DFTNode.OrEvent(id, children) => s""""${id}" or ${children.mkString("\"", "\" \"","\"")};"""
        case DFTNode.AndEvent(id, children) => s""""${id}" and ${children.mkString("\"", "\" \"","\"")};"""
        case DFTNode.BasicEvent(id, probability) => s""""${id}" prob=${probability};"""
    }

}

object Parsing {
    import fastparse.*
    import fastparse.NoWhitespace.*

    def dftToplevel[$: P]: P[DFTNode.TopLevel] =
        P("toplevel \"" ~ CharsWhile(_ != '\"').! ~ "\";")
            .map { x => DFTNode.TopLevel(x) }
    def dftOr[$: P]: P[DFTNode.OrEvent] =
        P("\"" ~ CharsWhile(_ != '\"').! ~ "\" or " ~ ("\"" ~ CharsWhile(_ != '\"').! ~ "\"").rep(sep=" ") ~ ";")
            .map { case (x, y) => DFTNode.OrEvent(x, y) }
    def dftAnd[$: P]: P[DFTNode.AndEvent] =
        P("\"" ~ CharsWhile(_ != '\"').! ~ "\" and " ~ ("\"" ~ CharsWhile(_ != '\"').! ~ "\"").rep(sep=" ") ~ ";")
            .map { case (x, y) => DFTNode.AndEvent(x, y) }
    def dftBasic[$: P]: P[DFTNode.BasicEvent] =
        P("\"" ~ CharsWhile(_ != '\"').! ~ "\" prob=" ~ CharsWhile(_ != ';').! ~ ";")
            .map { case (x, y) => DFTNode.BasicEvent(x, y.toDouble) }
    def dftLine[$: P]: P[DFTNode] = dftToplevel | dftOr | dftAnd | dftBasic

    def parseDFTLine(line: String): DFTNode = {
        val Parsed.Success(parsed, _) = parse(line, dftLine)
        parsed
    }

    def main(args: Array[String]): Unit = {
        val topExample = "toplevel \"swag\";"
        val orExample = "\"asdf\" or \"gh  jkl;\" \"yolo\";"
        val andExample = "\"asdf\" and \"ghjkl;\" \"yolo\";"
        val basicExample = "\"swag\" prob=0.12345;"

        val Parsed.Success(foo, _) = parse(topExample, dftToplevel)
        val Parsed.Success(bar, _) = parse(orExample, dftOr)
        val Parsed.Success(qux, _) = parse(andExample, dftAnd)
        val Parsed.Success(baz, _) = parse(basicExample, dftBasic)

        val Parsed.Success(_, _) = parse(topExample, dftLine)
        val Parsed.Success(_, _) = parse(orExample, dftLine)
        val Parsed.Success(_, _) = parse(andExample, dftLine)
        val Parsed.Success(_, _) = parse(basicExample, dftLine)

        println(foo)
        println(bar)
        println(qux)
        println(baz)
    }

}

object DFT {
    
    def getProbabilities(dftLines: Seq[DFTNode]): Map[String, Double] =
        Map.from(dftLines.collect {
            case DFTNode.BasicEvent(id, prob) => (id, prob)
        })

    def writeDFTFile(output: File, lines: Seq[DFTNode]): Unit = {
        val writer = new BufferedWriter(new FileWriter(output))
        for (line <- lines) {
            writer.write(Printing.print(line))
            writer.newLine()
        }
        writer.close()
    }

    def readDFTFile(source: Source): Seq[DFTNode] =
        source.getLines().map(Parsing.parseDFTLine).toSeq

    def readTreeLikeFaultTree(source: Source): faulttree.FaultTree =
        Conversion.translateToTreeLikeFaultTree(readDFTFile(source))

    def readDagLikeFaultTree(source: Source): minimalcutpathset.FaultTree =
        Conversion.translateToDagTree(readDFTFile(source))
    
    def main(args: Array[String]): Unit = {
        val source = Source.fromResource("AssessingtheRisks1.dft")

        val dftNodes = DFT.readDFTFile(source)
        println(s"DEBUG: how many lines?: ${dftNodes.size}")

        for (node <- dftNodes) {
            println(node)
        }

        println(Conversion.translateToTreeLikeFaultTree(dftNodes))
    }

}
