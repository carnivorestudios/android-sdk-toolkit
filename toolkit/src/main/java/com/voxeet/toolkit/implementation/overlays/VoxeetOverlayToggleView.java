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
        super(listener, provider, context, overlay);
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
}