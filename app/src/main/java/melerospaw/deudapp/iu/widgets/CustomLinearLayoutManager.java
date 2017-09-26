package melerospaw.deudapp.iu.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class CustomLinearLayoutManager extends LinearLayoutManager {

    private boolean scrollEnabled;

    public CustomLinearLayoutManager(Context context) {
        super(context);
        init();
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init();
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        scrollEnabled = true;
    }


    public void setScrollEnabled(boolean enabled) {
        this.scrollEnabled = enabled;
    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollEnabled && super.canScrollHorizontally();
    }

    @Override
    public boolean canScrollVertically() {
        return scrollEnabled && super.canScrollVertically();
    }
}
