package com.example.mzdoes.bites;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ArticleFragment extends Fragment {

    private String headline, description, urlImage, urlArticle;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_article, container, false);
        TextView headlineView = (TextView) view.findViewById(R.id.textView_fragmentTitle);
        TextView descView = (TextView) view.findViewById(R.id.textView_fragmentDescription);

        headlineView.setText(headline);
        descView.setText(description);

        Picasso.with(getContext()).load(urlImage).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
                drawable.setAlpha(180);
                view.setBackground(drawable);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = urlArticle;
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headline = getArguments().getString("headlineString");
        description = getArguments().getString("descString");
        urlImage = getArguments().getString("urlImageString");
        urlArticle = getArguments().getString("urlArticleString");
    }

    public static ArticleFragment newInstance(Article article) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString("headlineString", article.getTitle());
        args.putString("descString", article.getDescription());
        args.putString("urlImageString", article.getUrlToImage());
        args.putString("urlArticleString", article.getUrl());
        fragment.setArguments(args);
        return fragment;
    }
}

