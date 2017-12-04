package io.compactd.client.models;

/**
 * Created by vinz243 on 10/11/2017.
 */

public enum ArtworkSize {
    LARGE("large", 300),
    SMALL("small", 64);

    private final String size;
    private final int dimension;

    ArtworkSize(String size, int dimension) {
        this.size = size;
        this.dimension = dimension;
    }

    public String getSize() {
        return size;
    }

    public int getDimension() {
        return dimension;
    }
}
