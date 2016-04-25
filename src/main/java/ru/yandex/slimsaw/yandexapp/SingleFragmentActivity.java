package ru.yandex.slimsaw.yandexapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public abstract class SingleFragmentActivity extends ActionBarActivity {
    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_single_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if(fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    /** Отобразить Up button на action bar */
    public void showUpButton() {
        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);
    }

    /** Скрыть Up button на action bar */
    public void hideUpButton() {
        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(false);
    }
}