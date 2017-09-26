package videoplayer.comp.com.videoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;

/**
 * Created by suren on 9/25/17.
 */

public class CustomMediaController extends MediaController{

    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public CustomMediaController(Context context) {
        super(context);
    }
}
