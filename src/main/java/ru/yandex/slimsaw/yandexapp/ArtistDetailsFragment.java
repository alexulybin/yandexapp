package ru.yandex.slimsaw.yandexapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by slimsaw on 24.04.2016.
 */
public class ArtistDetailsFragment extends Fragment {
    private static final String TAG = "ArtistDetailsFragment";

    public static final String EXTRA_ARTIST_DESC = "ru.yandex.slimsaw.yandexapp.artist_desc";

    private Artist mArtist;
    private TextView mDescription;
    private ImageView bigCover;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SingleFragmentActivity act = (SingleFragmentActivity)getActivity();
        if(act != null) {
            act.showUpButton();
        }

        setHasOptionsMenu(true);

        //получаем id исполнителя и по нему вытаскиваем самого исполнителя
        String id = (String)getArguments().getSerializable(EXTRA_ARTIST_DESC);

        mArtist = ArtistsStorage.getInstance().getArtist(id);
        getActivity().setTitle(mArtist.getName());
        //mArtist = new Artist();
        //mArtist.setDescription(desc);
        //mArtist.setBigCover("http://cache-default02h.cdn.yandex.net/download.cdn.yandex.net/mobilization-2016/artists-detail.png");

        new LoadBigCoverTask().execute(mArtist.getBigCover());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_artist_details, container, false);

        bigCover = (ImageView)v.findViewById(R.id.artist_details_imageView);
        mDescription = (TextView)v.findViewById(R.id.artistDesc);
        mDescription.setText(mArtist.getDescription());

        TextView genres = (TextView)v.findViewById(R.id.artistGenres);
        TextView albums = (TextView)v.findViewById(R.id.artistAlbums);

        //Скрываем жанры, если нет данных
        if(mArtist.getGenresStr() != null) {
            genres.setText(mArtist.getGenresStr());
        } else {
            genres.setVisibility(View.GONE);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(mArtist.getAlbumsStr());
        if(sb.length() > 0)
            sb.append(", ");
        sb.append(mArtist.getTracksStr());
        albums.setText(sb.toString());

        return v;
    }

    /** Загрузчик больших изображений исполнителей */
    private class LoadBigCoverTask extends AsyncTask<String,Void,Bitmap> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Bitmap doInBackground(String... url) {
            try {
                byte[] bitmapBytes = new ArtistsFetcher().getUrlBytes(url[0]);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                bitmap = stretchImageToScreeWidth(bitmap);

                return bitmap;
            } catch (IOException ioe) {
                Log.e(TAG, "Error downloading big image", ioe);
            }
            return null;
        }

        /** Масштабирование изображения, чтобы его ширина соответствовала ширине экрана */
        private Bitmap stretchImageToScreeWidth(Bitmap bitmap) {
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            Activity act = getActivity();
            if(act != null) {
                DisplayMetrics metrics = act.getResources().getDisplayMetrics();

                int newWidth = metrics.widthPixels;
                float scaleFactor = (float) newWidth / (float) imageWidth;
                int newHeight = (int) (imageHeight * scaleFactor);

                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            return bitmap;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //скрываем индикатор загрузки и задаем bitmap для картинки исполнителя
            Activity act = getActivity();
            if(act != null) {
                act.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
            bigCover.setImageBitmap(bitmap);
        }
    }

    /** Создание экземпляра ArtistDetailsFragment с передачей id исполнителя */
    public static ArtistDetailsFragment newInstance(String artistId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_ARTIST_DESC, artistId);
        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
