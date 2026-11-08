package com.evilcorp.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.evilcorp.android.model.Area

enum class IconKind {
    Home, Grid, Checklist, Chart, More,
    Shield, Key, Database, Terminal, Network, Lock, Unlock,
    Code, Phone, Folder, Eye, Flag, Lightbulb, Search, Check,
    ChevronLeft, ChevronRight, Close, Bookmark, Copy, Trash, Clock, Flask
}

fun challengeIcon(id: String, area: Area): IconKind = when (id) {
    "prefs", "backup", "external", "pathtraversal" -> IconKind.Folder
    "sqli", "httpcache", "serialization" -> IconKind.Database
    "secrets", "keystore", "pin", "jwt", "stepup" -> IconKind.Key
    "logging", "antidebug", "tampering", "nativebridge" -> IconKind.Terminal
    "crypto", "random", "ivreuse" -> IconKind.Lock
    "webview", "webviewfile", "deeplink", "intentredirect", "dexload" -> IconKind.Code
    "provider", "broadcast", "pendingintent", "strandhogg" -> IconKind.Flask
    "root", "biometric", "emulator", "repackaging" -> IconKind.Shield
    "cleartext", "sslpinning", "trustmanager" -> IconKind.Network
    "clipboard", "keyboard", "memory" -> IconKind.Copy
    "screenshot", "tapjacking", "tracking", "notifications" -> IconKind.Eye
    else -> when (area) {
        Area.Storage -> IconKind.Folder
        Area.Crypto -> IconKind.Lock
        Area.Network -> IconKind.Network
        Area.Auth -> IconKind.Key
        Area.Platform -> IconKind.Code
        Area.Code -> IconKind.Terminal
        Area.Resilience -> IconKind.Shield
        Area.Privacy -> IconKind.Eye
    }
}

@Composable
fun EvilIcon(
    kind: IconKind,
    tint: Color = Color.White,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.095f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (kind) {
            IconKind.Home -> {
                val p = Path().apply {
                    moveTo(w * 0.14f, h * 0.48f)
                    lineTo(w * 0.50f, h * 0.16f)
                    lineTo(w * 0.86f, h * 0.48f)
                    moveTo(w * 0.24f, h * 0.44f)
                    lineTo(w * 0.24f, h * 0.84f)
                    lineTo(w * 0.76f, h * 0.84f)
                    lineTo(w * 0.76f, h * 0.44f)
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.Grid -> {
                val s = Size(w * 0.30f, h * 0.30f)
                val r = CornerRadius(w * 0.06f, w * 0.06f)
                drawRoundRect(tint, Offset(w * 0.16f, h * 0.16f), s, r, style = stroke)
                drawRoundRect(tint, Offset(w * 0.54f, h * 0.16f), s, r, style = stroke)
                drawRoundRect(tint, Offset(w * 0.16f, h * 0.54f), s, r, style = stroke)
                drawRoundRect(tint, Offset(w * 0.54f, h * 0.54f), s, r, style = stroke)
            }
            IconKind.Checklist -> {
                drawCircle(tint, w * 0.07f, Offset(w * 0.22f, h * 0.28f))
                drawLine(tint, Offset(w * 0.38f, h * 0.28f), Offset(w * 0.84f, h * 0.28f), stroke.width, StrokeCap.Round)
                drawCircle(tint, w * 0.07f, Offset(w * 0.22f, h * 0.52f))
                drawLine(tint, Offset(w * 0.38f, h * 0.52f), Offset(w * 0.84f, h * 0.52f), stroke.width, StrokeCap.Round)
                drawCircle(tint, w * 0.07f, Offset(w * 0.22f, h * 0.76f))
                drawLine(tint, Offset(w * 0.38f, h * 0.76f), Offset(w * 0.72f, h * 0.76f), stroke.width, StrokeCap.Round)
            }
            IconKind.Chart -> {
                val r = CornerRadius(w * 0.04f, w * 0.04f)
                drawRoundRect(tint, Offset(w * 0.16f, h * 0.52f), Size(w * 0.16f, h * 0.32f), r)
                drawRoundRect(tint, Offset(w * 0.42f, h * 0.32f), Size(w * 0.16f, h * 0.52f), r)
                drawRoundRect(tint, Offset(w * 0.68f, h * 0.18f), Size(w * 0.16f, h * 0.66f), r)
            }
            IconKind.More -> {
                drawCircle(tint, w * 0.38f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, w * 0.055f, Offset(w * 0.33f, h * 0.5f))
                drawCircle(tint, w * 0.055f, Offset(w * 0.50f, h * 0.5f))
                drawCircle(tint, w * 0.055f, Offset(w * 0.67f, h * 0.5f))
            }
            IconKind.Shield -> {
                val p = Path().apply {
                    moveTo(w * 0.50f, h * 0.14f)
                    lineTo(w * 0.80f, h * 0.26f)
                    lineTo(w * 0.80f, h * 0.52f)
                    cubicTo(w * 0.80f, h * 0.72f, w * 0.50f, h * 0.86f, w * 0.50f, h * 0.86f)
                    cubicTo(w * 0.50f, h * 0.86f, w * 0.20f, h * 0.72f, w * 0.20f, h * 0.52f)
                    lineTo(w * 0.20f, h * 0.26f)
                    close()
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.Key -> {
                drawCircle(tint, w * 0.18f, Offset(w * 0.34f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.52f, h * 0.50f), Offset(w * 0.86f, h * 0.50f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.72f, h * 0.50f), Offset(w * 0.72f, h * 0.66f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.84f, h * 0.50f), Offset(w * 0.84f, h * 0.62f), stroke.width, StrokeCap.Round)
            }
            IconKind.Database -> {
                drawOval(tint, Offset(w * 0.20f, h * 0.16f), Size(w * 0.60f, h * 0.20f), style = stroke)
                drawLine(tint, Offset(w * 0.20f, h * 0.26f), Offset(w * 0.20f, h * 0.74f), stroke.width)
                drawLine(tint, Offset(w * 0.80f, h * 0.26f), Offset(w * 0.80f, h * 0.74f), stroke.width)
                drawArc(tint, 0f, 180f, false, Offset(w * 0.20f, h * 0.42f), Size(w * 0.60f, h * 0.20f), style = stroke)
                drawArc(tint, 0f, 180f, false, Offset(w * 0.20f, h * 0.64f), Size(w * 0.60f, h * 0.20f), style = stroke)
            }
            IconKind.Terminal -> {
                drawRoundRect(tint, Offset(w * 0.14f, h * 0.18f), Size(w * 0.72f, h * 0.64f), CornerRadius(w * 0.08f), style = stroke)
                val p = Path().apply {
                    moveTo(w * 0.28f, h * 0.38f)
                    lineTo(w * 0.42f, h * 0.50f)
                    lineTo(w * 0.28f, h * 0.62f)
                }
                drawPath(p, tint, style = stroke)
                drawLine(tint, Offset(w * 0.48f, h * 0.62f), Offset(w * 0.68f, h * 0.62f), stroke.width, StrokeCap.Round)
            }
            IconKind.Network -> {
                drawCircle(tint, w * 0.36f, Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawOval(tint, Offset(w * 0.32f, h * 0.14f), Size(w * 0.36f, h * 0.72f), style = stroke)
                drawLine(tint, Offset(w * 0.16f, h * 0.50f), Offset(w * 0.84f, h * 0.50f), stroke.width)
            }
            IconKind.Lock -> {
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.44f), Size(w * 0.56f, h * 0.40f), CornerRadius(w * 0.08f), style = stroke)
                drawArc(tint, 180f, 180f, false, Offset(w * 0.32f, h * 0.20f), Size(w * 0.36f, h * 0.44f), style = stroke)
                drawCircle(tint, w * 0.055f, Offset(w * 0.50f, h * 0.64f))
            }
            IconKind.Unlock -> {
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.44f), Size(w * 0.56f, h * 0.40f), CornerRadius(w * 0.08f), style = stroke)
                drawArc(tint, 180f, 140f, false, Offset(w * 0.32f, h * 0.18f), Size(w * 0.36f, h * 0.44f), style = stroke)
            }
            IconKind.Code -> {
                val p = Path().apply {
                    moveTo(w * 0.34f, h * 0.30f)
                    lineTo(w * 0.16f, h * 0.50f)
                    lineTo(w * 0.34f, h * 0.70f)
                    moveTo(w * 0.66f, h * 0.30f)
                    lineTo(w * 0.84f, h * 0.50f)
                    lineTo(w * 0.66f, h * 0.70f)
                }
                drawPath(p, tint, style = stroke)
                drawLine(tint, Offset(w * 0.56f, h * 0.24f), Offset(w * 0.44f, h * 0.76f), stroke.width, StrokeCap.Round)
            }
            IconKind.Phone -> {
                drawRoundRect(tint, Offset(w * 0.28f, h * 0.12f), Size(w * 0.44f, h * 0.76f), CornerRadius(w * 0.08f), style = stroke)
                drawCircle(tint, w * 0.04f, Offset(w * 0.50f, h * 0.78f))
            }
            IconKind.Folder -> {
                val p = Path().apply {
                    moveTo(w * 0.16f, h * 0.26f)
                    lineTo(w * 0.40f, h * 0.26f)
                    lineTo(w * 0.48f, h * 0.36f)
                    lineTo(w * 0.84f, h * 0.36f)
                    lineTo(w * 0.84f, h * 0.78f)
                    lineTo(w * 0.16f, h * 0.78f)
                    close()
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.Eye -> {
                val p = Path().apply {
                    moveTo(w * 0.12f, h * 0.50f)
                    quadraticTo(w * 0.50f, h * 0.18f, w * 0.88f, h * 0.50f)
                    quadraticTo(w * 0.50f, h * 0.82f, w * 0.12f, h * 0.50f)
                }
                drawPath(p, tint, style = stroke)
                drawCircle(tint, w * 0.11f, Offset(w * 0.50f, h * 0.50f))
            }
            IconKind.Flag -> {
                drawLine(tint, Offset(w * 0.24f, h * 0.16f), Offset(w * 0.24f, h * 0.86f), stroke.width, StrokeCap.Round)
                val p = Path().apply {
                    moveTo(w * 0.24f, h * 0.18f)
                    lineTo(w * 0.78f, h * 0.28f)
                    lineTo(w * 0.64f, h * 0.42f)
                    lineTo(w * 0.78f, h * 0.54f)
                    lineTo(w * 0.24f, h * 0.54f)
                    close()
                }
                drawPath(p, tint)
            }
            IconKind.Lightbulb -> {
                drawCircle(tint, w * 0.24f, Offset(w * 0.50f, h * 0.40f), style = stroke)
                drawLine(tint, Offset(w * 0.38f, h * 0.72f), Offset(w * 0.62f, h * 0.72f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.42f, h * 0.84f), Offset(w * 0.58f, h * 0.84f), stroke.width, StrokeCap.Round)
            }
            IconKind.Search -> {
                drawCircle(tint, w * 0.26f, Offset(w * 0.44f, h * 0.44f), style = stroke)
                drawLine(tint, Offset(w * 0.64f, h * 0.64f), Offset(w * 0.84f, h * 0.84f), stroke.width, StrokeCap.Round)
            }
            IconKind.Check -> {
                drawCircle(tint.copy(alpha = 0.20f), w * 0.42f, Offset(w * 0.50f, h * 0.50f))
                drawCircle(tint, w * 0.40f, Offset(w * 0.50f, h * 0.50f), style = stroke)
                val p = Path().apply {
                    moveTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.65f)
                    lineTo(w * 0.70f, h * 0.38f)
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.ChevronLeft -> {
                val p = Path().apply {
                    moveTo(w * 0.62f, h * 0.22f)
                    lineTo(w * 0.34f, h * 0.50f)
                    lineTo(w * 0.62f, h * 0.78f)
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.ChevronRight -> {
                val p = Path().apply {
                    moveTo(w * 0.38f, h * 0.22f)
                    lineTo(w * 0.66f, h * 0.50f)
                    lineTo(w * 0.38f, h * 0.78f)
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.Close -> {
                drawLine(tint, Offset(w * 0.26f, h * 0.26f), Offset(w * 0.74f, h * 0.74f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.74f, h * 0.26f), Offset(w * 0.26f, h * 0.74f), stroke.width, StrokeCap.Round)
            }
            IconKind.Bookmark -> {
                val p = Path().apply {
                    moveTo(w * 0.26f, h * 0.16f)
                    lineTo(w * 0.74f, h * 0.16f)
                    lineTo(w * 0.74f, h * 0.84f)
                    lineTo(w * 0.50f, h * 0.66f)
                    lineTo(w * 0.26f, h * 0.84f)
                    close()
                }
                drawPath(p, tint, style = stroke)
            }
            IconKind.Copy -> {
                drawRoundRect(tint, Offset(w * 0.32f, h * 0.30f), Size(w * 0.50f, h * 0.54f), CornerRadius(w * 0.06f), style = stroke)
                drawRoundRect(tint, Offset(w * 0.18f, h * 0.16f), Size(w * 0.46f, h * 0.50f), CornerRadius(w * 0.06f), style = stroke)
            }
            IconKind.Trash -> {
                drawLine(tint, Offset(w * 0.20f, h * 0.26f), Offset(w * 0.80f, h * 0.26f), stroke.width, StrokeCap.Round)
                drawRoundRect(tint, Offset(w * 0.28f, h * 0.26f), Size(w * 0.44f, h * 0.58f), CornerRadius(w * 0.05f), style = stroke)
            }
            IconKind.Clock -> {
                drawCircle(tint, w * 0.38f, Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(tint, Offset(w * 0.50f, h * 0.30f), Offset(w * 0.50f, h * 0.52f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.52f), Offset(w * 0.66f, h * 0.52f), stroke.width, StrokeCap.Round)
            }
            IconKind.Flask -> {
                val p = Path().apply {
                    moveTo(w * 0.38f, h * 0.16f)
                    lineTo(w * 0.62f, h * 0.16f)
                    moveTo(w * 0.42f, h * 0.16f)
                    lineTo(w * 0.42f, h * 0.40f)
                    lineTo(w * 0.20f, h * 0.82f)
                    lineTo(w * 0.80f, h * 0.82f)
                    lineTo(w * 0.58f, h * 0.40f)
                    lineTo(w * 0.58f, h * 0.16f)
                }
                drawPath(p, tint, style = stroke)
            }
        }
    }
}
