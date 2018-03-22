package com.example.mzdoes.bites;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private TextView totalBitesView;

    private Call<ArticleList> articleListCall;
    private Call<SourceList> sourceListCall;
    private NewsAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    private void setup() {
        totalBitesView = (TextView) findViewById(R.id.textView_totalBites);
        articles = new ArrayList<>();
        sources = new ArrayList<>();

        searchedTopic = "trump"; //holder topic
        newsLanguage = "en"; //holder language
        newsCountry = "us"; //holder country

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NewsAPI.base_Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(NewsAPI.class);

        articleListCall = api.getArticleList(searchedTopic, KeySettings.API_KEY, 100);
        sourceListCall = api.getSourceList(newsLanguage, newsCountry, KeySettings.API_KEY);

        enqueueSourceCall();
        enqueueArticleCall();
    }

    private void enqueueSourceCall() {
        sourceListCall.enqueue(new Callback<SourceList>() {
            @Override
            public void onResponse(Call<SourceList> call, Response<SourceList> response) {
                SourceList tempSourceResponse = response.body();
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

                articles.clear();

                for (Article article : tempArticleList) {
                    for (NewsSource source : sources) {
                        if (!(source.getId().equals(article.getSource().getId()) && (source.getCountry().equals(newsCountry)) && (source.getLanguage().equals(newsLanguage)))) {
                            tempArticleList.remove(article);
                        }
                    }
                }

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

        //adapter.notifyDataSetChanged();
    }
}
