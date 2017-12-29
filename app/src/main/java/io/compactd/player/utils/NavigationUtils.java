package io.compactd.player.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;

import io.compactd.client.models.CompactdAlbum;
import io.compactd.client.models.CompactdArtist;
import io.compactd.player.helpers.CompactdParcel;
import io.compactd.player.ui.activities.AlbumActivity;
import io.compactd.player.ui.activities.ArtistActivity;

/**
 * Created by Vincent on 29/12/2017.
 */

public class NavigationUtils {
    public static void goToArtist(@NonNull final Activity activity, final CompactdArtist artist, @Nullable Pair... sharedElements) {
        final Intent intent = new Intent(activity, ArtistActivity.class);
        intent.putExtra(ArtistActivity.BUNDLE_ARTIST_KEY, new CompactdParcel<>(artist));

        //noinspection unchecked
        activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
    }

    public static void goToAlbum(@NonNull final Activity activity, final CompactdAlbum album, @Nullable Pair... sharedElements) {
        final Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra(AlbumActivity.BUNDLE_ALBUM_KEY, new CompactdParcel<>(album));

        //noinspection unchecked
        activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
    }
}