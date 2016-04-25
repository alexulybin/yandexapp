package ru.yandex.slimsaw.yandexapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Фоновый поток для загрузки изображений исполнителей */
public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mResponseHandler;
    Listener<Token> mListener;
    private LruCache<String, Bitmap> mLruCache;

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    @TargetApi(12)
    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;

        //создаем кэш изображений
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    Log.d(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url) {
        requestMap.put(token, url);
        //отправляем на постановку в очередь сообщений
        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    /** Загрузка изображений */
    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);
            if(url == null)
                return;

            //сначала ищем изображение в кэше
            Bitmap thumbnailImage = getBitmapFromMemCache(url);
            if (thumbnailImage == null) {
                //если не нашли - загружаем по URL и кладем его в кэш
                byte[] bitmapBytes = new ArtistsFetcher().getUrlBytes(url);
                thumbnailImage = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                addBitmapToMemoryCache(url, thumbnailImage);
            }
            final Bitmap bitmap = thumbnailImage;

            mResponseHandler.post(new Runnable() {
                public void run() {
                    if(requestMap.get(token) != url)
                        return;

                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }

    /** Очистка очереди */
    public void clearQueue() {
        mHandler.removeCallbacksAndMessages(null);
        requestMap.clear();
    }
}