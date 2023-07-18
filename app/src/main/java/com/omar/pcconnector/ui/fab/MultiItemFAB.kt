package com.omar.pcconnector.ui.fab

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omar.pcconnector.drawAnimatedBorder


data class FabAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun MultiItemFab(
    icon: ImageVector,
    expanded: Boolean = false,
    onClicked: () -> Unit,
    items: List<FabAction>,
    showRainbowBorder: Boolean = false,
    onDismissRequest: () -> Unit
) {

    BackHandler(expanded, onDismissRequest)

    val iconRotation: Float by animateFloatAsState(targetValue = if (expanded) 45.0f else 0.0f)

    val modifier = if (showRainbowBorder) Modifier.drawAnimatedBorder(
        3.dp,
        RoundedCornerShape(16.dp),
        listOf(Color(0xffc31432), Color(0xFF240b36)),
        durationMillis = 500
        )
    else Modifier

    Column(horizontalAlignment = Alignment.End) {
        if (expanded)
            items.forEach {
                FabItem(modifier = Modifier, it.label, it.icon, it.onClick, true)
            }

        Spacer(Modifier.height(8.dp))
        FloatingActionButton(modifier = modifier, onClick = onClicked) {
            val iconModifier = if (icon in arrayOf(Icons.Rounded.Add, Icons.Filled.Add, Icons.Outlined.Add))
                Modifier.rotate(iconRotation)
            else
                Modifier
            Icon(modifier = iconModifier, imageVector = icon, contentDescription = "FAB")
        }
    }


}


@Composable
fun FabItem(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    onClicked: () -> Unit,
    isVisible: Boolean = false
) {

    val scale: Float by animateFloatAsState(
        targetValue = if (isVisible) 1.0f else 0.0f,
        animationSpec = TweenSpec(200, 0)
    )
    val translateOffset: Dp by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 24.dp,
        animationSpec = TweenSpec(200, 0)
    )

    Row(
        modifier = modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.scale(scale), onClick = onClicked, shape = RoundedCornerShape(8.dp), shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(modifier = Modifier
                .offset(x = translateOffset)
                .padding(8.dp), text = label, fontSize = 14.sp)
        }
        SmallFloatingActionButton(onClick = onClicked, modifier = Modifier.scale(scale)) {
            Icon(imageVector = icon, contentDescription = "icon")
        }
    }
}