package org.dhis2.commons.date

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeVisualTransformation

class CustomDateTransformation : DateTimeVisualTransformation {

    companion object {
        private const val SEPARATOR = "-"
        internal const val DATE_MASK = "DDMMYYYY"
    }

    override val maskLength: Int
        get() = DATE_MASK.length

    override fun filter(text: AnnotatedString): TransformedText {
        return dateFilter(text)
    }

    private fun dateFilter(text: AnnotatedString): TransformedText {
        val originalLength = text.text.length

        val input = if (originalLength > DATE_MASK.length) text.text.substring(0, 8) else text.text

        val day = input.take(2).padEnd(2, 'D')
        val month = input.drop(2).take(2).padEnd(2, 'M')
        val year = input.drop(4).take(4).padEnd(4, 'Y')

        val transformed = "$year$SEPARATOR$month$SEPARATOR$day"

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 1 -> offset + 8
                    offset <= 3 -> offset + 3
                    offset <= 7 -> offset - 4
                    else -> transformed.length
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val calculatedOffset = when (offset) {
                    in 0..3 -> offset + 4
                    4 -> offset
                    in 5..6 -> offset - 3
                    7 -> offset - 6
                    in 8..9 -> offset - 8
                    else -> 0
                }

                return if (calculatedOffset > originalLength) originalLength else calculatedOffset
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
