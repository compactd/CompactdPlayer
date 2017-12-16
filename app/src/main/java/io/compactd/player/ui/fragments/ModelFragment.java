package io.compactd.player.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.compactd.client.models.CompactdModel;
import io.compactd.player.R;
import io.compactd.player.adapter.ModelAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ModelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public abstract class ModelFragment<T extends CompactdModel> extends Fragment {
    public static final int HORIZONTAL_LAYOUT = 0x01;
    public static final int VERTICAL_LAYOUT = 0x02;
    public static final int GRID_LAYOUT = 0x03;

    protected ModelAdapter<T> adapter;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL_LAYOUT, VERTICAL_LAYOUT, GRID_LAYOUT})
    @interface LayoutMode {}

    public static final String ARG_LAYOUT = "layout";
    public static final String ARG_STARTKEY = "start_key";

    @LayoutMode
    private int mLayout = GRID_LAYOUT;

    protected String mStartkey = "library/";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static <M extends ModelFragment> M newInstance(Class<M> clazz, @LayoutMode int layout, String startKey) {
        M fragment = null;
        try {
            fragment = clazz.newInstance();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Bundle args = new Bundle();

        args.putInt(ARG_LAYOUT, layout);
        if (startKey != null) {
            args.putString(ARG_STARTKEY, startKey);
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLayout = getArguments().getInt(ARG_LAYOUT);
            mStartkey = getArguments().getString(ARG_STARTKEY, "library/");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_model_adapter, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = getLayoutManager();
        layoutManager.setOrientation(getLayout());

        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @NonNull
    private LinearLayoutManager getLayoutManager() {
        if (mLayout == GRID_LAYOUT) {
            return new GridLayoutManager(getContext(), 3);
        }
        return new LinearLayoutManager(getContext());
    }

    private int getLayout() {
        switch (mLayout) {
            case GRID_LAYOUT:
                return GridLayoutManager.VERTICAL;
            case VERTICAL_LAYOUT:
                return LinearLayoutManager.VERTICAL;
            case HORIZONTAL_LAYOUT:
                return LinearLayoutManager.HORIZONTAL;
        }
        return -1;
    }

    public void bindModel (Class<? extends ModelAdapter<T>> clazz, List<T> items) {

        try {
            adapter = clazz.getConstructor(Context.class, ModelAdapter.LayoutType.class)
                    .newInstance(getContext(), getAdapterLayout());

            adapter.setTintBackground(mLayout != HORIZONTAL_LAYOUT);
            adapter.swapItems(items);

            recyclerView.setAdapter(adapter);

        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private ModelAdapter.LayoutType getAdapterLayout() {
        if (mLayout == VERTICAL_LAYOUT) {
            return ModelAdapter.LayoutType.ListItem;
        }
        return ModelAdapter.LayoutType.GridItem;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
