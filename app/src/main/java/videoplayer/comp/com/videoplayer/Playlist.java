package videoplayer.comp.com.videoplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    private static final String PLAYLIST_FILE_NAME = "playlist.pls";
    private final String mPath;
    private boolean mIsUpdated = false;
    private LinkedHashSet<String> mLocalFiles;
    private OnUpdateListener mListener;
    private ArrayList<String> mPlaylistItems = new ArrayList<>();
    private int mCurrentIndex = -1;

    public Playlist(String path) {
        mPath = path;

        mLocalFiles = new LinkedHashSet<>();
        File dir = new File(path);

        if (dir.listFiles() != null) {
            for (final File fileEntry : dir.listFiles()) {
                mLocalFiles.add(fileEntry.getName());
            }
        }
        loadPlaylist();
    }

    /**
     * Sets the index of current playing item
     * @param  index index to set the current playing item
     */
    public void setCurrentPlayingIndex(int index) {
        mCurrentIndex = index;
    }

    /**
     * Get the next item in playlist. If the current playing item is the last in the playlist
     * cycles to first item.
     * @return      the path to next item in playlist
     */
    public String getNext() {
        if (mPlaylistItems == null || mPlaylistItems.isEmpty()) return null;
        mCurrentIndex++;
        if (mCurrentIndex >= mPlaylistItems.size())
            mCurrentIndex = 0;
        return mPath + "/" + mPlaylistItems.get(mCurrentIndex);
    }

    /**
     * Get the previous item in playlist
     * @return      the path to next item in playlist
     */
    public String getPrev() {
        if (mPlaylistItems == null || mPlaylistItems.isEmpty()) return null;
        mCurrentIndex--;
        if (mCurrentIndex < 0)
            mCurrentIndex = 0;
        return mPath + "/" + mPlaylistItems.get(mCurrentIndex);
    }


    /**
     * Synchronizes the playlist with remote location
     * @param  url to synchronize the playlist with
     */

    public void update(final String url) {
        OkHttpClient client1 = new OkHttpClient.Builder()
                .build();
        PlaylistFetcher videoFetcher = new PlaylistFetcher(client1);

        try {
            videoFetcher.enqueue(url, new PlaylistFetcher.OnCompleteListener() {
                @Override
                public void onComplete(List<String> fileNames) {
                    mIsUpdated = removeLocalFiles(fileNames);
                    mIsUpdated = downloadRemoteFiles(url, fileNames)
                            || mIsUpdated;

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Caches the files for offline playback
     * @param  baseUrl url to base location for the files
     * @param  fetchedFiles files to download from baseUrl location
     */
    private boolean downloadRemoteFiles(String baseUrl, List<String> fetchedFiles) {

        HashSet<String> filesToDownload = new HashSet<>();
        if (fetchedFiles != null && !fetchedFiles.isEmpty()) {
            for (String remote : fetchedFiles) {
                if (!mLocalFiles.contains(remote)) {
                    filesToDownload.add(remote);
                    mIsUpdated = true;
                }

            }
            mPlaylistItems = new ArrayList<>(fetchedFiles);
        } else {
            mPlaylistItems = new ArrayList<>(mLocalFiles);
        }
        mLocalFiles.addAll(filesToDownload);
        final DownloadManager downloadManager = new DownloadManager(mPath);
        downloadManager.setOnCompleteListener(new DownloadManager.OnCompleteListener() {
            @Override
            public void onComplete() {
                if (mListener != null) mListener.onUpdated(mLocalFiles);
                if (mIsUpdated) savePlaylist();
            }
        });

        downloadManager.downloadPlaylist(baseUrl, new ArrayList<>(filesToDownload));
        return mIsUpdated;
    }

    /**
     * Save the current playlist in cache folder
     */
    public void savePlaylist() {
        String path = mPath + "/" + PLAYLIST_FILE_NAME;
        File f = new File(path);
        if (mIsUpdated || !f.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path), "utf-8"))) {
                for (String i : mPlaylistItems) {
                    writer.write(i);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Loads the playlist from cache folder
     */
    private void loadPlaylist() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(mPath + "/" + PLAYLIST_FILE_NAME), "utf-8"))) {
            String line;
            mPlaylistItems = new ArrayList<>();
            while ((line = reader.readLine()) != null)
                mPlaylistItems.add(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes cached files not present in fetchedUrls
     */
    private boolean removeLocalFiles(List<String> fetchedUrls) {
        HashSet<String> filesToRemove = new HashSet<>();
        if (null != mLocalFiles && fetchedUrls != null) {
            for (String local : mLocalFiles) {
                if (!fetchedUrls.contains(local)) {
                    filesToRemove.add(local);
                }
            }

            mLocalFiles.removeAll(filesToRemove);
        } else {
            return true;
        }
        return false;
    }


    /**
     * Sets a listener to call when files caching and playlist synchronization is finished
     * @param  listener listener to call
     */
    void setOnUpdateListener(OnUpdateListener listener) {
        mListener = listener;
    }


    /**
     * @return the path to current item in playlist
     */
    public String get() {
        if (mPlaylistItems == null || mPlaylistItems.isEmpty() || mCurrentIndex >= mPlaylistItems.size())
            return null;
        return mPath + "/" + mPlaylistItems.get(mCurrentIndex);
    }

    /**
     * @return the index of current item in playlist
     */
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    interface OnUpdateListener {
        void onUpdated(Set<String> files);
    }
}
