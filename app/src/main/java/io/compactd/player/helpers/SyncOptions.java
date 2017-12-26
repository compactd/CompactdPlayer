package io.compactd.player.helpers;

import io.compactd.client.CompactdPreset;

/**
 * Created by Vincent on 26/12/2017.
 */

public class SyncOptions {
    private boolean noMedia;
    private CompactdPreset preset;
    private String destination;

    public SyncOptions() {
        preset = CompactdPreset.NORMAL;
        noMedia = false;
    }

    public CompactdPreset getPreset() {
        return preset;
    }

    public void setPreset(CompactdPreset preset) {
        this.preset = preset;
    }

    public boolean isNoMedia() {
        return noMedia;
    }

    public void setNoMedia(boolean noMedia) {
        this.noMedia = noMedia;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
