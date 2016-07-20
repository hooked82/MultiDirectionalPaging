package com.hookedroid.multidirectionalpaging.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VerticalItemModel implements Parcelable {

    private String uuid;
    private String title;
    private String description;
    private String horizontalItemUuid;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(uuid);
        out.writeString(title);
        out.writeString(description);
        out.writeString(horizontalItemUuid);
    }

    private void readFromParcel(Parcel in) {
        uuid = in.readString();
        title = in.readString();
        description = in.readString();
        horizontalItemUuid = in.readString();
    }

    public static Parcelable.Creator<VerticalItemModel> CREATOR
            = new Parcelable.Creator<VerticalItemModel>() {
        public VerticalItemModel createFromParcel(Parcel in) {
            return new VerticalItemModel(in);
        }

        public VerticalItemModel[] newArray(int size) {
            return new VerticalItemModel[size];
        }
    };

    public VerticalItemModel() {

    }

    private VerticalItemModel(Parcel in) {
        readFromParcel(in);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHorizontalItemUuid() {
        return horizontalItemUuid;
    }

    public void setHorizontalItemUuid(String horizontalItemUuid) {
        this.horizontalItemUuid = horizontalItemUuid;
    }
}
