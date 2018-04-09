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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.eftimoff.viewpagertransformers.StackTransformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private String searchedTopic, newsLanguage, newsCountry;
    private List<Article> articles; //can be changed from searchedArticles to bookmarkedArticles
    private List<Article> bookmarkedArticles;
    private List<NewsSource> sources;
    private boolean pagerSetting; //true: searchedArticles, false: bookmarkedArticles
    private int articleRefreshState; //0 = havent refreshed for more, 1 = add 10, etc. all the way to 8 for all 100 articles loaded.
    private static final int DEFAULT_ARTICLENUM_LOAD = 20;

    private TextView totalBitesView;
    private FloatingActionButton biteSearchButton;
    private FloatingActionButton appSettingsButton;
    private FloatingActionButton bookmarkToggleButton;

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
        pagerSetting = true;
        articleRefreshState = 0;

        totalBitesView = (TextView) findViewById(R.id.textView_totalBites);
        biteSearchButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_biteSearch);
        appSettingsButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_appSettings);
        bookmarkToggleButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_showBookmarks);

        setButtons();

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            boolean lastPageChange = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (pagerSetting) {
                    int lastIdx = mPagerAdapter.getCount() - 1;

                    int curItem = mPager.getCurrentItem();
                    if (curItem == lastIdx && state == 1) {
                        lastPageChange = true;

                        if (articleRefreshState != 8) {
                            final AlertDialog refreshDialog = new AlertDialog.Builder(MainActivity.this).create();
                            refreshDialog.setTitle("Refresh for more articles?");

                            refreshDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            articleRefreshState += 1;
                                            searchForTopic(searchedTopic, articleRefreshState);
                                        }
                                    });
                            refreshDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "No",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            refreshDialog.dismiss();
                                        }
                                    });

                            refreshDialog.show();
                        } else {
                            Toast.makeText(MainActivity.this, "Cannot load any more articles!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        lastPageChange = false;
                    }
                }
            }
        });
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ParallaxPageTransformer());

        searchedTopic = "trump"; //holder topic
        newsLanguage = "en"; //holder language
        newsCountry = "us"; //holder country

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NewsAPI.base_Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(NewsAPI.class);

        updateArticles(pagerSetting);
    }

    private void setButtons() {
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
                                searchForTopic(searchedTopic, articleRefreshState);
                            }
                        });

                searchDialog.show();
            }
        });

        appSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog settingsDialog = new AlertDialog.Builder(MainActivity.this).create();
                LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
                View theView = inflater.inflate(R.layout.dialog_settings, null);
                theView.setBackgroundColor(getResources().getColor(R.color.colorDialog));
                settingsDialog.setView(theView);
                settingsDialog.setTitle("News Source Settings");

                final String[] chosenLanguage = new String[1];
                final String[] chosenCountry = new String[1];

                Spinner languageSpinner = (Spinner) theView.findViewById(R.id.spinner_languageSettings);
                ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.language_array, android.R.layout.simple_spinner_dropdown_item);
                languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                languageSpinner.setAdapter(languageAdapter);

                languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        switch (pos) {
                            case 0:
                                chosenLanguage[0] = "en";
                                break;
                            case 1:
                                chosenLanguage[0] = "es";
                                break;
                            default:
                                chosenLanguage[0] = "en";
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                Spinner countrySpinner = (Spinner) theView.findViewById(R.id.spinner_countrySettings);
                ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.country_array, android.R.layout.simple_spinner_dropdown_item);
                countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(countryAdapter);

                countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        switch (pos) {
                            case 0:
                                chosenCountry[0] = "us";
                                break;
                            case 1:
                                chosenCountry[0] = "mx";
                                break;
                            default:
                                chosenCountry[0] = "us";
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                settingsDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                newsLanguage = chosenLanguage[0];
                                newsCountry = chosenCountry[0];

                                searchForTopic(searchedTopic, 0);
                            }
                        });

                settingsDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Reset",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                newsLanguage = null;
                                newsCountry = null;

                                searchForTopic(searchedTopic, 0);
                            }
                        });

                settingsDialog.show();
            }
        });

        bookmarkToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pagerSetting) {
                    pagerSetting = false;
                    bookmarkToggleButton.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                    biteSearchButton.setImageResource(R.drawable.ic_clear_all_black_24dp);
                    biteSearchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final AlertDialog clearBookmarksDialog = new AlertDialog.Builder(MainActivity.this).create();
                            clearBookmarksDialog.setTitle("Clear all bookmarks?");

                            clearBookmarksDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            bookmarkedArticles.clear();
                                            pagerSetting = true;
                                            bookmarkToggleButton.setImageResource(R.drawable.ic_bookmark_black_24dp);
                                            biteSearchButton.setImageResource(R.drawable.ic_navigation_black_24dp);
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
                                                                    searchForTopic(searchedTopic, articleRefreshState);
                                                                }
                                                            });

                                                    searchDialog.show();
                                                }
                                            });
                                        }
                                    });
                            clearBookmarksDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "No",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            clearBookmarksDialog.dismiss();
                                        }
                                    });

                            clearBookmarksDialog.show();
                        }
                    });
                } else {
                    pagerSetting = true; bookmarkToggleButton.setImageResource(R.drawable.ic_bookmark_black_24dp);
                    biteSearchButton.setImageResource(R.drawable.ic_navigation_black_24dp);
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
                                            searchForTopic(searchedTopic, articleRefreshState);
                                        }
                                    });

                            searchDialog.show();
                        }
                    });}
                updateArticles(pagerSetting);
            }
        });

    }

    private void updateArticles(boolean pagerForMainList) {

//        Log.d(TAG, "updateArticles: " + newsLanguage + ", " + newsCountry );

//        try {
//            bookmarkedArticles = Utility.readList(this.getApplicationContext(), "bookmarks");
//            newsLanguage = Utility.readString(this.getApplicationContext(), "languageSetting");
//            newsCountry = Utility.readString(this.getApplicationContext(), "countrySetting");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        searchForSources(newsLanguage, newsCountry);
        while (sources == null) {
            Log.d(TAG, "setup: WAITING FOR SOURCES");
        }

        if (pagerForMainList) { searchForTopic(searchedTopic, articleRefreshState); }
        else { articles = bookmarkedArticles; updateWidgets(); }
    }

    private void searchForTopic(String searchedTopic, int articleRefreshState) {
        if (articleRefreshState == 0) { articleListCall = api.getArticleList(searchedTopic, KeySettings.API_KEY, DEFAULT_ARTICLENUM_LOAD); }
        else { articleListCall = api.getArticleList(searchedTopic, KeySettings.API_KEY, DEFAULT_ARTICLENUM_LOAD + (articleRefreshState * 10)); }
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
        if (pagerSetting) { totalBitesView.setText(articles.size() + " bites about '" + searchedTopic + "'"); }
        else { totalBitesView.setText(articles.size() + " saved bites");}

        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(0);
    }

    public void bookmarkArticle(Article articleToSave) {
        boolean found = false;
        for (Article article : bookmarkedArticles) {
            if (articleToSave.equals(article)) { found = true; }
        }
        if (!found) {
            bookmarkedArticles.add(articleToSave);
            try {
                Utility.saveList(this.getApplicationContext(), "bookmarks", bookmarkedArticles);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { Toast.makeText(this, "This article has already been saved.", Toast.LENGTH_LONG).show(); }

        //Log.d(TAG, "bookmarkArticle: " + bookmarkedArticles.toString());
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

    @Override
    protected void onPause() {
        super.onPause();

        try {
            Utility.saveList(this.getApplicationContext(), "bookmarks", bookmarkedArticles);
            Utility.saveString(this.getApplicationContext(), "languageSetting", newsLanguage);
            Utility.saveString(this.getApplicationContext(), "countrySetting", newsCountry);
            Utility.saveBoolean(this.getApplicationContext(), "pagerSetting", pagerSetting);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            bookmarkedArticles = Utility.readList(this.getApplicationContext(), "bookmarks");
            newsLanguage = Utility.readString(this.getApplicationContext(), "languageSetting");
            newsCountry = Utility.readString(this.getApplicationContext(), "countrySetting");
            pagerSetting = Utility.readBool(this.getApplicationContext(), "pagerSetting");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        pagerSetting = true;
        searchedTopic = null;
    }
}
