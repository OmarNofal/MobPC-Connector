package com.omar.pcconnector.ui.fs


import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import java.nio.file.Paths

val slidingExitToStartAnimation =
    fadeOut(tween(200)) +
            slideOutHorizontally(tween(200)) { -it / 2 }

val slidingEnterFromStartAnimation =
    fadeIn(tween(250)) +
            slideInHorizontally(tween(250)) { -it / 2 }

val slidingExitToEndAnimation =
    fadeOut(tween(200)) +
            slideOutHorizontally(tween(200)) { it / 2 }

val slidingEnterFromEndAnimation =
    fadeIn(tween(250)) +
            slideInHorizontally(tween(250)) { it / 2 }

val slidingExitToUp =
    fadeOut(tween(200)) +
            slideOutVertically { -it / 3 }

val slidingEnterFromUp =
    fadeIn(tween(200)) +
            slideInVertically { -it / 3 }

val slidingEnterFromDown =
    fadeIn(tween(200)) +
            slideInVertically { it / 3 }

val slidingExitToDown =
    fadeOut(tween(200)) +
            slideOutVertically { it / 3 }

fun AnimatedContentTransitionScope<FileSystemTreeState>.createTransition(): ContentTransform {

    val initialDir = this.initialState.directory
    val targetDir = this.targetState.directory

    val isChildDirectory = targetDir.startsWith(initialDir, ignoreCase = true)
    val isParentDirectory = initialDir.startsWith(targetDir, ignoreCase = true)

    if (initialDir == targetDir) {
        // same directory, don't animate
        return fadeIn(snap(0)) togetherWith  fadeOut(snap(0))
    }
    return if (isParentDirectory) {
        // we are moving backward
        slidingEnterFromStartAnimation togetherWith slidingExitToEndAnimation
    } else if (isChildDirectory) {
        // we are moving forward
        slidingEnterFromEndAnimation togetherWith slidingExitToStartAnimation
    } else {
        // possibly jumping around non-linearly (switching drives, etc..)
        fadeIn(tween(250, delayMillis = 100)) togetherWith fadeOut(tween(100))
    }
}