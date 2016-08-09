package com.example.xyzreader.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;
    private Toolbar mToolbar;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        photo = (ImageView) findViewById(R.id.photo);

        getSupportLoaderManager().initLoader(0, null, this);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    ImageLoaderHelper.getInstance(ArticleDetailActivity.this).getImageLoader()
                            .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                    Bitmap bitmap = imageContainer.getBitmap();
                                    if (bitmap != null) {
                                        Palette p = Palette.generate(bitmap, 12);
//                                        mMutedColor = p.getDarkMutedColor(0xFF333333);
                                        photo.setImageBitmap(imageContainer.getBitmap());
                                        changeToolbarColor(bitmap);
//                                        mRootView.findViewById(R.id.meta_bar)
//                                                .setBackgroundColor(mMutedColor);
//                                        updateStatusBar();
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError volleyError) {

                                }
                            });
                }
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    private void changeToolbarColor(Bitmap bitmap) {


        new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrant = palette.getDarkMutedSwatch();
                int mutedColor = vibrant.getRgb();
                if (vibrant != null) {
                    // If we have a vibrant color
                    // update the title TextView
                    collapsingToolbarLayout.setBackgroundColor(mutedColor);
                    //  mutedColor = palette.getMutedColor(R.attr.colorPrimary);
                    collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(mutedColor));
                    collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(mutedColor));
                }
            }

        });
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
//            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
        }
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
//                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            }
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
