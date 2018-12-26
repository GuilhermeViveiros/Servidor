package Cliente;

import Servidor.ConcurrentDistributer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class  ClientConnection {
    // Nova classe entre cliente e sistema

    private Client c;
    private PrintWriter pw;
    private BufferedReader br;
    private Socket s;
    private BufferedReader sin;
    private Map<String,String> opts = new HashMap<>();
    private String lastCmd;
    private volatile Boolean requestingInput;

    public ClientConnection(Client c, Socket s) throws IOException {
        this.c = c;
        this.s = s;
        this.pw = new PrintWriter(s.getOutputStream(), true);
        this.br = new BufferedReader(new InputStreamReader(System.in));
        this.sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.requestingInput = false;

        Thread readTerminal = new Thread() {
            public void run() {
                String current;
                try {
                    while ((current = br.readLine()) != null) {
                        synchronized (requestingInput){
                            if(requestingInput){
                                String toAppend = opts.get(lastCmd);
                                toAppend += " " + current;
                                opts.replace(lastCmd, toAppend);
                                requestingInput = false;
                                requestingInput.notify();
                                //System.out.println("Full command: " + toAppend);
                            } else
                            if(opts.containsKey(current)){
                                // System.out.println(current);
                                lastCmd = current;
                                pw.println(opts.get(current));
                            } else {
                                System.out.println("Not a valid input, use <optionNumber>");
                            }
                        }
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
                        readInterpreter(current);
                        // System.out.println(current);
                    }

                    br.close();
                    sin.close();
                    pw.close();
                    s.close();



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void readInterpreter(String message) throws Exception{
                if(message.length() == 0) return; //prevenção de erros

                String output = "";
                //System.out.println("Message: " +message);
                if(message.charAt(0) == '$'){ // caso seja um comando que o utilizador possa usar
                    String[] subS = message.substring(1).split(" ", 3);
                    opts.put(Integer.toString(opts.size()+1), subS[0]); // guardar numero correspondente à opçã
                    //System.out.println("Command :" + subS[0]);
                    switch (subS[0]){
                        case "Request":
                            requestingInput = true;
                            System.out.println(subS[2]);
                            while(requestingInput)
                                synchronized(requestingInput){
                                        requestingInput.wait();
                                }
                            break;
                        case "SendReply":
                            requestingInput = false;
                            pw.println(opts.get(lastCmd));
                            break;
                        case "Clear":
                            opts = new HashMap<>();
                            break;
                        default:
                            output += opts.size() + " - ";
                            output += subS[2];
                            System.out.println(output);
                            break;
                    }
                } else System.out.println(message);
            }

        };readServer.start();
    }
}
