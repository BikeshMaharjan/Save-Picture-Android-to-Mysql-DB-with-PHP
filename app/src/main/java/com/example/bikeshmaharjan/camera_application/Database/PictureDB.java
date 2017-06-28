package com.example.bikeshmaharjan.camera_application.Database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by bikeshmaharjan on 6/26/17.
 */

public class PictureDB extends RealmObject {
    @PrimaryKey
    private int picture_id;
    public String picture_dir_path;

    public int getPicture_id() {
        return picture_id;
    }

    public String get_picture_path() {
        return picture_dir_path;
    }

    public void set_picture_path(String picture_dir_path) {
        this.picture_dir_path = picture_dir_path;
    }
}
