package io.compactd.player.helper;

import android.os.Parcel;
import android.os.Parcelable;

import com.couchbase.lite.Manager;

import java.lang.reflect.InvocationTargetException;

import io.compactd.client.models.CompactdModel;

/**
 * Created by vinz243 on 14/12/2017.
 */

public class CompactdParcel<T extends CompactdModel> implements Parcelable {
    private String modelId;

    public CompactdParcel(T model) {
        this.modelId = model.getId();
    }

    protected CompactdParcel(Parcel in) {
        modelId = in.readString();
    }

    public T getModel (Class<T> modelClass, Manager manager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return modelClass.getConstructor(Manager.class, String.class).newInstance(manager, modelId);
    }

    public static final Creator<CompactdParcel> CREATOR = new Creator<CompactdParcel>() {
        @Override
        public CompactdParcel createFromParcel(Parcel in) {
            return new CompactdParcel(in);
        }

        @Override
        public CompactdParcel[] newArray(int size) {
            return new CompactdParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(modelId);
    }
}
