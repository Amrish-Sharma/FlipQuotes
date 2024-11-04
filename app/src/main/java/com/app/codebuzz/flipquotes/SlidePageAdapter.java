package com.app.codebuzz.flipquotes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;



public class SlidePageAdapter extends PagerAdapter {

    private Context context;

    private List<Quote> quotes;

    SlidePageAdapter(Context context) {
        this.context = context;
        this.quotes = loadQuotesFromAssets();
    }

    private List<Quote> loadQuotesFromAssets() {
        AssetManager assetManager = context.getAssets();
        try (InputStream is = assetManager.open("quotes.json")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Quote>>() {}.getType();
            List<Quote> quotes = gson.fromJson(json, listType);

            // Shuffle the quotes list
            Collections.shuffle(quotes);

            return quotes;
        } catch (IOException e) {
            Log.e("SlidePageAdapter", "Error reading quotes.json", e);
            return null;
        }
    }

    @Override
    public int getCount() {
        return quotes != null ? quotes.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = Objects.requireNonNull(layoutInflater).inflate(R.layout.card_layout, container, false);

        final TextView heading = view.findViewById(R.id.subheading);
        final TextView content = view.findViewById(R.id.content);
        final TextView readMore = view.findViewById(R.id.read_more);

        Quote quote = quotes.get(position);
        heading.setText(quote.getAuthor());
        content.setText(quote.getQuote());
        readMore.setText("Read More");

        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


}
