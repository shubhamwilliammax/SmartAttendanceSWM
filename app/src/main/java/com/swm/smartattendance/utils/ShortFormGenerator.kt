package com.swm.smartattendance.utils

import com.swm.smartattendance.model.ShortForm
import com.swm.smartattendance.model.ShortFormType

/**
 * Generates short forms for subjects and branches.
 * Uses common patterns: first letters of words, acronyms.
 */
object ShortFormGenerator {

    private val knownShortForms = mapOf(
        "Computer Networks" to "CN",
        "Mobile Communication" to "MC",
        "Theory of Computation" to "TOC",
        "Image Processing" to "IP",
        "Analog and Digital Communication" to "ADC",
        "Analog And Digital Communication" to "ADC",
        "Introduction to Cyber Security" to "ICS",
        "Introduction to Database Systems" to "IDBS",
        "Operating Systems" to "OS",
        "Cloud Computing" to "CC",
        "Machine Learning" to "ML",
        "Signals and Systems" to "SAS",
        "Computer Science" to "CS",
        "Computer Science Engineering" to "CSE",
        "Electronics and Communication" to "ECE",
        "Electrical Engineering" to "EE",
        "Mechanical Engineering" to "ME",
        "Civil Engineering" to "CE"
    )

    /**
     * Generate short form from full name
     */
    fun generate(fullName: String, type: ShortFormType): String {
        val trimmed = fullName.trim()
        if (trimmed.isBlank()) return ""
        return knownShortForms[trimmed] ?: generateFromWords(trimmed)
    }

    private fun generateFromWords(name: String): String {
        val words = name.split(Regex("\\s+")).filter { it.length > 1 }
        return when {
            words.isEmpty() -> name.take(3).uppercase()
            words.size == 1 -> words[0].take(3).uppercase()
            else -> words.mapNotNull { w ->
                when {
                    w.equals("and", true) -> null
                    w.equals("the", true) -> null
                    w.equals("of", true) -> null
                    w.equals("to", true) -> null
                    w.equals("in", true) -> null
                    w.length >= 2 -> w.first().uppercaseChar()
                    else -> null
                }
            }.joinToString("")
        }.ifBlank { name.take(4).uppercase() }
    }

    fun getOrCreate(
        fullName: String,
        type: ShortFormType,
        customShortForm: String?,
        existing: List<ShortForm>
    ): String {
        val existingForm = existing.find { it.fullName.equals(fullName, true) && it.type == type }
        if (existingForm != null) return existingForm.shortForm
        if (!customShortForm.isNullOrBlank()) return customShortForm.trim().uppercase()
        return generate(fullName, type)
    }
}
