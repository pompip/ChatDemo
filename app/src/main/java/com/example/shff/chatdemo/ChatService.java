package com.example.shff.chatdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChatService extends Service {

    private WebSocket webSocket;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                if (chatList.isEmpty()) {
                    sendNotification((String) msg.obj);
                } else {
                    for (OnChatListener chatListener : chatList) {
                        chatListener.onChatMessage((String) msg.obj);
                    }
                }
                return true;
            }else if (msg.what==200){
                for (OnChatListener chatListener : chatList) {
                    chatListener.onChatMessage((ByteString) msg.obj);
                }
            }
            return false;
        }
    });
    private final OkHttpClient client;
//    private Thread thread;
    private ExecutorService service;

    public ChatService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .build();
    }

    private void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentText(message);
        builder.setContentTitle("你有新的未读消息");
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        Notification build = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), build);

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    private static final String TAG = "ChatService";

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("ChatMessage"));
        service = Executors.newFixedThreadPool(8);
        service.submit(runnable);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        chatList.clear();
        service.shutdown();
    }

    private static LinkedList<OnChatListener> chatList = new LinkedList<>();


    public static void addChatListener(OnChatListener onChatListener) {
        chatList.add(onChatListener);
    }

    public static void removeChatListener(OnChatListener onChatListener) {
        chatList.remove(onChatListener);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {

            if ("img".equals(intent.getAction())){
                service.submit(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }else {
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        String chatMessage = intent.getStringExtra("chatMessage");
                        if ("img".equals(chatMessage)){
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher).compress(Bitmap.CompressFormat.PNG,100,bos);
                            webSocket.send(ByteString.of(bos.toByteArray()));
                        }else {


                        }
                        webSocket.send(chatMessage);
                    }
                });
            }



        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Request request = new Request.Builder().url("http://192.168.4.45:8080/echo").build();
            webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    handler.sendMessage(handler.obtainMessage(100, text));
                    Log.d(TAG, "onMessage() called with: webSocket = [" + webSocket + "], text = [" + text + "]");
                }

                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    Log.d(TAG, "onOpen() called with: webSocket = [" + webSocket + "], response = [" + response + "]");
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    Log.d(TAG, "onMessage() called with: webSocket = [" + webSocket + "], bytes = [" + bytes + "]");
                    bytes.asByteBuffer().array();
                    handler.sendMessage(handler.obtainMessage(200,bytes));

                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    Log.d(TAG, "onClosing() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    stopSelf();
                    Log.d(TAG, "onClosed() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    stopSelf();
                    Log.d(TAG, "onFailure() called with: webSocket = [" + webSocket + "], t = [" + t + "], response = [" + response + "]");
                }
            });
        }
    };
}
