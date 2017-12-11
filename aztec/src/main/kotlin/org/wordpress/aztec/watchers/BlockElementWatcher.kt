package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.spans.MarkForReplay
import java.lang.ref.WeakReference
import java.util.ArrayList

open class BlockElementWatcher(aztecText: AztecText) : TextWatcher {
    val handlers = ArrayList<TextChangeHandler>()

    interface TextChangeHandler {
        fun handleTextChanged(text: Spannable, inputStart: Int, count: Int, nestingLevel: Int)
    }

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) { }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (count == 0) {
            // clear deletions are handled elsewhere. We only care about newline and END_OF_BUFFER_MARKER additions here!
            return
        }

        var startIndex = start
        var charCount = count

        do {
            val nestingLevelToProcess = IAztecNestable.getNestingLevelAt(s as Spanned, startIndex, startIndex + charCount)

            // handle the text change. The potential text deletion will happen in a scheduled Runnable, to run on next frame
            handlers.forEach { textChangeHandler ->
                textChangeHandler.handleTextChanged(
                        s as Spannable,
                        startIndex,
                        charCount,
                        nestingLevelToProcess)
            }

            // check for a replay marker and use its bounds
            val hasReplay = aztecTextRef.get()?.text?.let text@ { text ->
                text.getSpans(0, s.length, MarkForReplay::class.java).firstOrNull { mark ->
                    if (mark != null) {
                        startIndex = text.getSpanStart(mark)
                        charCount = text.getSpanEnd(mark) - startIndex

                        // remove the replay mark since we're about to do the replay now
                        text.removeSpan(mark)

                        return@text true
                    } else {
                        return@text false
                    }
                }
                return@text false
            } ?: false
        } while (hasReplay)
    }

    override fun afterTextChanged(text: Editable) {}

    fun add(textChangeHandler: TextChangeHandler): BlockElementWatcher {
        handlers.add(textChangeHandler)
        return this
    }

    fun install(text: AztecText): BlockElementWatcher {
        text.addTextChangedListener(this)
        return this
    }

    companion object {
        fun replay(text: Spannable, index: Int) {
            text.setSpan(MarkForReplay(), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
