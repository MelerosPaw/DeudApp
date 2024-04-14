package melerospaw.deudapp.iu.widgets;

import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.TransitionSet;

public class CustomTransitionSet extends TransitionSet {

    public CustomTransitionSet() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new Fade(Fade.OUT)).
                addTransition(new ChangeBounds()).
                addTransition(new Fade(Fade.IN));
    }
}
