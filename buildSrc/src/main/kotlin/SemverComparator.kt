class SemverComparator : Comparator<SemVer> {
    override fun compare(p0: SemVer, p1: SemVer): Int {
        if (p0.major > p1.major) return 1
        if (p0.major < p1.major) return -1
        if (p0.minor > p1.minor) return 1
        if (p0.minor < p1.minor) return -1
        if (p0.patch > p1.patch) return 1
        if (p0.patch < p1.patch) return -1

        if (p0.preRelease == p1.preRelease) return 0
        if (p0.preRelease.isNotBlank() && p1.preRelease.isBlank()) return -1
        if (p0.preRelease.isBlank() && p1.preRelease.isNotBlank()) return 1

        val parts = p0.preRelease.split(".")
        val otherParts = p1.preRelease.split(".")

        val endIndex = parts.size.coerceAtMost(otherParts.size) - 1
        for (i in 0..endIndex) {
            val part = parts[i]
            val otherPart = otherParts[i]
            if (part == otherPart) continue

            val partIsNumeric = part.isNumeric()
            val otherPartIsNumeric = otherPart.isNumeric()

            when {
                partIsNumeric && !otherPartIsNumeric -> return -1
                !partIsNumeric && otherPartIsNumeric -> return 1
                !partIsNumeric && !otherPartIsNumeric ->
                    if (part > otherPart) return 1
                    else if (part < otherPart) return -1
                else -> {
                    val partInt = part.toInt()
                    val otherPartInt = otherPart.toInt()
                    if (partInt > otherPartInt) return 1
                    if (partInt < otherPartInt) return -1
                }
            }
        }

        return if (parts.size == endIndex + 1 && otherParts.size > endIndex + 1) -1
        else if (parts.size > endIndex + 1 && otherParts.size == endIndex + 1) 1
        else 0
    }

    private fun String.isNumeric(): Boolean = toBigIntegerOrNull() != null
}
