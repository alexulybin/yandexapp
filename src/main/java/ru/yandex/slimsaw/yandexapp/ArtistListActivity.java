package ru.yandex.slimsaw.yandexapp;

import android.support.v4.app.Fragment;
import android.view.Menu;

public class ArtistListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ArtistListFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_list, menu);
        return super.onCreateOptionsMenu(menu);
    }
}