package com.plusmobileapps.chefmate.recipe.exporter.impl

/**
 * Minimal, dependency-free ZIP writer. Each entry is stored uncompressed (compression method 0) so
 * we don't need a cross-platform DEFLATE implementation — the importer's [ZipReader sibling]
 * already supports STORED entries, and recipe HTML files are small enough that compression wouldn't
 * move the needle anyway. ZIP64 is not emitted; archive size must fit in 32-bit fields.
 */
internal object ZipWriter {

    class Entry(val name: String, val bytes: ByteArray)

    private const val LOCAL_FILE_HEADER_SIGNATURE = 0x04034b50
    private const val CENTRAL_DIR_HEADER_SIGNATURE = 0x02014b50
    private const val EOCD_SIGNATURE = 0x06054b50
    private const val METHOD_STORED = 0
    private const val VERSION = 20
    private const val UTF8_FLAG = 1 shl 11

    // Fixed timestamp written to every entry. Apps that show "Modified" dates will surface this
    // value, but it keeps the output deterministic, which makes tests trivial to assert against.
    private const val DOS_TIME: Int = 0
    private const val DOS_DATE: Int = (1 shl 9) or (1 shl 5) or 1 // 1980-01-01

    fun write(entries: List<Entry>): ByteArray {
        val out = ByteArrayBuilder()
        val centralDirOffsets = IntArray(entries.size)

        entries.forEachIndexed { index, entry ->
            centralDirOffsets[index] = out.size
            writeLocalFileHeader(out, entry)
        }

        val centralDirStart = out.size
        entries.forEachIndexed { index, entry ->
            writeCentralDirectoryHeader(out, entry, centralDirOffsets[index])
        }
        val centralDirEnd = out.size

        writeEocd(
            out = out,
            entryCount = entries.size,
            centralDirSize = centralDirEnd - centralDirStart,
            centralDirOffset = centralDirStart,
        )
        return out.toByteArray()
    }

    private fun writeLocalFileHeader(out: ByteArrayBuilder, entry: Entry) {
        val nameBytes = entry.name.encodeToByteArray()
        val crc = crc32(entry.bytes)
        out.writeIntLE(LOCAL_FILE_HEADER_SIGNATURE)
        out.writeShortLE(VERSION) // version needed
        out.writeShortLE(UTF8_FLAG)
        out.writeShortLE(METHOD_STORED)
        out.writeShortLE(DOS_TIME)
        out.writeShortLE(DOS_DATE)
        out.writeIntLE(crc.toInt())
        out.writeIntLE(entry.bytes.size)
        out.writeIntLE(entry.bytes.size)
        out.writeShortLE(nameBytes.size)
        out.writeShortLE(0) // extra length
        out.writeBytes(nameBytes)
        out.writeBytes(entry.bytes)
    }

    private fun writeCentralDirectoryHeader(out: ByteArrayBuilder, entry: Entry, localOffset: Int) {
        val nameBytes = entry.name.encodeToByteArray()
        val crc = crc32(entry.bytes)
        out.writeIntLE(CENTRAL_DIR_HEADER_SIGNATURE)
        out.writeShortLE(VERSION) // version made by
        out.writeShortLE(VERSION) // version needed
        out.writeShortLE(UTF8_FLAG)
        out.writeShortLE(METHOD_STORED)
        out.writeShortLE(DOS_TIME)
        out.writeShortLE(DOS_DATE)
        out.writeIntLE(crc.toInt())
        out.writeIntLE(entry.bytes.size)
        out.writeIntLE(entry.bytes.size)
        out.writeShortLE(nameBytes.size)
        out.writeShortLE(0) // extra length
        out.writeShortLE(0) // comment length
        out.writeShortLE(0) // disk number
        out.writeShortLE(0) // internal attrs
        out.writeIntLE(0) // external attrs
        out.writeIntLE(localOffset)
        out.writeBytes(nameBytes)
    }

    private fun writeEocd(
        out: ByteArrayBuilder,
        entryCount: Int,
        centralDirSize: Int,
        centralDirOffset: Int,
    ) {
        out.writeIntLE(EOCD_SIGNATURE)
        out.writeShortLE(0) // disk
        out.writeShortLE(0) // disk with start
        out.writeShortLE(entryCount)
        out.writeShortLE(entryCount)
        out.writeIntLE(centralDirSize)
        out.writeIntLE(centralDirOffset)
        out.writeShortLE(0) // comment length
    }

    // CRC32 over IEEE polynomial 0xEDB88320. Tablified on first call.
    private val crcTable: IntArray =
        IntArray(256) { i ->
            var c = i
            repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }
            c
        }

    private fun crc32(bytes: ByteArray): Long {
        var c = 0xFFFFFFFF.toInt()
        for (b in bytes) {
            c = crcTable[(c xor b.toInt()) and 0xFF] xor (c ushr 8)
        }
        return (c.inv().toLong()) and 0xFFFFFFFFL
    }
}

private class ByteArrayBuilder {
    private var buffer = ByteArray(1024)
    private var pos = 0

    val size: Int
        get() = pos

    fun writeShortLE(value: Int) {
        ensure(2)
        buffer[pos++] = (value and 0xFF).toByte()
        buffer[pos++] = ((value ushr 8) and 0xFF).toByte()
    }

    fun writeIntLE(value: Int) {
        ensure(4)
        buffer[pos++] = (value and 0xFF).toByte()
        buffer[pos++] = ((value ushr 8) and 0xFF).toByte()
        buffer[pos++] = ((value ushr 16) and 0xFF).toByte()
        buffer[pos++] = ((value ushr 24) and 0xFF).toByte()
    }

    fun writeBytes(bytes: ByteArray) {
        ensure(bytes.size)
        bytes.copyInto(buffer, pos)
        pos += bytes.size
    }

    fun toByteArray(): ByteArray = buffer.copyOf(pos)

    private fun ensure(extra: Int) {
        if (pos + extra <= buffer.size) return
        var newSize = buffer.size
        while (newSize < pos + extra) newSize *= 2
        buffer = buffer.copyOf(newSize)
    }
}
