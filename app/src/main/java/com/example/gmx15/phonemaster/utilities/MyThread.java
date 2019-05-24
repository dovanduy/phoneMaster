package com.example.gmx15.phonemaster.utilities;

import java.io.OutputStream;
import java.net.Socket;


public class MyThread extends Thread {
    private String payload;
    private Socket sck;

    public MyThread(String payload, Socket sck)
    {
        this.payload = payload;
        this.sck = sck;
    }

    @Override
    public void run() {
        try {
            byte[] bstream = this.payload.getBytes();
            OutputStream os = sck.getOutputStream();
            os.write(bstream);
//            sck.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}