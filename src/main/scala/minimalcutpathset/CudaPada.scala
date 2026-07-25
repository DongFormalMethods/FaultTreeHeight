package minimalcutpathset

import decisiontree.BinaryDecisionTree

def CuDAProb(faultTree: FaultTree): BinaryDecisionTree = {
    val basicEvents = getBasicEvents(faultTree)
    val probabilities = getProbabilities(faultTree)(basicEvents)
    val cutSets = minimalCutSets(faultTree)(basicEvents)
    CuDAprob(cutSets, probabilities)
}

def CuDAprob(cutSets: Set[Set[Event]], probabilities: Map[Event, Probability]): BinaryDecisionTree = {
    if cutSets.isEmpty then
        BinaryDecisionTree.Zero
    else if cutSets.contains(Set.empty[Event]) then
        BinaryDecisionTree.One
    else
        val Cstar: Set[Event] = cutSets.maxBy(cs => cutSetProbability(cs, probabilities))
        val b: Event = Cstar.minBy(probabilities)
        BinaryDecisionTree.NonLeaf(b, CuDAprob(setsWithoutB(cutSets, b), probabilities), CuDAprob(setsWithBRemoved(cutSets, b), probabilities))
}

def setsWithBRemoved(cutSets: Set[Set[Event]], b: Event): Set[Set[Event]] =
    cutSets.map(cutSet => cutSet - b)

def setsWithoutB(cutSets: Set[Set[Event]], b: Event): Set[Set[Event]] =
    cutSets.filter(cutSet => !cutSet.contains(b))



def PaDAprob(faultTree: FaultTree): BinaryDecisionTree = {
    val basicEvents = getBasicEvents(faultTree)
    val probabilities = getProbabilities(faultTree)(basicEvents)
    val pathSets = minimalPathSets(faultTree)(basicEvents)
    PaDAprob(pathSets, probabilities)
}

def PaDAprob(pathSets: Set[Set[Event]], probabilities: Map[Event, Probability]): BinaryDecisionTree = {
    if pathSets.isEmpty then
        BinaryDecisionTree.Zero
    else if pathSets.contains(Set.empty[Event]) then
        BinaryDecisionTree.One
    else
        val Cstar: Set[Event] = pathSets.maxBy(ps => pathSetProbability(ps, probabilities))
        val b: Event = Cstar.maxBy(probabilities)
        BinaryDecisionTree.NonLeaf(b, PaDAprob(setsWithBRemoved(pathSets, b), probabilities), PaDAprob(setsWithoutB(pathSets, b), probabilities))
}