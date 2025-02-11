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
    private volatile BufferedReader sin;
    private volatile Map<String,String> opts = new HashMap<>();
    private String lastCmd;

    private volatile Boolean requestingInput;

    private ReentrantLock lock;
    private Condition con;



    public ClientConnection(Client c, Socket s) throws IOException {
        this.c = c;
        this.s = s;
        this.pw = new PrintWriter(s.getOutputStream(), true);
        this.br = new BufferedReader(new InputStreamReader(System.in));
        this.sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.requestingInput = false;
        lock = new ReentrantLock();
        con = lock.newCondition();

        Thread readTerminal = new Thread() {
            public void run() {
                    String current;
                    try {
                        while ((current = br.readLine()) != null) {
                            current = filterSpacesFrom(current);
                            if (requestingInput) {
                                /*if(current.equals("quit") && (opts.get(lastCmd).equals("Registar") || opts.get(lastCmd).equals("Autenticar"))){
                                    while ((current = sin.readLine()) != null) {
                                        if(current.equals("$SendReply")){
                                            break;
                                        }
                                    }
                                    opts = new HashMap<>();
                                    pw.println("Login");
                                }*/
                                //System.out.println(current.equals("quit"));
                                //System.out.println(lastCmd);
                                try {
                                    lock.lock();
                                    String toAppend = opts.get(lastCmd);
                                    if(toAppend != null) {
                                        toAppend += " " + current;
                                        opts.replace(lastCmd, toAppend);
                                    }
                                    requestingInput = false;
                                    con.signal();
                                }finally {
                                    lock.unlock();
                                }
                                //System.out.println("Full command: " + toAppend);
                            } else if (opts.containsKey(current)) {
                                System.out.println(current);
                                lastCmd = current;
                                pw.println(opts.get(current));
                            } else {
                                System.out.println("Not a valid input, use <optionNumber>");
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


            private String filterSpacesFrom (String s) {
                while(s.contains(" ")){
                    String[] ss = s.split(" ");
                    String res = "";
                    for(String str: ss)
                        res += str;
                    s = res;
                }
                return s;
            }
        };readTerminal.start();



        Thread readServer = new Thread() {

            public void run() {
                String current;
                try {
                    while ((current = sin.readLine()) != null) {
                        readInterpreter(current);
                    }

                    br.close();
                    sin.close();
                    pw.close();
                    s.close();



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void readInterpreter(String message) throws Exception {
                try {
                    lock.lock();
                    if (message.length() == 0) return; //prevenção de erros

                    String output = "";
                    //System.out.println("Message: " +message);
                    if (message.charAt(0) == '$') { // caso seja um comando que o utilizador possa usar
                        String[] subS = message.substring(1).split(" ", 3);
                        opts.put(Integer.toString(opts.size() + 1), subS[0]); // guardar numero correspondente à opçã
                        //System.out.println("Command :" + subS[0]);
                        switch (subS[0]) {
                            case "Request":
                                requestingInput = true;
                                System.out.println(subS[2]);
                                while (requestingInput) {
                                    con.await();
                                }
                                break;
                            case "SendReply":
                                requestingInput = false;
                                pw.println(opts.get(lastCmd));
                                break;
                            case "Clear":
                                opts = new HashMap<>();
                                break;
                            case "Exit":
                                System.exit(0);
                                break;
                            default:
                                output += opts.size() + " - ";
                                output += subS[2];
                                System.out.println(output);
                                break;
                        }
                    } else System.out.println(message);
                }finally {
                    lock.unlock();
                }
            }

        };readServer.start();
    }
}
