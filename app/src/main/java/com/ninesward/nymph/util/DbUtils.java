package com.ninesward.nymph.util;

import com.litesuits.orm.LiteOrm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.ninesward.nymph.NymphApp;
import com.ninesward.nymph.event.BroadcastEvent;
import com.ninesward.nymph.model.LocBookmark;
import com.ninesward.nymph.model.LocPoint;

import java.util.ArrayList;
import java.util.Collection;

public class DbUtils {
    private static final String SHARED_PREF_NAME = "FakeGPS";
    private static final String KEY_LAST_LOC = "last_loc";

    private DbUtils() {
    }


    public static long insertBookmark(LocBookmark bookmark) {
        if (bookmark == null) {
            return -1;
        }
        long id = NymphApp.getLiteOrm().insert(bookmark, ConflictAlgorithm.Replace);
        if (id != -1) {
            notifyBookmarkUpdate();
        }
        return id;
    }

    public static void deleteBookmark(LocBookmark bookmark) {
        if (bookmark == null) return;
        NymphApp.getLiteOrm().delete(bookmark);
        notifyBookmarkUpdate();
    }

    public static void saveBookmark(Collection<LocBookmark> bookmarks) {
        NymphApp.getLiteOrm().deleteAll(LocBookmark.class);
        NymphApp.getLiteOrm().save(bookmarks);
    }

    public static ArrayList<LocBookmark> getAllBookmark() {
        return NymphApp.getLiteOrm().query(LocBookmark.class);
    }

    public static void saveLastLocPoint(@NonNull Context context, @NonNull LocPoint locPoint) {
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_LOC, locPoint.toString())
                .apply();
    }

    public static String getLastLocPoint(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_LOC, "");
    }

    public static void notifyBookmarkUpdate() {
        Intent intent = new Intent(BroadcastEvent.BookMark.ACTION_BOOK_MARK_UPDATE);
        LocalBroadcastManager.getInstance(NymphApp.get()).sendBroadcast(intent);
    }
}
