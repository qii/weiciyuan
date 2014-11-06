package org.qii.weiciyuan.support.database.dbUpgrade;

import org.qii.weiciyuan.support.database.DatabaseHelper;
import org.qii.weiciyuan.support.file.FileManager;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * User: qii
 * Date: 14-6-14
 */
public class Upgrade36to37 {

    private static final String AVATAR_SMAll = "avatar_small";
    private static final String AVATAR_LARGE = "avatar_large";
    private static final String PICTURE_THUMBNAIL = "picture_thumbnail";
    private static final String PICTURE_BMIDDLE = "picture_bmiddle";
    private static final String PICTURE_LARGE = "picture_large";
    private static final String COVER = "cover";

    public static void upgrade(SQLiteDatabase db) {
        db.execSQL(DatabaseHelper.CREATE_DOWNLOAD_PICTURES_TABLE_SQL);

        String thumbnailPath = FileManager.getSdCardPath() + File.separator + PICTURE_THUMBNAIL;
        String middlePath = FileManager.getSdCardPath() + File.separator + PICTURE_BMIDDLE;
        String oriPath = FileManager.getSdCardPath() + File.separator + PICTURE_LARGE;
        String largeAvatarPath = FileManager.getSdCardPath() + File.separator + AVATAR_LARGE;
        String smallAvatarPath = FileManager.getSdCardPath() + File.separator + AVATAR_SMAll;
        String coverPath = FileManager.getSdCardPath() + File.separator + COVER;

        FileManager.deleteDirectory(new File(thumbnailPath));
        FileManager.deleteDirectory(new File(middlePath));
        FileManager.deleteDirectory(new File(oriPath));
        FileManager.deleteDirectory(new File(largeAvatarPath));
        FileManager.deleteDirectory(new File(smallAvatarPath));
        FileManager.deleteDirectory(new File(coverPath));
    }
}
