package de.paso2.einkaufsliste.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuantityMergerTest {

    @Test
    fun mergeSameLiterUnits() {
        val result = QuantityMerger.merge(
            existingQty = 1.0, existingUnit = "l", existingNote = "",
            newQty = 2.0, newUnit = "l", newNote = ""
        )
        assertEquals(3.0, result.quantity!!, 1e-6)
        assertEquals("l", result.unit)
        assertEquals("3 l", QuantityMerger.formatQuantity(result.quantity, result.unit))
    }

    @Test
    fun mergeMillilitersAndLiters() {
        val result = QuantityMerger.merge(
            existingQty = 500.0, existingUnit = "ml", existingNote = "",
            newQty = 1.0, newUnit = "l", newNote = ""
        )
        assertEquals(1.5, result.quantity!!, 1e-6)
        assertEquals("l", result.unit)
        assertEquals("1,5 l", QuantityMerger.formatQuantity(result.quantity, result.unit))
    }

    @Test
    fun mergeGramsOver1000ConvertsToKg() {
        val result = QuantityMerger.merge(
            existingQty = 600.0, existingUnit = "g", existingNote = "",
            newQty = 400.0, newUnit = "g", newNote = ""
        )
        assertEquals(1.0, result.quantity!!, 1e-6)
        assertEquals("kg", result.unit)
        assertEquals("1 kg", QuantityMerger.formatQuantity(result.quantity, result.unit))
    }

    @Test
    fun mergeGramsAndKilograms() {
        val result = QuantityMerger.merge(
            existingQty = 500.0, existingUnit = "g", existingNote = "Magerquark",
            newQty = 1.0, newUnit = "kg", newNote = "Bio"
        )
        assertEquals(1.5, result.quantity!!, 1e-6)
        assertEquals("kg", result.unit)
        assertEquals("Magerquark • Bio", result.note)
    }

    @Test
    fun mergeNullExistingWithExplicitNew() {
        val result = QuantityMerger.merge(
            existingQty = null, existingUnit = null, existingNote = "",
            newQty = 2.0, newUnit = "l", newNote = ""
        )
        assertEquals(2.0, result.quantity!!, 1e-6)
        assertEquals("l", result.unit)
    }

    @Test
    fun mergeExplicitExistingWithNullNewIncrementsByOne() {
        val result = QuantityMerger.merge(
            existingQty = 2.0, existingUnit = "Stk", existingNote = "",
            newQty = null, newUnit = null, newNote = ""
        )
        assertEquals(3.0, result.quantity!!, 1e-6)
        assertEquals("Stk", result.unit)
    }

    @Test
    fun mergeBothNullQuantitiesKeepsNull() {
        val result = QuantityMerger.merge(
            existingQty = null, existingUnit = null, existingNote = "Kamille",
            newQty = null, newUnit = null, newNote = "Pfefferminz"
        )
        assertNull(result.quantity)
        assertEquals("Kamille • Pfefferminz", result.note)
    }

    @Test
    fun mergeIncompatibleUnitsPreservesBothInNote() {
        val result = QuantityMerger.merge(
            existingQty = 500.0, existingUnit = "g", existingNote = "",
            newQty = 1.0, newUnit = "Pkg", newNote = ""
        )
        assertEquals(500.0, result.quantity!!, 1e-6)
        assertEquals("g", result.unit)
        assertEquals("+ 1 Pkg", result.note)
    }
}
