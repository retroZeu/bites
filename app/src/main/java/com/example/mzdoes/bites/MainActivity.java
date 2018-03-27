package com.example.mzdoes.bites;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private String searchedTopic, newsLanguage, newsCountry;
    private List<Article> articles;
    private List<NewsSource> sources;

    private List<Article> bookmarkedArticles;

    private TextView totalBitesView;
    private FloatingActionButton biteSearchButton;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;


    private Call<ArticleList> articleListCall;
    private Call<SourceList> sourceListCall;
    private NewsAPI api;

    public static final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    private void setup() {
        articles = new ArrayList<>();
        sources = new ArrayList<>();
        bookmarkedArticles = new ArrayList<>();

        totalBitesView = (TextView) findViewById(R.id.textView_totalBites);
        biteSearchButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_biteSearch);
        setBiteSearchButton();

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        searchedTopic = "trump"; //holder topic
        newsLanguage = "en"; //holder language
        newsCountry = "us"; //holder country

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NewsAPI.base_Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(NewsAPI.class);

        searchForSources(newsLanguage, newsCountry);
        while (sources == null) {
            Log.d(TAG, "setup: WAITING FOR SOURCES");
        }
        searchForTopic(searchedTopic);
    }

    private void setBiteSearchButton() {
        biteSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog searchDialog = new AlertDialog.Builder(MainActivity.this).create();
                LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_search, null);
                theView.setBackgroundColor(getResources().getColor(R.color.colorDialog));
                searchDialog.setView(theView);
                searchDialog.setTitle(null);

                final EditText searchEditText = theView.findViewById(R.id.editText_searchTopic);

                searchDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Search",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                searchedTopic = searchEditText.getText().toString();
                                searchForTopic(searchedTopic);
                            }
                        });

                searchDialog.show();
            }
        });
    }

    private void searchForTopic(String searchedTopic) {
        articleListCall = api.getArticleList(searchedTopic, KeySettings.API_KEY, 100);
        enqueueArticleCall();
    }

    private void searchForSources(String language, String country) {
        sourceListCall = api.getSourceList(language, country, KeySettings.API_KEY);
        enqueueSourceCall();
    }

    private void enqueueSourceCall() {
        sourceListCall.enqueue(new Callback<SourceList>() {
            @Override
            public void onResponse(Call<SourceList> call, Response<SourceList> response) {
                SourceList tempSourceResponse = response.body();
//                Log.d(TAG, "TEMP SOURCE RESPONSE: " + tempSourceResponse.toString());
//                Log.d(TAG, "RESPONSE URL: " + call.request().url());
                sources.clear();
                sources.addAll(tempSourceResponse.getSourceList());
            }

            @Override
            public void onFailure(Call<SourceList> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    private void enqueueArticleCall() {
        articleListCall.enqueue(new Callback<ArticleList>() {
            @Override
            public void onResponse(Call<ArticleList> call, Response<ArticleList> response) {
                ArticleList tempArticleResponse = response.body();
                List<Article> tempArticleList = tempArticleResponse.getArticles();
                List<Article> articlesToRemove = new ArrayList<>();

//                Log.d(TAG, "ALL SOURCES: " + sources.toString());

                boolean found = false;
                for (Article article : tempArticleList) {
//                    Log.d(TAG, "ARTICLE ID: " + article.getSource().getId().toString() );
                    for (NewsSource source : sources) {
                        if ((article.getSource().getId().toString()).equals(source.getId().toString())) { found = true; }
                    }
                    if (!found) { articlesToRemove.add(article); }
                    found = false;
                }
                tempArticleList.removeAll(articlesToRemove);

                articles.clear();
                articles.addAll(tempArticleList);
                updateWidgets();
            }

            @Override
            public void onFailure(Call<ArticleList> call, Throwable t) {
                Log.d("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    private void updateWidgets() {
        totalBitesView.setText(articles.size() + " bites about '" + searchedTopic + "'");

        mPagerAdapter.notifyDataSetChanged();
    }

    public void bookmarkArticle(Article articleToSave) {
        boolean found = false;
        for (Article article : bookmarkedArticles) {
            if (articleToSave.equals(article)) { found = true; }
        }
        if (!found) { bookmarkedArticles.add(articleToSave); }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleFragment.newInstance(articles.get(position));
        }

        @Override
        public int getCount() {
            return articles.size();
        }
    }
}
