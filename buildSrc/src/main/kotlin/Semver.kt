/**
 * Version number in [Semantic Versioning 2.0.0](http://semver.org/spec/v2.0.0.html) specification (SemVer).
 *
 * @property major major version, increment it when you make incompatible API changes.
 * @property minor minor version, increment it when you add functionality in a backwards-compatible manner.
 * @property patch patch version, increment it when you make backwards-compatible bug fixes.
 * @property preRelease pre-release version.
 * @property buildMetadata build metadata.
 */
data class SemVer(val major: Int = 0, val minor: Int = 0, val patch: Int = 0, val preRelease: String = "", val buildMetadata: String = "") {
    companion object {
        fun parse(version: String): SemVer =
            semverRegex.matchEntire(version)
                ?.toSemVer()
                ?: throw IllegalArgumentException("invalid version string `$version`")

        private fun MatchResult.toSemVer(): SemVer {
            fun <T> MatchResult.matchOrElse(n: Int, def: T, t: (String) -> T): T =
                if (groupValues[n].isEmpty()) def else t(groupValues[n])

            return SemVer(
                matchOrElse(1, 0) { it.toInt() },
                matchOrElse(2, 0) { it.toInt() },
                matchOrElse(3, 0) { it.toInt() },
                matchOrElse(4, "") { it },
                matchOrElse(5, "") { it },
            )
        }

        private val semverRegex =
            Regex("""(0|[1-9]\d*)?(?:\.)?(0|[1-9]\d*)?(?:\.)?(0|[1-9]\d*)?(?:-([\dA-z\-]+(?:\.[\dA-z\-]+)*))?(?:\+([\dA-z\-]+(?:\.[\dA-z\-]+)*))?""")
        private val extraInfoRegex =
            Regex("""[\dA-z\-]+(?:\.[\dA-z\-]+)*""")
    }

    init {
        require(major >= 0) { "major not positive" }
        require(minor >= 0) { "minor not positive" }
        require(patch >= 0) { "patch not positive" }
        if (preRelease.isNotBlank()) require(preRelease.matches(extraInfoRegex)) { "Pre-release version is not valid" }
        if (buildMetadata.isNotBlank()) require(buildMetadata.matches(extraInfoRegex)) { "Build metadata is not valid" }
    }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        if (preRelease.isNotBlank()) {
            append("-$preRelease")
        }
        if (buildMetadata.isNotBlank()) {
            append("+$buildMetadata")
        }
    }

    val isPreRelease: Boolean = preRelease.isNotBlank()
}
