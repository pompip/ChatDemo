package com.example.shff.chatdemo;

import okio.ByteString;

/**
 * Created by shff on 2017/6/12.
 */

public interface OnChatListener {
    void onChatMessage(String message);
    void onChatMessage(ByteString byteString);
}
