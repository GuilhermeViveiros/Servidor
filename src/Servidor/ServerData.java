package Servidor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerData {
    //mapa de servidores do sistema
    private static Map<String,List<Server>> servers = new HashMap<>();

    static{ //meter vários servidores de um TIPO
        List<Server> t3Server = new ArrayList<>();
        List<Server> m5Server = new ArrayList<>();
        List<Server> f4Server = new ArrayList<>();

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

    //Getter e Setter
    public static Map<String, List<Server>> getServers() {
        return servers;
    }

    public static void setServers(Map<String, List<Server>> servers) {
        ServerData.servers = servers;
    }
}
