package ru.yandex.slimsaw.yandexapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class ArtistListFragment extends ListFragment implements AbsListView.OnScrollListener {
    private static final String TAG = "ArtistListFragment";

    ThumbnailDownloader<ImageView> mThumbnailThread;
    ArtistItemAdapter adapter;

    @TargetApi(11)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //не загружаем исполнителей, если они уже были загружены
        if(ArtistsStorage.getInstance().getArtistCount() == 0) {
            new FetchArtistsTask().execute();
        }

        mThumbnailThread = new ThumbnailDownloader(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    //отдаем Bitmap только для видимых ImageView
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
    }

    public void onScrollStateChanged(AbsListView v, int s) { }

    /** Обработка скролинга, каждый раз подгружаем еще 20 элементов списка */
    public void onScroll(AbsListView view,
                         int firstVisible, int visibleCount, int totalCount) {

        boolean loadMore = (firstVisible + visibleCount >= totalCount) && (adapter.count < adapter.allCount);

        int addCount = 20;
        if(loadMore) {
            if(adapter.count + addCount > adapter.allCount) {
                addCount = adapter.allCount - adapter.count;
            }
            adapter.count += addCount;
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //завершаем фоновый поток
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    /** Обработчик кликов на элементах списка */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //анимация при клике на элемент списка
        Animation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1000);
        v.startAnimation(animation);

        Artist artist = (Artist)getListAdapter().getItem(position);
        Intent i = new Intent(getActivity(), ArtistDetailsActivity.class);
        //сообщаем ArtistDetailsFragment какой объект Artist следует отображать
        i.putExtra(ArtistDetailsFragment.EXTRA_ARTIST_DESC, artist.getId());
        startActivity(i);
    }

    public void setScrollListener()
    {
        getListView().setOnScrollListener(this);
    }

    /** Фоновый загрузчик списка исполнителей из JSON */
    private class FetchArtistsTask extends AsyncTask<Void,Void,ArrayList<Artist>> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected ArrayList<Artist> doInBackground(Void... params) {
            String endPoint = getResources().getString(R.string.yandex_json_endpoint);
            return new ArtistsFetcher().fetchArtists(endPoint);
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> items) {
            //инициализируем хранилище исполнителей
            ArtistsStorage.getInstance().setArtistList(items);
            //задаем адаптер для списка исполнителей
            adapter = new ArtistItemAdapter(items);
            setListAdapter(adapter);
            setScrollListener();
        }
    }

    /** Адаптер для отображения списка исполнителей */
    private class ArtistItemAdapter extends ArrayAdapter<Artist> {
        private int count = 20; //сколько в данный момент отображается элементов списка
        private int allCount; //сколько всего элементов (исполнителей)

        public int getCount() { return count; }

        public ArtistItemAdapter(ArrayList<Artist> items) {
            super(getActivity(), 0, items);
            allCount = items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.artist_item, parent, false);
            }

            Artist item = getItem(position);
            if(convertView.getTag() != item.getId()) {
                ImageView imageView = (ImageView) convertView.findViewById(R.id.artist_item_imageView);
                imageView.setImageResource(R.drawable.loadingstub);

                TextView name = (TextView) convertView.findViewById(R.id.artistName);
                TextView genres = (TextView) convertView.findViewById(R.id.artistGenrs);
                TextView albums = (TextView) convertView.findViewById(R.id.artistAlbums);

                name.setText(item.getName());
                genres.setText(item.getGenresStr());

                StringBuffer sb = new StringBuffer();
                sb.append(item.getAlbumsStr());
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(item.getTracksStr());
                albums.setText(sb.toString());

                //помечаем View как загруженный, чтобы не грузить картинку ещё раз при обновлении списка
                convertView.setTag(item.getId());
                mThumbnailThread.queueThumbnail(imageView, item.getSmallCover());
            }
            return convertView;
        }
    }
}