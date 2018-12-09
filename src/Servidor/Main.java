package Servidor;

import Cliente.Client;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main {


    public static void main(String args[]) throws IOException {
        ServerSocket s = new ServerSocket(9998);
        Socket ss;

        while(true){
            ss = s.accept();
            Interface x  = new Interface(ss);
        }

    }
}
