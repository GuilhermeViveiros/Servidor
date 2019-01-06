package Cliente;

import Servidor.ServerData;
import javafx.util.Pair;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import static java.lang.Thread.sleep;

public class Testing {
    private static Map<String, Pair<String, String>> clientes = new HashMap<>();
    private static Map<String, Stack<String>> actions = new HashMap<>();
    private static Map<String, PrintWriter> pw = new HashMap<>();


    private static void prepareRequisitar1(){
        for(String user: clientes.keySet()){
            actions.get(user).push("Requisitar f4Server");
        }
    }

    private static void prepareRequisitar2(){
        for(String user: clientes.keySet()){
            actions.get(user).push("Requisitar f4Server");
            actions.get(user).push("Requisitar m5Server");
        }
    }

    private static void prepareLeaveAllServers(){
        for(String user: clientes.keySet()){
            actions.get(user).push("Leave f4Server 4");
            actions.get(user).push("Leave t3Server 1");
            actions.get(user).push("Leave m5Server 2");
            actions.get(user).push("Leave m5Server 3");
        }
    }

    private static void prepareRegistar(){
        for(String user : actions.keySet())
            actions.get(user).push("Registar " + user + " " + clientes.get(user).getKey() + " " + clientes.get(user).getValue());

    }

    private static void prepareAutenticar(){
        for(String user : actions.keySet())
            actions.get(user).push("Autenticar " + clientes.get(user).getKey() + " " + clientes.get(user).getValue());
    }

    private static void custom(){
        for(String user : actions.keySet()){
            prepareLeaveAllServers();
            actions.get(user).push("Leiloar_Server m5Server 10");
            actions.get(user).push("Requisitar m5Server");
        }
    }


    private static void initClientes(){
        clientes.put("Zé", new Pair<>("1","123"));
        clientes.put("Carlos", new Pair<>("2","123"));
        clientes.put("Daniel", new Pair<>("3","123"));
        clientes.put("Gui", new Pair<>("5","123"));
        clientes.put("José", new Pair<>("6","123"));


        for(String user : clientes.keySet()){
            Stack<String> actionsOfCliente = new Stack<>();
            actions.put(user, actionsOfCliente);
        }


    }

    public static void main(String[] args) throws Exception{
        initClientes();

        for(String user: clientes.keySet()){
            Socket s = new Socket("localhost",9998);
            new ClientConnection(new Client(),s);
            pw.put(user, new PrintWriter(s.getOutputStream(), true));
        }

        prepareLeaveAllServers();
        prepareAutenticar();
        for(int i = 0; i < actions.get("Zé").size(); i++)
            for(String user: actions.keySet()){
                if(!actions.get(user).empty()){
                    String action = actions.get(user).pop();
                    pw.get(user).println(action);
                }
            }
    }
}
