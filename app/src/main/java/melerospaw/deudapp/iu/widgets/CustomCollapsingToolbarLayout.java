package melerospaw.deudapp.iu.widgets;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;


public class CustomCollapsingToolbarLayout extends CollapsingToolbarLayout {

    private boolean scrollEnabled;

    public CustomCollapsingToolbarLayout(Context context) {
        super(context);
        init();
    }

    public CustomCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scrollEnabled = true;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }


}
