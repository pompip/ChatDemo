package com.example.shff.chatdemo;

import com.example.shff.chatdemo.bean.ChatMessage;

import okio.ByteString;

/**
 * Created by shff on 2017/6/12.
 */

public interface OnChatListener {
    void onChatMessage(ChatMessage message);
    void onChatMessage(ByteString byteString);
}
