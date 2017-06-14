package com.example.shff.chatdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shff.chatdemo.bean.ChatMessage;

import java.util.ArrayList;

import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    OnChatListener onChatListener = new OnChatListener() {
        @Override
        public void onChatMessage(ChatMessage message) {
            chatList.add(message);
            adapter.notifyDataSetChanged();

        }

        @Override
        public void onChatMessage(ByteString byteString) {

        }
    };
    ArrayList<ChatMessage> chatList = new ArrayList<>();
    private ChatAdapter adapter;
    private ServiceConnection conn;
    private ChatService chatService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startChatService();
        final EditText edit = (EditText) findViewById(R.id.edit_message);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);

        initEdit(edit);


    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatHolder>{

        @Override
        public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ChatHolder(getLayoutInflater().inflate(R.layout.item_chat,parent,false));
        }

        @Override
        public void onBindViewHolder(ChatHolder holder, int position) {
            ChatMessage chatMessage = chatList.get(position);
            holder.chat_text.setText(chatMessage.message);

        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }
    }

    private class ChatHolder extends RecyclerView.ViewHolder {

        private final TextView chat_text;

        public ChatHolder(View itemView) {
            super(itemView);
            chat_text = (TextView) itemView.findViewById(R.id.chat_text);
        }
    }


    private void initEdit(final EditText edit) {
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND){
                    chatService.sendMessage(edit.getText().toString());
                    Toast.makeText(MainActivity.this, "send", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    private void startChatService() {
        Intent intent = new Intent(this, ChatService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, ChatService.class);
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service instanceof ChatService.ChatBinder) {
                    chatService = ((ChatService.ChatBinder) service).getService();
                    chatService.addChatListener(onChatListener);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                chatService.removeChatListener(onChatListener);
            }
        };
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    }


}
