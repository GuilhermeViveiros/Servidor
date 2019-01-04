package Cliente;

import javafx.util.Pair;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Testing {
    private static Map<String, Pair<String, String>> clientes = new HashMap<>();
    private static Map<String, Stack<String>> actions = new HashMap<>();
    private static Map<String, PrintWriter> pw = new HashMap<>();

    private static void initClientes(){
        clientes.put("Zé", new Pair<>("1","123"));
        clientes.put("Carlos", new Pair<>("2","123"));
        clientes.put("Daniel", new Pair<>("3","123"));
        clientes.put("Gui", new Pair<>("5","123"));
        clientes.put("José", new Pair<>("6","123"));


        for(String user : clientes.keySet()){
            Stack<String> actionsOfCliente = new Stack<>();
            //actionsOfCliente.push("Leave_Program");
            actionsOfCliente.push("Requisitar f4Server");
            actionsOfCliente.push("Registar " + user + " " + clientes.get(user).getKey() + " " + clientes.get(user).getValue());
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

        for(int i = 0; i < 3; i++)
            for(String user: actions.keySet()){
                if(!actions.get(user).empty()){
                    String action = actions.get(user).pop();
                    pw.get(user).println(action);
                }
            }


        Socket s = new Socket("localhost",9998);
        new ClientConnection(new Client(),s);
    }
}
