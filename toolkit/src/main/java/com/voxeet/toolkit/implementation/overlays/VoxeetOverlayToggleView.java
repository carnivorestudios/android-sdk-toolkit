package com.voxeet.toolkit.implementation.overlays;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.voxeet.toolkit.R;
import com.voxeet.toolkit.activities.VoxeetEventCallBack;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;

public class VoxeetOverlayToggleView extends AbstractVoxeetOverlayView {

    VoxeetEventCallBack voxeetEventCallBack;
    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param listener the listener used to create the sub view
     * @param provider
     * @param context  the context
     * @param overlay
     */
    public VoxeetOverlayToggleView(@NonNull IExpandableViewProviderListener listener,
                                   @NonNull IVoxeetSubViewProvider provider,
                                   @NonNull VoxeetEventCallBack mVoxeetEventCallBack,
                                   @NonNull Context context,
                                   @NonNull OverlayState overlay) {
        super(listener, provider, mVoxeetEventCallBack, context, overlay);
        voxeetEventCallBack = mVoxeetEventCallBack;
    }

    @Override
    final protected void onActionButtonClicked() {
        toggleSize();

        getExpandableViewProviderListener().onActionButtonClicked();
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_toggle_view;
    }

    @Override
    public void onConferenceMute(Boolean isMuted) {
        voxeetEventCallBack.onConferenceMute(isMuted);
    }

    @Override
    public void onConferenceVideo(Boolean isVideoEnabled) {
        voxeetEventCallBack.onConferenceVideo(isVideoEnabled);
    }

    @Override
    public void onConferenceCallEnded() {
        voxeetEventCallBack.onConferenceCallEnded();
    }

    @Override
    public void onConferenceMinimized() {
        voxeetEventCallBack.onConferenceMinimized();
    }

    @Override
    public void onConferenceSpeakerOn(Boolean isSpeakerOn) {
        voxeetEventCallBack.onConferenceSpeakerOn(isSpeakerOn);
    }

    @Override
    public void onConferenceNoMoreUser() {
        voxeetEventCallBack.onConferenceNoMoreUsers();
    }

    @Override
    public void onConferenceLeft() {
        voxeetEventCallBack.onConferenceLeft();
    }
}