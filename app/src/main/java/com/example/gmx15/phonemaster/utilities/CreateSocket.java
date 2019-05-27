package com.example.gmx15.phonemaster.utilities;

import java.net.Socket;

public class CreateSocket extends Thread {
    public Socket sck;

    @Override
    public void run() {
        try {
            String ip = "183.173.108.75";
            int port = 8000;
            this.sck = new Socket(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
