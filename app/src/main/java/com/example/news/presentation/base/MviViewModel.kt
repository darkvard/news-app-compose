package com.example.news.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything the screen renders. Immutable: always replaced with `copy(...)`, never mutated. */
interface UiState

/** Something the user did (tap, type, long-press...). The only way the UI talks to a ViewModel. */
interface UiIntent

/** A one-shot event for the UI (toast, open a link...). Consumed once, never re-delivered on rotation. */
interface UiEffect

/**
 * MVVM + MVI base:
 * - **MVVM**: a lifecycle-aware ViewModel exposes observable state ([state]) to a passive View.
 * - **MVI**: one entry point for user actions ([onIntent]), state changes only through a reducer
 *   ([setState]), side effects through a separate one-shot channel ([effects]).
 *
 * Data flows one way:  UI --Intent--> ViewModel --(use cases)--> setState --> UI renders State
 *                                                          \--> sendEffect --> UI runs Effect once
 *
 * A screen without intents or effects uses `Nothing` for those type parameters.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    /** Latest state, for reading inside intent handlers. */
    protected val currentState: S get() = _state.value

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    /** Single entry point for everything the user does on the screen. */
    abstract fun onIntent(intent: I)

    /** Reducer: produce the next state from the current one (thread-safe, atomic). */
    protected fun setState(reducer: S.() -> S) = _state.update(reducer)

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
