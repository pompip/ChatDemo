package com.example.shff.chatdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ChatService extends Service {
    public ChatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
