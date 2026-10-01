package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

@Composable
fun PlushieImage(
    uri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    filterName: String = "Original",
    brightness: Float = 0f,
    contrast: Float = 1f,
    contentDescription: String? = "Plushie Dog"
) {
    // Construct color matrix for filter + brightness + contrast
    val matrix = ColorMatrix()

    // Apply filter presets
    when (filterName) {
        "Cozy Warm" -> {
            // Warm sepia-amber tint
            matrix.setToSaturation(1.1f)
            val cozy = ColorMatrix(floatArrayOf(
                1.15f, 0f, 0f, 0f, 15f,
                0f, 1.05f, 0f, 0f, 8f,
                0f, 0f, 0.90f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.timesAssign(cozy)
        }
        "Golden Glow" -> {
            // Golden sunlight tint
            matrix.setToSaturation(1.2f)
            val golden = ColorMatrix(floatArrayOf(
                1.2f, 0f, 0f, 0f, 20f,
                0f, 1.15f, 0f, 0f, 12f,
                0f, 0f, 0.85f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.timesAssign(golden)
        }
        "Pastel Fluff" -> {
            // Soft dreamy pastel
            matrix.setToSaturation(0.85f)
            val pastel = ColorMatrix(floatArrayOf(
                1.05f, 0f, 0f, 0f, 25f,
                0f, 1.05f, 0f, 0f, 25f,
                0f, 0f, 1.1f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.timesAssign(pastel)
        }
        "Vintage Soft" -> {
            // Retro gentle warmth
            matrix.setToSaturation(0.75f)
            val vintage = ColorMatrix(floatArrayOf(
                1.1f, 0.05f, 0.05f, 0f, 10f,
                0.05f, 1.0f, 0.05f, 0f, 5f,
                0.02f, 0.02f, 0.85f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.timesAssign(vintage)
        }
        "Calming Sepia" -> {
            // Earthy grounding sepia
            val sepia = ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.timesAssign(sepia)
        }
        else -> {
            // Original
            matrix.setToSaturation(1f)
        }
    }

    // Apply brightness (shift) and contrast (scaling)
    if (brightness != 0f || contrast != 1f) {
        val t = (1f - contrast) / 2f * 255f + (brightness * 255f)
        val adj = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, t,
            0f, contrast, 0f, 0f, t,
            0f, 0f, contrast, 0f, t,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.timesAssign(adj)
    }

    val colorFilter = ColorFilter.colorMatrix(matrix)

    if (uri.startsWith("drawable://")) {
        val drawableName = uri.removePrefix("drawable://")
        val resId = when (drawableName) {
            "plushie_corgi_friend" -> R.drawable.plushie_corgi_friend
            "plushie_hero_dog" -> R.drawable.plushie_hero_dog
            else -> R.drawable.ic_plush_dog_icon
        }
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .error(R.drawable.plushie_hero_dog)
                .placeholder(R.drawable.plushie_hero_dog)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter
        )
    }
}
