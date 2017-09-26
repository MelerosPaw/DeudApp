package melerospaw.deudapp.iu.widgets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

/**
 * Created by Juan José Melero on 30/06/2015.
 */
public class ContextRecyclerView extends RecyclerView {

    private RecyclerContextMenuInfo mContextMenuInfo;

    public ContextRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getChildAdapterPosition(originalView);
        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            mContextMenuInfo = new RecyclerContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    /**Contains the id and the position in the adapter's data collection of the view
     * triggering its context menu. */
    public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

        final public int position;
        final public long id;

        public RecyclerContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }
    }
}
