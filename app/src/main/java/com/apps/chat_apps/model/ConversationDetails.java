package com.apps.chat_apps.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ConversationDetails implements Parcelable {
    private String conversationId;
    private String otherUserId;

    public ConversationDetails(String conversationId, String otherUserId) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
    }

    protected ConversationDetails(Parcel in) {
        conversationId = in.readString();
        otherUserId = in.readString();
    }

    public static final Creator<ConversationDetails> CREATOR = new Creator<ConversationDetails>() {
        @Override
        public ConversationDetails createFromParcel(Parcel in) {
            return new ConversationDetails(in);
        }

        @Override
        public ConversationDetails[] newArray(int size) {
            return new ConversationDetails[size];
        }
    };

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(conversationId);
        parcel.writeString(otherUserId);
    }
}
