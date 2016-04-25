package ru.yandex.slimsaw.yandexapp;

import android.support.v4.app.Fragment;
import android.view.Menu;

public class ArtistDetailsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        String artistId = (String)getIntent().getSerializableExtra(ArtistDetailsFragment.EXTRA_ARTIST_DESC);
        //при создании ArtistDetailsFragment передаем id исполнителя, полученный из дополнения
        return ArtistDetailsFragment.newInstance(artistId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_details, menu);
        return true;
    }
}