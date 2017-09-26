package videoplayer.comp.com.videoplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;

/**
 * Created by suren on 9/26/17.
 */

class Playlist {
    private final String mPath;
    LinkedHashSet<String> localFiles;
    private OnUpdateListener mListener;

    private ArrayList<String> mPlaylistItems = new ArrayList<>();

    private int mCurrentIndex = -1;

    public Playlist(String path) {
        mPath = path;

        localFiles = new LinkedHashSet<>();
        File dir = new File(path);

        if (dir != null && dir.listFiles() != null) {
            for (final File fileEntry : dir.listFiles()) {
                localFiles.add(fileEntry.getName());
            }
        }
        mPlaylistItems = new ArrayList<>(localFiles);
    }

    public void setCurrentPlayingIndex(int index) {
        mCurrentIndex = index;
    }

    public String getNext() {
        if (mPlaylistItems == null || mPlaylistItems.isEmpty()) return null;
        mCurrentIndex++;
        if (mCurrentIndex >= mPlaylistItems.size())
            mCurrentIndex = 0;
        return mPath + "/" + mPlaylistItems.get(mCurrentIndex);
    }

    public String getPrev() {
        if (mPlaylistItems == null || mPlaylistItems.isEmpty()) return null;
        mCurrentIndex--;
        if (mCurrentIndex < 0)
            mCurrentIndex = 0;
        return mPath + "/" + mPlaylistItems.get(mCurrentIndex);
    }


    public void update(final String url) {
        OkHttpClient client1 = new OkHttpClient.Builder()
                .build();
        PlaylistFetcher videoFetcher = new PlaylistFetcher(client1);

        try {
            videoFetcher.enqueue(url, new PlaylistFetcher.OnCompleteListener() {
                @Override
                public void onComplete(List<String> fileNames) {
                    boolean updated = false;
                    updated = removeLocalFiles(fileNames);
                    updated = downloadRemoteFiles(url, fileNames)
                            || updated;

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean downloadRemoteFiles(String baseUrl, List<String> fetchedFiles) {

        boolean updated = false;
        HashSet<String> filesToDownload = new HashSet<>();
        if (fetchedFiles != null && !fetchedFiles.isEmpty()) {
            for (String remote : fetchedFiles) {
                if (!localFiles.contains(remote)) {
                    filesToDownload.add(remote);
                    updated = true;
                }

            }
            mPlaylistItems = new ArrayList<>(fetchedFiles);
        } else {
            mPlaylistItems = new ArrayList<>(localFiles);
        }
        localFiles.addAll(filesToDownload);
        final DownloadManager downloadManager = new DownloadManager(mPath);
        downloadManager.setOnCompleteListener(new DownloadManager.OnCompleteListener() {
            @Override
            public void onComplete() {
                if (mListener != null) mListener.onUpdated(localFiles);
            }
        });

        downloadManager.downloadPlaylist(baseUrl, new ArrayList<String>(filesToDownload));
        return updated;
    }

    private boolean removeLocalFiles(List<String> fetchedUrls) {
        boolean updated = false;
        HashSet<String> filesToRemove = new HashSet<>();
        if (null != localFiles && fetchedUrls != null) {
            for (String local : localFiles) {
                if (!fetchedUrls.contains(local)) {
                    filesToRemove.add(local);
                    updated = true;
                }
            }

            localFiles.removeAll(filesToRemove);
        } else {
            return true;
        }

        return false;
    }

    void setOnUpdateListener(OnUpdateListener listener) {
        mListener = listener;
    }


    public String get() {
        if (mPlaylistItems == null || mPlaylistItems.isEmpty() || mCurrentIndex >= mPlaylistItems.size())
            return null;
        return mPath + "/" + mPlaylistItems.get(mCurrentIndex);
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    interface OnUpdateListener {
        void onUpdated(Set<String> files);
    }
}
