package bdd

import dft.DFT

import java.io.{BufferedReader, File, FileReader}
import scala.io.Source

object BDDOrdering {

    type DftId = String
    type BddId = String

    def extractBDDOrdering(file: File): Map[DftId, BddId] = {
        val map = Map.newBuilder[DftId, BddId]

        val reader = new BufferedReader(new FileReader(file))
        var line: String = null
        while {
            line = reader.readLine()
            line != null
        } do {
            line match
                case s"// ${basicevent_bdd_id} -> ${basicevent_dft_id}" =>
                    map.addOne(basicevent_dft_id -> basicevent_bdd_id)
                case _ =>
        }

        map.result()
    }

    def bddProbabilities(dftProbabilities: Map[DftId, Double],
                         bddDotfile: File): Map[BddId, Double] = {
        val bddIds: Map[DftId, BddId] = extractBDDOrdering(bddDotfile)

        dftProbabilities.map((dftId, prob) => (bddIds(dftId), prob))
    }

    def readBddProbabilities(dftGalileoFile: File, bddDotFile: File): Map[BddId, Double] = {
        val dftLines = DFT.readDFTFile(Source.fromFile(dftGalileoFile))
        val dftProbabilities: Map[DftId, Double] = DFT.getProbabilities(dftLines)
        bddProbabilities(dftProbabilities, bddDotFile)
    }
}
