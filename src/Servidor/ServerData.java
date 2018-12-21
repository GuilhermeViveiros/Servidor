package Servidor;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ServerData {
    //mapa de servidores do sistema
    private static Map<String,List<Server>> servers = new HashMap<>();
    private static ReentrantLock lock = new ReentrantLock();
    private static Map<String,Condition> servers_conditions = new HashMap<>();

    static{ //meter vários servidores de um TIPO
        servers_conditions.put("t3Server",lock.newCondition());
        List<Server> t3Server = new ArrayList<>();
        servers_conditions.put("m5Server",lock.newCondition());
        List<Server> m5Server = new ArrayList<>();
        List<Server> f4Server = new ArrayList<>();
        servers_conditions.put("f4Server",lock.newCondition());

        t3Server.add(new ServerGenerator("t3Server",20F,1));
        m5Server.add(new ServerGenerator("m5Server",10F,2));
        m5Server.add(new ServerGenerator("m5Server",10F,3));
        f4Server.add(new ServerGenerator("f4Server",2F,4));
        //f4Server.add(new ServerGenerator("f4Server",2F,5));
        //f4Server.add(new ServerGenerator("f4Server",3F,6));

        servers.put("t3Server",t3Server);
        servers.put("m5Server",m5Server);
        servers.put("f4Server",f4Server);

    }

    public static String showServers(){
        String current = "";
        for(String x:servers.keySet()){
            current = current + "Tipo: " + x + " e Preço por hora: " + servers.get(x).get(0).getCost() + "\n";
        }

        return current;
    }

    public static void await(String x){
                try
                {
                    lock.lock();
                    System.out.println("Siga dormir " + x);

                        try {
                            servers_conditions.get(x).await();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                } finally

                {
                    lock.unlock();
            }
    }

    public static void signal(String x){
        Thread a = new Thread() {
            public void run() {
                try {
                    lock.lock();

                    servers_conditions.get(x).signal();

                }finally {
                    lock.unlock();
                }
            }

        };a.start();

    }

    //verifica se algum server do tipo Server_name está livre, devolve o primeiro server a encontrar livre
    //variavel aux ajuda na determinação do server , porque se quisermos requesitar um server podemos passar por cima dos leiloados
    public static Server checkAnyServer(String aux , String Server_name){
        try {
            lock.lock();

            for (Server x : servers.get(Server_name)) {
                if(aux.equals("Requesitado")) {
                    if (x.getInUse() == false || x.getSaleServer().equals("Leiloado")) {
                        return x;
                    }
                }
                if(aux.equals("Leiloado")){
                    if(x.getInUse() == false){
                        return  x;
                    }
                }

            }
        }finally {
            lock.unlock();
        }

        return null;
    }

    //Getter e Setter
    public static Map<String, List<Server>> getServers() {
        return servers;
    }

    public static void setServers(Map<String, List<Server>> servers) {
        ServerData.servers = servers;
    }
}
