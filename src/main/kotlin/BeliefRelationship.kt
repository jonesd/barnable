class BeliefRelationship(from: Belief, to: Belief, relationship: BeliefRelationship) {
}

enum class RelationshipBind(val code: String) {
    Achievement("a"),
    Thwarting("t"),
    Realization("r"),
    Causation("c")
}