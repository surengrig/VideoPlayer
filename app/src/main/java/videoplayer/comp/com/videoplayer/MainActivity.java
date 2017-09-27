package videoplayer.comp.com.videoplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    VideoView mVideoView;
    private Playlist mPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        mVideoView = findViewById(R.id.videoView);
        MediaController vidControl = new CustomMediaController(this);
        vidControl.setAnchorView(mVideoView);
        mVideoView.setMediaController(vidControl);


        mPlaylist = new Playlist(this.getCacheDir().getPath() + "/" + Constants.CACHE_DIR);


        mPlaylist.setOnUpdateListener(new Playlist.OnUpdateListener() {
            @Override
            public void onUpdated(Set<String> files) {
                playVideo(mPlaylist.getNext());
            }
        });

        if (savedInstanceState == null) {
            mPlaylist.update(Constants.PLAYLIST_URL);
        } else {
            int position = savedInstanceState.getInt(Constants.PLAYLIST_CURRENT_POSITION);
            int index = savedInstanceState.getInt(Constants.PLAYLIST_CURRENT_ITEM);
            boolean isPlaying = savedInstanceState.getBoolean(Constants.PLAYLIST_IS_PLAYING);

            mPlaylist.setCurrentPlayingIndex(index);
            if (isPlaying) {
                String path = mPlaylist.get();
                playVideo(path, position);
            }
        }
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playVideo(mPlaylist.getNext());
            }
        });
    }

    void playVideo(final String path) {
        playVideo(path, 0);
    }

    void playVideo(final String path, final int position) {
        if (path != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVideoView.setVideoPath(path);
                    if (position > 0) mVideoView.seekTo(position);
                    mVideoView.requestFocus();
                    mVideoView.start();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Constants.PLAYLIST_IS_PLAYING, mVideoView.isPlaying());
        outState.putInt(Constants.PLAYLIST_CURRENT_POSITION, mVideoView.getCurrentPosition());
        outState.putInt(Constants.PLAYLIST_CURRENT_ITEM, mPlaylist.getCurrentIndex());
        mPlaylist.savePlaylist();
        super.onSaveInstanceState(outState);
    }

}
