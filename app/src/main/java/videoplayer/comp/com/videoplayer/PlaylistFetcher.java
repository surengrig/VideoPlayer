package videoplayer.comp.com.videoplayer;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class PlaylistFetcher {
    private static final String TAG = PlaylistFetcher.class.getSimpleName();
    private final OkHttpClient client;
    private final List<String> fetchedUrls = Collections.synchronizedList(
            new LinkedList<String>());

    public PlaylistFetcher(OkHttpClient client) {
        this.client = client;
    }

    public void enqueue(String url, OnCompleteListener listener) throws IOException {
        fetch(url, listener);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void fetch(final String url, final OnCompleteListener listener) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onComplete(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();


                String contentType = response.header("Content-Type");
                if (responseCode != 200 || contentType == null) {
                    return;
                }

                MediaType mediaType = MediaType.parse(contentType);
                if (mediaType == null || !mediaType.subtype().equalsIgnoreCase("html")) {
                    return;
                }

                Document document = Jsoup.parse(response.body().string(), url.toString());
//                for (Element element : document.select("a[href]")) {
//                    String href = element.attr("href");
//                    HttpUrl link = response.request().url().resolve(href);
//                    if (link == null)
//                        continue; // URL is either invalid or its scheme isn't http/https.
//                    fetchedUrls.add(link);
//                }
                for (Element element : document.select("a[href]")) {
//                    TODO create a mediatype checker method
                    if (element.html().endsWith(".mp4") || element.html().endsWith(".avi") || element.html().endsWith(".mkv")) {
                        fetchedUrls.add(element.html());
                    }
                }
                Log.d(TAG, fetchedUrls.toString());
                listener.onComplete(fetchedUrls);
            }
        });

    }

    public Object getUrls() {
        return fetchedUrls;
    }

    interface OnCompleteListener {
        void onComplete(List<String> fetchedUrls);
    }
}