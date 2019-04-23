package com.example.gmx15.phonemaster.utilities;

import com.example.gmx15.phonemaster.accessibility_service.MyAccessibilityService;

import java.io.IOException;
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
            String ip = "183.173.110.140";
            int port = 8000;
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