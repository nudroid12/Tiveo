package com.vueo.shared.core.update

private val legacyReleaseBrand = Regex(
    """^(Latest successful )?(?:VUEO|Virio|Tivio)(?=\s|$)""",
    RegexOption.IGNORE_CASE,
)

/** Normalize titles and generated notes from older release manifests. */
fun String.tiveoReleaseDisplayText(): String =
    legacyReleaseBrand.replace(this) { match -> match.groupValues[1] + "Tiveo" }
