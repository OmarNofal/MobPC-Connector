package com.omar.pcconnector.operation

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents any operation that the app can
 * achieve in the server
 *
 * Note that the start method is a suspend function so cancelling the job is
 * the way to cancel the operation itself. It is up to the operation to cancel itself
 */
abstract class Operation<T> {

    /**
     * Name of the operation like Copy, Download, etc...
     */
    abstract val name: String

    /**
     * The current status of the operation.
     * Can be used to show status updates
     */
//    abstract val status: MutableStateFlow<State>

    /**
     * User-friendly description
     */
    abstract val operationDescription: String

    /**
     * Start the operation and return the result
     */
    abstract suspend fun start(): T
}

/**
 * An operation which its progress can be tracked.
 * For example, a download operation mostly takes
 * a longer time so it is easy to track the progress
 *
 * @param S the type of progress updated by the operation
 * @param R the result type
 */
abstract class MonitoredOperation<S, R> : Operation<R>() {

    abstract val progress: StateFlow<S>

}