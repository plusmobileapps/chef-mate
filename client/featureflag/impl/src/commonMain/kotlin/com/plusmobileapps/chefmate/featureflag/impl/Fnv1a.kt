package com.plusmobileapps.chefmate.featureflag.impl

// kotlin.String.hashCode() is not guaranteed identical across JVM and Kotlin/Native, so a flag
// rolled out at 50% would assign different users on Android vs iOS. FNV-1a 32-bit is platform-
// independent and good enough for bucketing (no security claims).
internal fun fnv1a(s: String): UInt {
    var hash = 2166136261u
    for (byte in s.encodeToByteArray()) {
        hash = (hash xor byte.toUByte().toUInt()) * 16777619u
    }
    return hash
}

internal fun bucket(flagKey: String, identity: String): Int =
    (fnv1a("$flagKey:$identity") % 100u).toInt()
