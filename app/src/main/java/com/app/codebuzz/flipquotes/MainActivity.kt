package com.app.codebuzz.flipquotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var viewPager: VerticalViewPager? = null
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.card_view)
        viewPager?.setAdapter(SlidePageAdapter(this))
    }
}

