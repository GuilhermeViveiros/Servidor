package Servidor;

import Cliente.Client;
import javafx.util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ClientData {
    //mapa de clientes presentes no sistema
    private static Map<String, Client> clients = new HashMap<>();
    //mapa entre Server e Lista de Clientes que o requesitaram
    private static Map<String,LinkedList<String>> required_servers = new HashMap<>();
    //mapa de servers adquiridos pelos clientes -> Cliente , Lista de Server
    private static Map<String,LinkedList<Server>> acquired_servers = new HashMap<>();
    //mapa de server a serem pedidos a leilao , por vários clientes e os seus respetivos valores -> Server , Lista de Clientes
    private static Map<String,LinkedList<Pair<String,Float>>> sale_servers = new HashMap<>();
    //lock do sistema
    private static ReentrantLock lock = new ReentrantLock();
    //mapa de clientes que ganharam um determinado server por leilao -> Server,Cliente
    private static Map<String,String> sale_winners = new HashMap<>();
    //mapa de mensagens para cada cliente
    private static Map<String,LinkedList<String>> clients_msg = new HashMap<>();
    //cada cliente tem uma restrição
    private static Map<String,Condition> clients_contidions = new HashMap<>();




    //autentica um utilizador
    public static Client Autentication(String email,String password) throws IOException {
        try {
            lock.lock();
            if(clients.containsKey(email)){ //caso contenha o email
                if(clients.get(email).getPassword().equals(password)){ //caso a pass do respetivo mail esteja certa
                    return  clients.get(email);
                }
            }
             return null;
        }finally {
            lock.unlock();
        }
    }

    //regista um utilizador
    public static Client addUser(String name,String email,String password) throws IOException{
        try {
            lock.lock();
            if(!clients.containsKey(email)){//caso não contenha mail
                Client c = new Client(name,email,password);
                clients.put(email,c);
                clients_contidions.put(email,lock.newCondition());//adiciona a condição do utilizador
                clients_msg.put(email,null);//adiciona o cliente ao sistema de mensagens
                return c;
            }
            return null;
        }finally {
            lock.unlock();
        }
    }

    //requesita um server para um cliente
    public static boolean RequestServer(String Client_email,String server_name) {
        try { //implementação de lock caso dois users tentem requesitar um server ao mesmo tempo
            lock.lock();
            if (ServerData.getServers().containsKey(server_name)) {
                if (required_servers.keySet().contains(server_name)) {//se o server já foi requesitado acrescenta o user na lista de espera
                    required_servers.get(server_name).add(Client_email);
                } else {
                    //quando está vazio siggnifca que o server não foi requesitado ainda , é logo atribuido
                    LinkedList tmp = new LinkedList<>();
                    tmp.add(Client_email);
                    required_servers.put(server_name, tmp);//caso ninguém tenho requesitado é lhe atribuido o server

                }
                return true;
            } else return false;

        } finally {
            lock.unlock();
        }
    }

    //Adiciona um server aos adquiridos de um cliente
    //Client_email só é usado para quando o server vai a leilao , já recebo qual o cliente que ganhou o server
    //Caso seja requesitado o primeiro cliente a requesitar é o que fica com o server
    public static void addAcquiredServer(Server sv,String Client_email) {
        try {
            lock.lock();
            String server_name = sv.getName();

            //Caso o server tenha sido requesitado
            if(sv.getSaleServer().equals("Requesitado")) {
                String x = required_servers.get(server_name).removeFirst();//remove o cliente da lista

                if (required_servers.get(server_name).isEmpty())
                    required_servers.remove(server_name); //elimina da lista

                //se o cliente já tem server adquiridos adiciono , caso contrario crio uma  nova lista
                if (acquired_servers.containsKey(x)) acquired_servers.get(x).add(sv);
                else {
                    LinkedList<Server> tmp = new LinkedList<>();
                    tmp.add(sv);
                    acquired_servers.put(x, tmp);
                }
            }

            //Caso o server tenha sido leiloado
            if(sv.getSaleServer().equals("Leiloado")) {

                if (acquired_servers.containsKey(Client_email)) acquired_servers.get(Client_email).add(sv);
                else {
                    LinkedList<Server> tmp = new LinkedList<>();
                    tmp.add(sv);
                    acquired_servers.put(Client_email, tmp);
                }
            }

        } finally {
            lock.unlock();
        }
    }


    //mostra os servers de um cliente
    public static String showServers(String Client_email){
        String current = "";
        if(acquired_servers.containsKey(Client_email)){
            int i =0;
           LinkedList<Server> x = acquired_servers.get(Client_email);
           for(Server v : x ){
               current = current + "Servidor "+ (i++) + ": "  + (v.getName()) +  " Id: " + v.getServerId() + "\n";
           }
        }
        return current;
    }

    //todas as dividas de um cliente
    public static float Debts(String Client_email) {
        try{
            lock.lock();
            Float tmp = 0F;
            float horas;
            if(acquired_servers.containsKey(Client_email)) {

                for (Server x : acquired_servers.get(Client_email)) {
                    System.out.println("SERVER MEU + " + x.getName());
                    //milisegundos -> segundos -> minutos -> horas
                    horas = Calendar.getInstance().getTimeInMillis() - x.getTime().getTimeInMillis();
                    horas = horas/1000/60/60;
                    tmp += x.getCost() * horas;
                }
            }
            return tmp;
        }finally {
            lock.unlock();
        }
    }

    //dividas de um cliente para um respetivo server
    public static Float Debts(String Client_email,Integer Server_id){
        try{
            lock.lock();
            Float tmp =0F;
            if(acquired_servers.containsKey(Client_email)) {
                for (Server x : acquired_servers.get(Client_email)) {
                    if (x.getServerId() == Server_id) {
                        //milisegundos -> segundos -> minutos -> horas
                        float horas = (Calendar.getInstance().getTimeInMillis() - x.getTime().getInstance().getTimeInMillis()) / 1000 / 60 / 60;
                        tmp = x.getCost() * horas; //valor a pagar
                        break;
                    }
                }
            }
            return tmp;
        }finally {
            lock.unlock();
        }
    }


    //dado um cliente este pode sair de algum server que este tenha requesitado
    public static Boolean LeaveServer(String Client_email,String Server_name,Integer Server_id){
        try {
            lock.lock();
                for (Server sv : ServerData.getServers().get(Server_name)) {
                    if (sv.getServerId() == Server_id) {
                        try {
                            sv.TurnOff();//poe o server inativo
                            synchronized (Server_name) {
                                ServerData.signal(Server_name);//aviso os clientes que este server está disponível
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        acquired_servers.get(Client_email).remove(sv);//reitra o server dos adquiridos do servidor
                        return true;
                    }
            }
        }finally {
            lock.unlock();
        }
        return false;
    }

    //servidos a serem pedidos a leilao
    public static boolean SaleServer(String Client_email,String Server_name,Float price ) {
        try {
            lock.lock();
            if (ServerData.getServers().containsKey(Server_name)) {//caso o server já esteja a ser pedido para leilao
                if (sale_servers.containsKey(Server_name)) {//caso exista o server já em leilao
                    sale_servers.get(Server_name).add(new Pair(Client_email, price));

                } else {//caso o server nunca tenha sido requesitado , adiciona o server e o cliente na lista
                    LinkedList x = new LinkedList();
                    LinkedList<Pair<String, Float>> l = new LinkedList();
                    l.add(new Pair(Client_email, price));
                    sale_servers.put(Server_name, l);
                }
                System.out.println("Cliente quer comprar server " + Client_email + " " +  Server_name);

                ConcurrentDistributer.deal_sale_servers(Server_name);//trata dos servers a leilao


                return true;
            }
        }finally {
            lock.unlock();
        }
        return false;
    }

    //adiciona uma mensagem a todos os clientes
    public static void addClients_msg(String Client_email, String Server_name){
        try {
            lock.lock();

            String x = ("Cliente " + Client_email + " ganhou o server " + Server_name);


            for(String tmp : clients_msg.keySet()) {

                if (clients_msg.get(tmp)!=null) {//caso o cliente já tenha mensagens
                    clients_msg.get(tmp).add(x);
                    System.out.println("Cliente " + tmp + " recebeu uma mensagem");
                    ClientData.signal(tmp); //avisa que o cliente x já tem mensagens disponíveis

                } else {
                    LinkedList y = new LinkedList();
                    y.add(x);
                    clients_msg.put(tmp, y);
                    ClientData.signal(tmp); //avisa que o cliente x já tem mensagens disponíveis
                }
            }
        }finally {
            lock.unlock();
        }
    }

    //remove um server do leilao
    public static void removeSaleServer(String Server_name){
        try{
            lock.lock();
            sale_servers.remove(Server_name);
        }finally {
            lock.unlock();
        }
    }


    //remove um server de um utilizador
    public static void removeAcquiredServer(Server x){
        try{
            lock.lock();
            for(String Client_email:acquired_servers.keySet()){
                for(Server sv : acquired_servers.get(Client_email)){
                    if (sv.equals(x)){
                        acquired_servers.get(Client_email).remove(sv);
                        break;
                    }
                }
            }
        }finally {
            lock.unlock();
        }
    }

    //await para um cliente
    public static void await(String x) throws InterruptedException{
        try {
            lock.lock();
            clients_contidions.get(x).await();
        }finally {
            lock.unlock();
        }
    }


    public static void signal(String x) {
        try{
            lock.lock();
            clients_contidions.get(x).signal();
        }finally {
            lock.unlock();
        }
    }

    //remove uma mensagem de um determinado cliente
    public static void removeMsg(String Client_email,String sms){
        try {
            lock.lock();
            if(sms == null){//remove todas
                clients_msg.remove(Client_email);
            }else {//remove especifica
                clients_msg.get(Client_email).remove(sms);
            }
        }finally {
            lock.unlock();
        }
    }


    //Getters e Setters ----------------------------------------------------------------------------------------------------------

    public static synchronized Map<String, LinkedList<Pair<String, Float>>> getSale_servers() {
        return sale_servers;
    }

    public static synchronized void  setSale_servers(Map<String, LinkedList<Pair<String, Float>>> sale_servers) {
        ClientData.sale_servers = sale_servers;
    }

    public static synchronized Map<String, String> getSale_winners() {
        return sale_winners;
    }

    public static synchronized void setSale_winners(LinkedHashMap<String, String> sale_winners) {
        ClientData.sale_winners = sale_winners;
    }

    public static synchronized Map<String, Client> getClients() {
        return clients;
    }

    public static synchronized void setClients(Map<String, Client> clients) {
        ClientData.clients = clients;
    }

    public static synchronized Map<String, LinkedList<String>> getRequired_servers() {
        return required_servers;
    }

    public static synchronized void setRequired_servers(Map<String, LinkedList<String>> required_servers) {
        ClientData.required_servers = required_servers;
    }

    public static synchronized Map<String, LinkedList<Server>> getAcquired_servers() {
        return acquired_servers;
    }

    public static synchronized Map<String, LinkedList<String>> getClients_msg() {
        return clients_msg;
    }

    public static synchronized void setClients_msg(Map<String, LinkedList<String>> clients_msg) {
        ClientData.clients_msg = clients_msg;
    }

    public static synchronized void setAcquired_servers(Map<String, LinkedList<Server>> acquired_servers) {
        ClientData.acquired_servers = acquired_servers;
    }

}

