package com.omar.pcconnector.ui.fs


import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

val slidingExitToStartAnimation =
    fadeOut(tween(200)) +
            slideOutHorizontally(tween(200)) { -it / 3 }

val slidingEnterFromStartAnimation =
    fadeIn(tween(250)) +
            slideInHorizontally(tween(250)) { -it / 3 }

val slidingExitToEndAnimation =
    fadeOut(tween(200)) +
            slideOutHorizontally(tween(200)) { it / 3 }

val slidingEnterFromEndAnimation =
    fadeIn(tween(250)) +
            slideInHorizontally(tween(250)) { it / 3 }


@OptIn(ExperimentalAnimationApi::class)
fun AnimatedContentTransitionScope<FileSystemTreeState>.createTransition(): ContentTransform {

    val initial = this.initialState
    val target = this.targetState

    if (initial.directory == target.directory) {
        // same directory, don't animate
        return fadeIn(snap(0)) togetherWith  fadeOut(snap(0))
    }
    return if (initial.directory.length > target.directory.length) {
        // we are moving backward
        slidingEnterFromStartAnimation togetherWith slidingExitToEndAnimation
    } else  {
        // we are moving forward
        slidingEnterFromEndAnimation togetherWith slidingExitToStartAnimation
    }
}