package io.compactd.client;

/**
 * Created by Vincent on 30/10/2017.
 */

public class CompactdException extends Exception {
    private CompactdErrorCode mCode;
    public CompactdException(CompactdErrorCode code) {
        super();
        mCode = code;
    }

    public CompactdErrorCode getCode() {
        return mCode;
    }
}
