package atlow.chemi.mymada.home

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode

fun SemanticsNodeInteractionsProvider.homeDestination(label: String) =
    onNode(hasScrollToIndexAction()).performScrollToNode(hasText(label)).let {
        onNodeWithText(label)
    }
