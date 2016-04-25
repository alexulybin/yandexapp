package ru.yandex.slimsaw.yandexapp;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ArtistsFetcher {
    private static final String TAG = "ArtistsFetcher";

    /** Загрузка контента в виде байт массива из переданного url */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /** Загрузить исполнителей из указанного url */
    public ArrayList<Artist> fetchArtists(String url) {
        ArrayList<Artist> items = new ArrayList<Artist>();

        try {
            String jsonString = getJSONFromUrl(url);
            if(jsonString != null) {
                JSONArray jArray = new JSONArray(jsonString);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);

                    String name = jObject.has("name") ? jObject.getString("name") : null;
                    String id = jObject.has("id") ? jObject.getString("id") : null;
                    String link = jObject.has("link") ? jObject.getString("link") : null;
                    Integer tracks = jObject.has("tracks") ? jObject.getInt("tracks") : null;
                    Integer albums = jObject.has("albums") ? jObject.getInt("albums") : null;

                    String desc = jObject.has("description") ? jObject.getString("description") : null;
                    if(desc != null && !desc.equals("")) {
                        desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);
                    }

                    JSONObject cover = jObject.has("cover") ? jObject.getJSONObject("cover") : null;
                    String small = null;
                    String big = null;
                    if(cover != null) {
                        small = cover.has("small") ? cover.getString("small") : null;
                        big = cover.has("big") ? cover.getString("big") : null;
                    }

                    JSONArray genres = jObject.has("genres") ? jObject.getJSONArray("genres") : null;
                    ArrayList<String> gen = null;
                    if(genres != null) {
                        gen = new ArrayList<String>();
                        for (int j = 0; j < genres.length(); j++) {
                            gen.add(genres.getString(j));
                        }
                    }

                    Artist item = new Artist();
                    item.setAlbums(albums);
                    item.setDescription(desc);
                    item.setId(id);
                    item.setLink(link);
                    item.setName(name);
                    item.setTracks(tracks);
                    item.setSmallCover(small);
                    item.setBigCover(big);
                    item.setGenres(gen);
                    items.add(item);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to fetch Artists", e);
        }
        return items;
    }

    /** Получить JSON из переданного URL */
    public String getJSONFromUrl(String url) {
        String json = null;
        InputStream is = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load JSON", e);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error converting result ", e);
        }
        return json;
    }
}