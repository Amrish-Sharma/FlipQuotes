package com.app.codebuzz.flipquotes

import android.graphics.Bitmap
import kotlin.math.*
import androidx.core.graphics.createBitmap

fun blurBitmap(bitmap: Bitmap, radius: Int): Bitmap? {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val wm = width - 1
    val hm = height - 1
    val wh = width * height
    val div = radius + radius + 1

    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)

    val vmin = IntArray(max(width, height))

    val dv = IntArray(256 * div)
    for (i in dv.indices) {
        dv[i] = i / div
    }

    var yi = 0
    var yw = 0

    for (y in 0 until height) {
        var rsum = 0
        var gsum = 0
        var bsum = 0
        for (i in -radius..radius) {
            val p = pixels[yi + min(wm, max(i, 0))]
            rsum += (p shr 16) and 0xFF
            gsum += (p shr 8) and 0xFF
            bsum += p and 0xFF
        }

        for (x in 0 until width) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]

            if (y == 0) vmin[x] = min(x + radius + 1, wm)
            val p1 = pixels[yw + vmin[x]]
            val p2 = pixels[yw + max(x - radius, 0)]

            rsum += ((p1 shr 16) and 0xFF) - ((p2 shr 16) and 0xFF)
            gsum += ((p1 shr 8) and 0xFF) - ((p2 shr 8) and 0xFF)
            bsum += (p1 and 0xFF) - (p2 and 0xFF)

            yi++
        }
        yw += width
    }

    for (x in 0 until width) {
        var rsum = 0
        var gsum = 0
        var bsum = 0
        var yp = -radius * width
        for (i in -radius..radius) {
            val yi2 = max(0, yp) + x
            rsum += r[yi2]
            gsum += g[yi2]
            bsum += b[yi2]
            yp += width
        }
        var yi2 = x
        for (y in 0 until height) {
            pixels[yi2] = (0xFF shl 24) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            if (x == 0) vmin[y] = min(y + radius + 1, hm) * width
            val p1 = x + vmin[y]
            val p2 = x + max(y - radius, 0) * width

            rsum += r[p1] - r[p2]
            gsum += g[p1] - g[p2]
            bsum += b[p1] - b[p2]

            yi2 += width
        }
    }

    val blurredBitmap = bitmap.config?.let { createBitmap(width, height, it) }
    blurredBitmap?.setPixels(pixels, 0, width, 0, 0, width, height)
    return blurredBitmap
}
