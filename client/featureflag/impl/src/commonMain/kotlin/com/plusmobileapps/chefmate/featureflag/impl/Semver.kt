package com.plusmobileapps.chefmate.featureflag.impl

internal fun compareSemver(a: String, b: String): Int {
    val aParts = a.substringBefore('-').split('.').mapNotNull { it.toIntOrNull() }
    val bParts = b.substringBefore('-').split('.').mapNotNull { it.toIntOrNull() }
    val len = maxOf(aParts.size, bParts.size)
    for (i in 0 until len) {
        val diff = aParts.getOrElse(i) { 0 } - bParts.getOrElse(i) { 0 }
        if (diff != 0) return diff
    }
    return 0
}
