package videoplayer.comp.com.videoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.SeekBar;

/**
 * Created by suren on 9/25/17.
 */

public class MediaControllerEx extends MediaController {

    Context mContext;

    public MediaControllerEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MediaControllerEx(Context context, boolean useFastForward) {
        super(context, useFastForward);
        mContext = context;
    }

    public MediaControllerEx(Context context) {
        super(context);
        mContext = context;
    }


    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        SeekBar progressBar = new SeekBar(mContext);
        float padding = 5;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START;
        addView(progressBar, params);
    }
}
