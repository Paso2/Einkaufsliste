package de.paso2.einkaufsliste.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

data class MergeResult(
    val quantity: Double?,
    val unit: String?,
    val note: String
)

object QuantityMerger {

    val standardUnits = listOf(
        "Stk", "g", "kg", "ml", "l", "Pkg", "Becher",
        "Flasche", "Dose", "Glas", "Bund", "Rolle", "Tube", "Tafel", "Netz", "Kasten"
    )

    private val germanFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.GERMAN))

    fun formatQuantity(quantity: Double?, unit: String?): String {
        if (quantity == null) return ""
        val numStr = if (abs(quantity - quantity.toLong()) < 1e-6) {
            quantity.toLong().toString()
        } else {
            germanFormat.format(quantity)
        }
        return if (unit.isNullOrBlank()) numStr else "$numStr $unit"
    }

    fun merge(
        existingQty: Double?,
        existingUnit: String?,
        existingNote: String,
        newQty: Double?,
        newUnit: String?,
        newNote: String
    ): MergeResult {
        val normExistingUnit = normalizeUnit(existingUnit)
        val normNewUnit = normalizeUnit(newUnit)

        val (mergedQty, mergedUnit, extraNote) = mergeQuantities(
            existingQty, normExistingUnit,
            newQty, normNewUnit
        )

        val mergedNote = mergeNotes(existingNote, newNote, extraNote)
        return MergeResult(mergedQty, mergedUnit, mergedNote)
    }

    private fun mergeQuantities(
        q1: Double?, u1: String?,
        q2: Double?, u2: String?
    ): Triple<Double?, String?, String?> {
        if (q1 == null && q2 == null) {
            return Triple(null, u1 ?: u2, null)
        }
        if (q1 == null && q2 != null) {
            return Triple(q2, u2 ?: u1, null)
        }
        if (q1 != null && q2 == null) {
            // For g/ml, adding without specifying a quantity shouldn't add 1g/1ml
            val nextQty = if (u1 == "g" || u1 == "ml") q1 else q1 + 1.0
            return Triple(nextQty, u1 ?: u2, null)
        }

        // Both q1 and q2 are non-null
        val v1 = q1!!
        val v2 = q2!!

        // Same unit (or one/both null)
        if (u1 == u2 || u1 == null || u2 == null) {
            val targetUnit = u1 ?: u2
            return normalizeScale(v1 + v2, targetUnit)
        }

        // Mass conversion: g <-> kg
        val g1 = toGrams(v1, u1)
        val g2 = toGrams(v2, u2)
        if (g1 != null && g2 != null) {
            return normalizeScale(g1 + g2, "g")
        }

        // Volume conversion: ml <-> l
        val ml1 = toMilliliters(v1, u1)
        val ml2 = toMilliliters(v2, u2)
        if (ml1 != null && ml2 != null) {
            return normalizeScale(ml1 + ml2, "ml")
        }

        // Incompatible units: keep primary quantity & append secondary to note
        val extra = "+ ${formatQuantity(v2, u2)}"
        return Triple(v1, u1, extra)
    }

    private fun normalizeScale(value: Double, unit: String?): Triple<Double?, String?, String?> {
        return when {
            unit == "g" && value >= 1000.0 -> Triple(value / 1000.0, "kg", null)
            unit == "ml" && value >= 1000.0 -> Triple(value / 1000.0, "l", null)
            else -> Triple(value, unit, null)
        }
    }

    private fun toGrams(value: Double, unit: String?): Double? = when (unit) {
        "g" -> value
        "kg" -> value * 1000.0
        else -> null
    }

    private fun toMilliliters(value: Double, unit: String?): Double? = when (unit) {
        "ml" -> value
        "l" -> value * 1000.0
        else -> null
    }

    private fun normalizeUnit(unit: String?): String? {
        val trimmed = unit?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return when (trimmed.lowercase(Locale.GERMAN)) {
            "g", "gr", "gramm" -> "g"
            "kg", "kilo", "kilogramm" -> "kg"
            "ml", "milliliter" -> "ml"
            "l", "liter" -> "l"
            "stk", "stück", "st" -> "Stk"
            "pkg", "packung", "packungen", "pck" -> "Pkg"
            else -> trimmed
        }
    }

    private fun mergeNotes(existing: String, incoming: String, extraQtyNote: String?): String {
        val parts = linkedSetOf<String>()
        existing.split("•").map { it.trim() }.filter { it.isNotEmpty() }.forEach { parts.add(it) }
        incoming.split("•").map { it.trim() }.filter { it.isNotEmpty() }.forEach { parts.add(it) }
        extraQtyNote?.trim()?.takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        return parts.joinToString(" • ")
    }
}
