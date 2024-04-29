package com.omar.pcconnector.ui.fs

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with


private val slidingExitToStartAnimation =
    fadeOut(tween(400)) +
            slideOutHorizontally(tween(400)) { -it }

private val slidingEnterFromStartAnimation =
    fadeIn(tween(400)) +
            slideInHorizontally(tween(400)) { -it }

private val slidingExitToEndAnimation =
    fadeOut(tween(400)) +
            slideOutHorizontally(tween(400)) { it }

private val slidingEnterFromEndAnimation =
    fadeIn(tween(400)) +
            slideInHorizontally(tween(400)) { it }

@OptIn(ExperimentalAnimationApi::class)
fun AnimatedContentScope<FileSystemTreeState>.createTransition(): ContentTransform {
    val initial = initialState
    val target = targetState

    return fadeIn(snap()) with fadeOut(snap())

    if (initial.directory == target.directory) {
        // same directory, don't animate
        return fadeIn(snap(0)) with fadeOut(snap(0))
    }
    return if (initial.directory.length > target.directory.length) {
        // we are moving backward
        slidingEnterFromStartAnimation with slidingExitToEndAnimation
    } else  {
        // we are moving forward
        slidingEnterFromEndAnimation with slidingExitToStartAnimation
    }
}