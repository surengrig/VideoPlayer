package videoplayer.comp.com.videoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

public class VolumeController extends AppCompatSeekBar {
    private static int sDefaultTimeout = 3000;
    private Context mContext;
    private boolean mShowing;
    private final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (mShowing) {
                    hide();
                } else {
                    show(sDefaultTimeout);
                }
            }
            return false;
        }
    };

    public VolumeController(Context context) {
        super(context);
        init(context);
    }

    public VolumeController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VolumeController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        initVolumeService();
        this.setVisibility(INVISIBLE);
    }

    private void hide() {
        if (mShowing) {
            mShowing = false;
            this.setVisibility(INVISIBLE);
        }
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show
     *                the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing) {
            mShowing = true;
            this.setVisibility(VISIBLE);
        }
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        removeCallbacks(mFadeOut);
        if (timeout != 0) {
            postDelayed(mFadeOut, timeout);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show(0); // show until hide is called
                break;
            case MotionEvent.ACTION_UP:
                show(sDefaultTimeout); // start timeout
                break;
            case MotionEvent.ACTION_CANCEL:
                hide();
                break;
            default:
                break;
        }
        super.onTouchEvent(event);
        return true;
    }

    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Activity's main view.
     * When VideoView calls this method, it will use the VideoView's parent
     * as the anchor.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        view.setOnTouchListener(mTouchListener);
    }


    private void initVolumeService() {
        final AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        this.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        this.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }
        });
    }
}
