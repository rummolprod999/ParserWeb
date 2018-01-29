fun GetConformity(conf: String): Int {
    val s = conf.toLowerCase()
    return when {
        s.contains("открыт") -> 5
        s.contains("аукцион") -> 1
        s.contains("котиров") -> 2
        s.contains("предложен") -> 3
        s.contains("единств") -> 4
        else -> 6
    }
}