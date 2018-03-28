package com.example.mzdoes.bites;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ArticleFragment extends Fragment {

    private String headline, description, urlImage, urlArticle, sourceName;
    private Article currentArticle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_article, container, false);
        TextView headlineView = (TextView) view.findViewById(R.id.textView_fragmentTitle);
        TextView descView = (TextView) view.findViewById(R.id.textView_fragmentDescription);

        headlineView.setText(headline);
        descView.setText(description);

        Picasso.with(getContext()).load(urlImage).resize(container.getWidth(), container.getHeight()).centerCrop().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BitmapDrawable drawable = new BitmapDrawable(getContext().getResources(), bitmap);
                drawable.setAlpha(135);
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

        this.registerForContextMenu(view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headline = getArguments().getString("headlineString");
        description = getArguments().getString("descString");
        sourceName = getArguments().getString("sourceNameString");
        urlImage = getArguments().getString("urlImageString");
        urlArticle = getArguments().getString("urlArticleString");
        currentArticle = getArguments().getParcelable("currentArticle");
    }

    public static ArticleFragment newInstance(Article article) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString("headlineString", article.getTitle());
        args.putString("sourceNameString", article.getSource().getName());
        args.putString("descString", "BY "+ article.getSource().getName() + ": " + article.getDescription());
        args.putString("urlImageString", article.getUrlToImage());
        args.putString("urlArticleString", article.getUrl());
        args.putParcelable("currentArticle", article);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(this.getContext());
        inflater.inflate(R.menu.menu_articlecontext, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.item_share:
                //share
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, urlArticle);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            case R.id.item_bookmark:
                ((MainActivity) getActivity()).bookmarkArticle(currentArticle);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

}

