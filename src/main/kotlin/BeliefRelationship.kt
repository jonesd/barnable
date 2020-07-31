class BeliefRelationship(from: Belief, to: Belief, relationship: RelationshipBind)

enum class RelationshipBind(val code: String) {
    Achievement("a"),
    Thwarting("t"),
    Realization("r"),
    Causation("c")
}