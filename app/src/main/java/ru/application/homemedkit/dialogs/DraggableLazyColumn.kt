package ru.application.homemedkit.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
fun rememberDraggableListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit
): DraggableListState {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DraggableListState(
            listState = lazyListState,
            onMove = onMove,
            scope = scope
        )
    }
    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            lazyListState.scrollBy(diff)
        }
    }
    return state
}

class DraggableListState internal constructor(
    val listState: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)

    private var draggingItemInitialOffset by mutableIntStateOf(0)

    val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    var previousItemOffset = Animatable(0f)
        private set

    internal fun onDragStart(index: Int) {
        listState.layoutInfo.visibleItemsInfo
            .find { it.index == index }
            ?.also {
                draggingItemIndex = it.index
                draggingItemInitialOffset = it.offset
            }
    }

    internal fun onDragStart(key: Any) {
        listState.layoutInfo.visibleItemsInfo
            .find { it.key == key }
            ?.also {
                draggingItemIndex = it.index
                draggingItemInitialOffset = it.offset
            }
    }

    internal fun onDragInterrupted() {
        if (draggingItemIndex != null) {
            previousIndexOfDraggedItem = draggingItemIndex
            val startOffset = draggingItemOffset
            scope.launch {
                previousItemOffset.snapTo(startOffset)
                previousItemOffset.animateTo(
                    0f,
                    spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 1f)
                )
                previousIndexOfDraggedItem = null
            }
        }
        draggingItemDraggedDelta = 0f
        draggingItemIndex = null
        draggingItemInitialOffset = 0
    }

    internal fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset.y

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = listState.layoutInfo.visibleItemsInfo.find { item ->
            middleOffset.toInt() in item.offset..item.offsetEnd &&
                    draggingItem.index != item.index
        }

        if (targetItem != null) {
            if (
                draggingItem.index == listState.firstVisibleItemIndex ||
                targetItem.index == listState.firstVisibleItemIndex
            ) {
                listState.requestScrollToItem(
                    listState.firstVisibleItemIndex,
                    listState.firstVisibleItemScrollOffset
                )
            }
            onMove(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll = when {
                draggingItemDraggedDelta > 0 ->
                    (endOffset - listState.layoutInfo.viewportEndOffset).coerceAtLeast(0f)

                draggingItemDraggedDelta < 0 ->
                    (startOffset - listState.layoutInfo.viewportStartOffset).coerceAtMost(0f)

                else -> 0f
            }
            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll)
            }
        }
    }

    private val LazyListItemInfo.offsetEnd: Int
        get() = this.offset + this.size
}

fun Modifier.dragHandle(
    state: DraggableListState,
    index: Int,
    onlyAfterLongPress: Boolean = false
): Modifier {
    return pointerInput(state) {
        if (onlyAfterLongPress) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, offset ->
                    change.consume()
                    state.onDrag(offset = offset)
                },
                onDragStart = { state.onDragStart(index = index) },
                onDragEnd = { state.onDragInterrupted() },
                onDragCancel = { state.onDragInterrupted() }
            )
        } else {
            detectDragGestures(
                onDrag = { change, offset ->
                    change.consume()
                    state.onDrag(offset = offset)
                },
                onDragStart = { state.onDragStart(index = index) },
                onDragEnd = { state.onDragInterrupted() },
                onDragCancel = { state.onDragInterrupted() }
            )
        }
    }
}

fun Modifier.dragHandle(
    state: DraggableListState,
    key: Any,
    onlyAfterLongPress: Boolean = false
): Modifier {
    return pointerInput(state) {
        if (onlyAfterLongPress) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, offset ->
                    change.consume()
                    state.onDrag(offset = offset)
                },
                onDragStart = { state.onDragStart(key = key) },
                onDragEnd = { state.onDragInterrupted() },
                onDragCancel = { state.onDragInterrupted() }
            )
        } else {
            detectDragGestures(
                onDrag = { change, offset ->
                    change.consume()
                    state.onDrag(offset = offset)
                },
                onDragStart = { state.onDragStart(key = key) },
                onDragEnd = { state.onDragInterrupted() },
                onDragCancel = { state.onDragInterrupted() }
            )
        }
    }
}

inline fun <T> LazyListScope.draggableItemsIndexed(
    state: DraggableListState,
    items: List<T>,
    noinline key: ((index: Int, item: T) -> Any)? = null,
    crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T, isDragging: Boolean) -> Unit
) = itemsIndexed(
    items = items,
    key = key,
    contentType = contentType
) { index, item ->

    val isDragging = index == state.draggingItemIndex
    val draggingModifier = if (isDragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer { translationY = state.draggingItemOffset }
    } else if (index == state.previousIndexOfDraggedItem) {
        Modifier
            .zIndex(1f)
            .graphicsLayer { translationY = state.previousItemOffset.value }
    } else {
        Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
    }
    Box(modifier = draggingModifier) {
        itemContent(index, item, isDragging)
    }
}

inline fun <T> LazyListScope.draggableItems(
    state: DraggableListState,
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    crossinline contentType: (item: T) -> Any? = { _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(item: T, isDragging: Boolean) -> Unit
) = draggableItemsIndexed(
    state = state,
    items = items,
    key = key?.let { block -> { _, item -> block(item) } },
    contentType = { _, item -> contentType(item) },
    itemContent = { _, item, isDragging -> itemContent(item, isDragging) }
)