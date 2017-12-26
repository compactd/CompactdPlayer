package io.compactd.client;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import io.compactd.player.R;

/**
 * Created by Vincent on 26/12/2017.
 */

public enum CompactdPreset {
    ORIGINAL("original", R.string.preset_original, R.string.preset_original_desc, 0),
    HIGH("high", R.string.preset_high, R.string.preset_high_desc, 245),
    NORMAL("normal", R.string.preset_normal, R.string.preset_normal_desc, 190),
    LOW("low", R.string.preset_low, R.string.preset_low_desc, 165);

    public static final CompactdPreset[] PRESETS = {HIGH, NORMAL, LOW};

    private final String preset;
    private final int name;
    private final int desc;
    private final int bitrate;

    CompactdPreset(String preset, @StringRes int name, @StringRes int desc, int bitrate) {
        this.preset = preset;
        this.name = name;
        this.desc = desc;
        this.bitrate = bitrate;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getDesc() {
        return desc;
    }

    public int getName() {
        return name;
    }

    public String getPreset() {
        return preset;
    }

    @Nullable
    public static CompactdPreset from (String id) {
        for (CompactdPreset preset : PRESETS) {
            if (preset.getPreset().equals(id)) {
                return preset;
            }
        }
        return null;
    }
    @Nullable
    public static CompactdPreset from (Resources resources, String name) {
        for (CompactdPreset preset : PRESETS) {
            if (resources.getString(preset.getName()).equals(name)) {
                return preset;
            }
        }
        return null;
    }

    public static List<String> names (Resources resources) {
        List<String> names = new ArrayList<>();
        for (CompactdPreset preset : PRESETS) {
            names.add(resources.getString(preset.getName()));
        }
        return names;
    }

    @Override
    public String toString() {
        return "CompactdPreset{" +
                "preset='" + preset + '\'' +
                ", name=" + name +
                ", desc=" + desc +
                ", bitrate=" + bitrate +
                '}';
    }
}
