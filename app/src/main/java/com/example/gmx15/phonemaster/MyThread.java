package com.example.gmx15.phonemaster;

import java.io.OutputStream;
import java.net.Socket;

public class MyThread extends Thread {
    private String payload;
    public MyThread(String payload)
    {
        this.payload = payload;
    }

    @Override
    public void run() {
        try {
            String ip = "101.5.118.12";   //服务器端ip地址
            int port = 8000;        //端口号
            Socket sck = new Socket(ip, port);
            byte[] bstream = this.payload.getBytes();
            OutputStream os = sck.getOutputStream();
            os.write(bstream);
            sck.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
