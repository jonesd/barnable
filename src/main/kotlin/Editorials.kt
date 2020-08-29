class Editorial(val content: String, val writer: String, val country: String) {
}

val ED_JOBS = Editorial("""
Recent protectionist measures by the Reagan administration have disappointed us. Voluntary limits on Japanese automobiles and voluntary limits on steel by the Common Market are bad for the nation. They do not promote the long-run health of the industries affected. The problem of the automobile and steel industries is: in both industries, average wage rates are twice as high as the average. Far from saving jobs, the limitations on imports will cost jobs. If we import less to spend on American exports. The result will be fewer jobs in export industries.""",
    "Milton Friedman",
    "U.S.")

// Note the text was modified (trimmed) from the original published
val ED_RESTRICTIONS = Editorial("""
The American machine-tool industry is seeking protection from foreign competition. The industry has been hurt by cheaper machine tools from Japan. The toolmakers argue that restrictions on imports must be imposed so industry can survive. It is a wrongheaded argument. Restrictions on imports would mean that American manufacturers would have to make do with more expensive American machine tools. Inevitably those American manufacturers would produce more expensive products. They would lose sales. Then those manufactures would demand protection against foreign competition.""".trimIndent(),
    "Lance Morrow",
    "U.S.")

