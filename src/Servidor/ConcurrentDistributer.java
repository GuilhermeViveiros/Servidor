package Servidor;


import javafx.util.Pair;


public class ConcurrentDistributer {


    private static int help = 0;


    //distribui os servers pelos clientes
    public static void distribute_servers() {
        Thread x = new Thread() { //thread que trata dos servers a serem requesitados
            public void run() {
                while (!ClientData.getRequired_servers().isEmpty()) {


                    //                  FALTA ME POR PARA OS CLIENTES ESPERAREM CASO NAO EXISTA SERVERS DISPONIVEIS

                    for (String x : ClientData.getRequired_servers().keySet()) {//vai a todos os tipos de server requesitados pelos users

                        for (Server sv : ServerData.getServers().get(x)) {//verifica se um dos servers do determinado tipo em questão está em uso

                            // ESTA PARTE È CASO O SERVER ESTEJA LEILOADO FICA A REQUESITADO

                            if (!sv.getInUse()) {//se não estiver em uso
                                sv.TurnOn("Requesitado");
                                ClientData.addAcquiredServer(sv, null);//adiciona o server aos adquiridos de um cliente
                                break;
                            } else {
                                if (sv.getSaleServer().equals("Leiloado")) {//caso o server tenha sido leiloado, passa não estar
                                    sv.TurnOn("Requesitado");
                                    // TIRA UM SERVER DOS REQUESITOS DE UM GAJO;
                                    //FALTA AVISAR O CLIENTE QUE FICOU SEM O SERVER
                                    ClientData.removeAcquiredServer(sv);//remove o server de um cliente
                                    ClientData.addAcquiredServer(sv, null);//adiciona o server aos adquiridos de um cliente
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
        x.start();
    }

    public static void deal_sale_servers(String Server_name) {
        //atribui um cliente a este servidor
        Thread x = new Thread() {

            public void run() {

                Server sv;
                while ((sv = ServerData.checkAnyServer(Server_name)) == null) {//enquanto não houver nenhum server livre do tipo Server_name espera

                    System.out.println("Adormece");
                    ServerData.await(Server_name);//espera que algum server volte
                    System.out.println("Acordei");

                }

                String Client_email = "";//cliente name para o cliente que der mais pelo server
                Float tmp = 0F;
                for (Pair<String, Float> aux : ClientData.getSale_servers().get(sv.getName())) { //vou ver quem dá mais
                    if (aux.getValue() > tmp) {
                        tmp = aux.getValue();
                        System.out.println("VAMOS LA VERRRRRRR  +" + aux.getKey() + "   " + aux.getValue());
                        Client_email = aux.getKey(); //Cliente que dá mais dinheiro pelo server
                    }
                }

                System.out.println("Encontrei 1 -> "+ sv.getName());
                sv.TurnOn("Leiloado");
                ClientData.addAcquiredServer(sv, Client_email); //adiciona o server ao cliente
                System.out.println("FIQUEI COM O SERVER " + sv.getName() + " " + Client_email);
                ClientData.addClients_msg(Client_email, sv.getName());//informa qual o cliente que ganhou aquele tipo de server

                //ClientData.removeSaleServer(x);//remove o server x do leilao porque já tem cliente


            }
        };x.start();
    }

}

