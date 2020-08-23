package com.app.codebuzz.flipquotes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public class SlidePageAdapter extends PagerAdapter {

    private Context context;
    private int[] imageImageView = {
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3,
            R.drawable.img4,
            R.drawable.img5,
            R.drawable.img6,
            R.drawable.img7,
            R.drawable.img9,
            R.drawable.img8
    };

    private String[] subHeadingTextView;
    private String[] contentTextView;
    private String[] readMoreTextView;

    SlidePageAdapter(Context context) {
        this.context = context;
        //trying the experimentation
/*        final String TAG = "DocSnippets";
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("quotes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                ArrayList<String> quote = new ArrayList<String>();
                                ArrayList<String> fullquote = new ArrayList<String>();
                                quote.add(document.get("quote")+"");

                                fullquote.add(document.get("quote")+"~"+document.get("author"));

                                //Working code to fetch the quotes and respective author
                                Log.d(TAG, document.getId() + " => " + "Quote: "+ document.get("quote")+" by Author: " + document.get("author"));


                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });*/
        //end
        subHeadingTextView = context.getResources().getStringArray(R.array.subHeading);

        contentTextView = context.getResources().getStringArray(R.array.content);
        readMoreTextView = context.getResources().getStringArray(R.array.readMore);

    }


    @Override
    public int getCount() {
        return imageImageView.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
//Working Code
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = Objects.requireNonNull(layoutInflater).inflate(R.layout.card_layout, container, false);

        final ImageView image = view.findViewById(R.id.image);
        final ImageView readMoreImage = view.findViewById(R.id.read_more_image);
        final TextView heading = view.findViewById(R.id.subheading);
        final TextView content = view.findViewById(R.id.content);
        final TextView readMore = view.findViewById(R.id.read_more);

        final RelativeLayout footer1 = view.findViewById(R.id.footer1);
        final RelativeLayout footer2 = view.findViewById(R.id.footer2);
        final RelativeLayout header = view.findViewById(R.id.header);

        final ImageView like = view.findViewById(R.id.like);
        final TextView like_count = view.findViewById(R.id.like_count);
        final ImageView share = view.findViewById(R.id.share);
        //created refresh button
        final ImageView refresh = view.findViewById(R.id.refresh);
        final ImageView bookmark = view.findViewById(R.id.bookmark);

        image.setImageResource(imageImageView[position]);

        Drawable drawable = view.getResources().getDrawable(imageImageView[position]);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap blurredBitmap = BlurBuilder.blur(context, bitmap);
        readMoreImage.setImageBitmap(blurredBitmap);
        readMoreImage.setColorFilter(0x76AAAAAA, PorterDuff.Mode.MULTIPLY);

        heading.setText(subHeadingTextView[position]);
        content.setText(contentTextView[position]);
        readMore.setText(readMoreTextView[position]);





        container.addView(view);
//working code end
//experimentation

//experimentation end



        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (footer1.getVisibility() == View.INVISIBLE) {
                    footer1.setVisibility(View.VISIBLE);
                    footer2.setVisibility(View.INVISIBLE);
                    header.setVisibility(View.VISIBLE);
                } else {
                    footer1.setVisibility(View.INVISIBLE);
                    footer2.setVisibility(View.VISIBLE);
                    header.setVisibility(View.INVISIBLE);
                }

            }
        });


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (footer1.getVisibility() == View.INVISIBLE) {
                    footer1.setVisibility(View.VISIBLE);
                    footer2.setVisibility(View.INVISIBLE);
                    header.setVisibility(View.VISIBLE);
                } else {
                    footer1.setVisibility(View.INVISIBLE);
                    footer2.setVisibility(View.VISIBLE);
                    header.setVisibility(View.INVISIBLE);
                }

            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String tag = String.valueOf(like.getTag());

                if (tag.equals("like_outline")) {
                    like.setImageResource(R.drawable.thumb_up);
                    like_count.setText("1");
                    like.setTag("like");
                } else if (tag.equals("like")) {
                    like.setImageResource(R.drawable.thumb_up_outline);
                    like_count.setText("");
                    like.setTag("like_outline");
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subHeadingTextView[position]);
                intent.putExtra(android.content.Intent.EXTRA_TEXT, contentTextView[position]+" by: @FlipQuotes");
                intent.putExtra(android.content.Intent.EXTRA_TITLE,"Share with your friends:");

                context.startActivity(intent);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){



            }


        });
        heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (heading.getCurrentTextColor() == view.getResources().getColor(R.color.subHeadingColor)) {
                    bookmark.setImageResource(R.drawable.bookmark);
                    Toast.makeText(context, "Quote Bookmarked", Toast.LENGTH_SHORT).show();
                    heading.setTextColor(view.getResources().getColor(R.color.bookmarkText));
                    bookmark.setTag("bookmark");
                } else {
                    bookmark.setImageResource(R.drawable.bookmark_outline);
                    Toast.makeText(context, "Bookmark Removed", Toast.LENGTH_SHORT).show();
                    heading.setTextColor(view.getResources().getColor(R.color.subHeadingColor));
                    bookmark.setTag("bookmark_outline");
                }
            }
        });

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String tag = String.valueOf(bookmark.getTag());

                if (tag.equals("bookmark_outline")) {
                    bookmark.setImageResource(R.drawable.bookmark);
                    Toast.makeText(context, "Quote Bookmarked", Toast.LENGTH_SHORT).show();
                    heading.setTextColor(view.getResources().getColor(R.color.bookmarkText));
                    bookmark.setTag("bookmark");
                } else if (tag.equals("bookmark")) {
                    bookmark.setImageResource(R.drawable.bookmark_outline);
                    Toast.makeText(context, "Bookmark Removed", Toast.LENGTH_SHORT).show();
                    heading.setTextColor(view.getResources().getColor(R.color.subHeadingColor));
                    bookmark.setTag("bookmark_outline");
                }
            }
        });

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((FrameLayout) object);
    }


}
