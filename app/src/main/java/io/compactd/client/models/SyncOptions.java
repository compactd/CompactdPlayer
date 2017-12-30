package io.compactd.client.models;

import io.compactd.client.CompactdPreset;

/**
 * Created by Vincent on 26/12/2017.
 */

public class SyncOptions {
    private boolean mNoMedia;
    private CompactdPreset mPreset;
    private String mDestination;

    public SyncOptions() {
        mPreset = CompactdPreset.NORMAL;
        mNoMedia = false;
    }

    public CompactdPreset getPreset() {
        return mPreset;
    }

    public void setPreset(CompactdPreset preset) {
        this.mPreset = preset;
    }

    public boolean isNoMedia() {
        return mNoMedia;
    }

    public void setNoMedia(boolean noMedia) {
        this.mNoMedia = noMedia;
    }

    public String getDestination() {
        return mDestination;
    }

    public void setDestination(String destination) {
        this.mDestination = destination;
    }
}
