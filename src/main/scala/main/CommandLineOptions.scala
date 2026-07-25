package main

import java.nio.file.Path

case class CommandLineOptions(
    inputFile: Path = Path.of("input.dft"),     // .dft file (galileo format) representing Fault Tree
    outputFile: Path = Path.of("output.dot"),   // .dot file (graphviz format) representing Diagnostic Decision Tree
    algorithim: Algorithm = Algorithm.Buda,
)

enum Algorithm {
    case Buda // recursive algorithm
    case Cuda // cutset-based algorithm
    case Pada // pathset-based algorithm
}

object Algorithm {
    given scopt.Read[Algorithm] = scopt.Read.reads(Algorithm.valueOf)
}

object CommandLineOptions {
    import scopt.OParser

    val builder = OParser.builder[CommandLineOptions]
    val parser = {
        import builder.*
        OParser.sequence(
            programName("java -jar FaultTreeHeight.jar"),
            head("FaultTreeHeight", "1.x"),
            opt[Path]('i', "input")
                .required()
                .valueName("<file>")
                .action((x, c) => c.copy(inputFile = x))
                .text("Your Fault Tree input file (Galileo format)"),
            opt[Path]('o', "output")
                .required()
                .valueName("<file>")
                .action((x, c) => c.copy(outputFile = x))
                .text("Diagnostic Decision Tree file (Graphviz format)"),
            opt[Algorithm]('a', "algorithm")
                .required()
                .valueName("<algorithm>")
                .action((x, c) => c.copy(algorithim = x))
                .text(s"Algorithm; one of ${Algorithm.values.mkString("[", ", ", "]")}"),
        )
    }

    // Note: when None is returned, scopt has already printed an error message.
    def parse(args: Array[String]): Option[CommandLineOptions] =
        OParser.parse(parser, args, CommandLineOptions())
}
