package com.example.lab5serverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int SERVER_PORT = 9999;
    private ServerSocket serverSocket;
    private Socket tempClientSocket;
    private Thread serverThread;

    private LinearLayout msgList;
    private Handler handler;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Server");
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }

    // cau hinh cho text
    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Message bi trong>";
        }
        String m = message ;
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(m);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }
    // Hien thi text len list
    public void showMessage(final String message, final int color) {
        handler.post(() -> msgList.addView(textView(message, color)));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_server) {
            removeAllViews();
            showMessage("Server san sang .", Color.BLACK);
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
            return;
        }
        if (view.getId() == R.id.send_data) {
            String msg = edMessage.getText().toString().trim();
            showMessage("Server : " + msg, Color.BLUE);
            sendMessage(msg);
        }
    }

    private void removeAllViews(){
        handler.post(() -> msgList.removeAllViews());
    }

    private void hideStartServerBtn(){
        handler.post(() -> findViewById(R.id.start_server).setVisibility(View.GONE));
    }
// gui messeger
    private void sendMessage(final String message) {
        try {
            if (null != tempClientSocket) {
                new Thread(() -> {
                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out.println(message);
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {
        public void run() {
            Socket socket;
            try {
                hideStartServerBtn();
                serverSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Khong the bat dau Server : " + e.getMessage(), Color.RED);
            }
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMessage("Khong the tim thay Client :" + e.getMessage(), Color.RED);
                    }
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private final Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Khong the ket noi den Client!!", Color.RED);
                return;
            }
            showMessage("Da ket noi Client!!", Color.DKGRAY);
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) try {
                String read = input.readLine();
                if (null == read || "Ngat ket noi".contentEquals(read)) {
                    boolean interrupted = Thread.interrupted();
                    read = "Client Ngat ket noi: " + interrupted;
                    showMessage("Client : " + read, Color.DKGRAY);
                    break;
                }
                showMessage("Client : " + read, Color.DKGRAY);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}