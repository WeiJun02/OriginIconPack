package com.origin.icon.pack.listener;

import com.google.android.material.appbar.AppBarLayout;

/**
 * @author hawvu
 */
public abstract class AppBarStateChangeListener
    implements AppBarLayout.OnOffsetChangedListener{

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED,Math.abs(i),appBarLayout.getTotalScrollRange());
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED,Math.abs(i),appBarLayout.getTotalScrollRange());
            }
            mCurrentState = State.COLLAPSED;
        } else {
//            if (mCurrentState != State.IDLE) {
//                onStateChanged(appBarLayout, State.IDLE,Math.abs(i),appBarLayout.getTotalScrollRange());
//            }
//
            onStateChanged(appBarLayout, State.IDLE,Math.abs(i),appBarLayout.getTotalScrollRange());
            mCurrentState = State.IDLE;
        }
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, State state,int i,int max);
}
