package com.app.codebuzz.flipquotes

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Collections
import java.util.Objects
import androidx.core.graphics.createBitmap

class SlidePageAdapter internal constructor(private val context: Context) : PagerAdapter() {
    private var quotes: List<Quote?>? = null

    init {
        fetchQuotesFromUrl()
    }

    private fun fetchQuotesFromUrl() {
        val client = OkHttpClient()
        val request = Request.Builder().url(QUOTES_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SlidePageAdapter", "Error fetching quotes", e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    val json = response.body!!.string()
                    val gson = Gson()
                    val listType = object : TypeToken<List<Quote?>?>() {}.type
                    quotes = gson.fromJson(json, listType)

                    // Shuffle the quotes list
                    Collections.shuffle(quotes)

                    // Notify the adapter on the main thread
                    Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
                }
            }
        })
    }

    override fun getCount(): Int {
        return if (quotes != null) quotes!!.size else 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view =
            Objects.requireNonNull(layoutInflater).inflate(R.layout.card_layout, container, false)

        val heading = view.findViewById<TextView>(R.id.moreAt)
        val content = view.findViewById<TextView>(R.id.content)
        val refreshButton = view.findViewById<Button>(R.id.r_button)
        val shareButton = view.findViewById<Button>(R.id.share_button)

        if (quotes != null && !quotes!!.isEmpty()) {
            val quote = quotes!![position]
            content.text = quote!!.quote
            heading.text = String.format("~ %s", quote.author)
        }

        view.setOnClickListener { v: View? ->
            shareButton.visibility =
                View.VISIBLE
        }

        refreshButton.setOnClickListener { v: View? ->
            refreshButton.visibility = View.GONE
            (container as VerticalViewPager).setCurrentItem(0, true)
        }

        shareButton.setOnClickListener { v: View? ->
            val bitmap = getBitmapFromView(view)
            shareImage(bitmap)
        }

        container.addView(view)
        return view
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val refreshButton = view.findViewById<Button>(R.id.r_button)
        val shareButton = view.findViewById<Button>(R.id.share_button)
        val originalVisibility = refreshButton.visibility
        val originalVisibilityShare = shareButton.visibility
        refreshButton.visibility = View.GONE
        shareButton.visibility = View.GONE

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)


        refreshButton.visibility = originalVisibility
        shareButton.visibility = originalVisibilityShare
        return bitmap
    }

    private fun shareImage(bitmap: Bitmap) {
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Quote", null)
        val uri = Uri.parse(path)

        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, "Share Quote"))
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    companion object {
        private const val QUOTES_URL =
            "https://raw.githubusercontent.com/Amrish-Sharma/fq_quotes/refs/heads/main/Quotes.json" // Replace with your URL
    }
}