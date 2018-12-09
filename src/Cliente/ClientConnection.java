package Cliente;

import Servidor.ConcurrentDistributer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection {
    // Nova classe entre cliente e sistema

    private Client c;
    private PrintWriter pw;
    private BufferedReader br;
    private Socket s;
    private BufferedReader sin;


    public ClientConnection(Client c, Socket s) throws IOException {
        this.c = c;
        this.s = s;
        this.pw = new PrintWriter(s.getOutputStream(), true);
        this.br = new BufferedReader(new InputStreamReader(System.in));
        this.sin = new BufferedReader(new InputStreamReader(s.getInputStream()));


        Thread readTerminal = new Thread() {
            public void run() {
                String current;
                try {
                    while ((current = br.readLine()) != null) {
                        System.out.println(current);
                        pw.println(current);
                    }

                    br.close();
                    sin.close();
                    pw.close();
                    s.close();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };readTerminal.start();


        Thread readServer = new Thread() {
            public void run() {
                String current;
                try {
                    while ((current = sin.readLine()) != null) {

                        System.out.println(current);
                    }

                    br.close();
                    sin.close();
                    pw.close();
                    s.close();



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };readServer.start();

    }

}
