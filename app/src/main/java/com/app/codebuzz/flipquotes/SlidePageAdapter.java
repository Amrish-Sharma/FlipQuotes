package com.app.codebuzz.flipquotes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;


import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        final TextView heading = view.findViewById(R.id.moreAt);
        final TextView content = view.findViewById(R.id.content);
        final TextView readMore = view.findViewById(R.id.read_more);
        final Button refreshButton= view.findViewById(R.id.r_button);
        final Button shareButton = view.findViewById(R.id.share_button);

        Quote quote = quotes.get(position);
        content.setText(quote.getQuote());
        heading.setText(String.format("~ %s", quote.getAuthor()));
        //heading.setText("Read More");
//        view.setOnClickListener(v -> refreshButton.setVisibility(View.VISIBLE));

        view.setOnClickListener(v -> shareButton.setVisibility(View.VISIBLE));

        refreshButton.setOnClickListener(v -> {
            refreshButton.setVisibility(View.GONE);
            ((VerticalViewPager) container).setCurrentItem(0, true);
        });

        shareButton.setOnClickListener(v -> {
            Bitmap bitmap = getBitmapFromView(view);
            shareImage(bitmap);
        });

        container.addView(view);
        return view;
    }

    private Bitmap getBitmapFromView(View view) {
        Button refreshButton = view.findViewById(R.id.r_button);
        Button shareButton = view.findViewById(R.id.share_button);
        int originalVisibility = refreshButton.getVisibility();
        int originalVisibilityShare = shareButton.getVisibility();
        refreshButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        refreshButton.setVisibility(originalVisibility);
        shareButton.setVisibility(originalVisibilityShare);
        return bitmap;
    }

    private void shareImage(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Quote", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, "Share Quote"));
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


}
