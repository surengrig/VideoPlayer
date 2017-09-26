package videoplayer.comp.com.videoplayer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by suren on 9/25/17.
 */

public class DownloadManager {
    private static final String TAG = DownloadManager.class.getSimpleName();
    private static int sInProcessCount = 0;
    private final OkHttpClient mClient;
    public String mPath;
    private OnCompleteListener mOnCompleteListener;

    public DownloadManager(String path) {
        mPath = path;
        mClient = new OkHttpClient();
    }

    private void enqueue(String basePath, final String fileName) {

        Request request = new Request.Builder()
                .url(basePath + "/" + fileName)
                .build();

        mClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                sInProcessCount--;
                if (sInProcessCount == 0 && mOnCompleteListener != null) {
                    mOnCompleteListener.onComplete();
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                sInProcessCount--;
                if (response.isSuccessful()) {

                    File directory = new File(mPath);
                    if (! directory.exists()){
                        directory.mkdir();
                    }
                    File downloadedFile = new File(mPath, fileName);
                    BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                if (sInProcessCount == 0 && mOnCompleteListener != null) {
                    mOnCompleteListener.onComplete();
                }
            }
        });
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        mOnCompleteListener = listener;
    }

    public void downloadPlaylist(String baseUrl, List<String> fileNames) {
        sInProcessCount = fileNames.size();
        HashSet<String> noDuplicates = new HashSet<>(fileNames);

        if (fileNames.isEmpty()) {
            mOnCompleteListener.onComplete();
            return;
        }
        for (String fileName : noDuplicates) {
            enqueue(baseUrl, fileName);
        }
    }

    interface OnCompleteListener {
        //        TODO remove argument
        void onComplete();
    }
}