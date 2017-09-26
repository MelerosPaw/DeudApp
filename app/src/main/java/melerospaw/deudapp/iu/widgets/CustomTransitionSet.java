package melerospaw.deudapp.iu.widgets;

import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.TransitionSet;

public class CustomTransitionSet extends TransitionSet {

    public CustomTransitionSet() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new Fade(Fade.OUT)).
                addTransition(new ChangeBounds()).
                addTransition(new Fade(Fade.IN));
    }
}
