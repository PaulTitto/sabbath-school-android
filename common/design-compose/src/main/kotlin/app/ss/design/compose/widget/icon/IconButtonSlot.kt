/*
 * Copyright (c) 2025. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package app.ss.design.compose.widget.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

/**
 * An IconButton [IconSlot]
 */
@Immutable
data class IconButtonSlot(
    val imageVector: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val tint: Color? = null
) : IconSlot {

    @Composable
    override fun Content(contentColor: Color, modifier: Modifier) {
        androidx.compose.material3.IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.wrapContentSize()
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint ?: contentColor,
                modifier = modifier
            )
        }
    }
}

/**
 * An IconButton [IconSlot]
 */
@Immutable
data class IconButtonResSlot(
    @param:DrawableRes val iconRes: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val tint: Color? = null
) : IconSlot {

    @Composable
    override fun Content(contentColor: Color, modifier: Modifier) {
        androidx.compose.material3.IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.wrapContentSize()
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = tint ?: contentColor,
                modifier = modifier
            )
        }
    }
}
