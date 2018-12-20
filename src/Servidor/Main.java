package Servidor;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
