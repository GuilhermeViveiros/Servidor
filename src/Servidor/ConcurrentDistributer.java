package Servidor;


import javafx.util.Pair;


public class ConcurrentDistributer {


    private static int help = 0;


    //distribui os servers requesitados pelos clientes
    static void distribute_servers() {
        Thread x = new Thread() { //thread que trata dos servers a serem requesitados
            public void run() {
                while (!ClientData.getRequired_servers().isEmpty()) {

                    Server sv;

                    for (String x : ClientData.getRequired_servers().keySet()) {//vai a todos os tipos de server requesitados pelos users

                        while ((sv = ServerData.checkAnyServer("Requesitado", x)) == null) {//enquanto não houver nenhum server livre ou a leilao do tipo Server_name espera
                            //String sms = "Server do tipo " + x + " esgotado, vai ter que esperar";
                            //ClientData.addClients_msg("all",sms,null);
                            ServerData.await(x);//espera que algum server volte
                            System.out.println("Acordei");

                        }
                        //aqui tenho a certeza que um servidor está livre , neste caso o sv

                        //caso não esteja em uso nem a leilao
                        if (!sv.getInUse()) {//se não estiver em uso
                            sv.TurnOn("Requesitado");
                            ClientData.addAcquiredServer(sv, null);//adiciona o server aos adquiridos de um cliente
                            break;
                        } else { //caso o server esteja em uso e tenha sido leiloado
                            if (sv.getSaleServer().equals("Leiloado")) {//caso o server tenha sido leiloado, passa a não estar
                                for (String Client_email : ClientData.getAcquired_servers().keySet()) {
                                    if (ClientData.getAcquired_servers().get(Client_email).contains(sv)) {//caso este cliente contenha o server
                                        sv.TurnOff();
                                        String sms = "Pedimos desculpa , mas o seu server : " + sv.getName() + " foi requesitado, ficou sem o server";
                                        ClientData.addClients_msg(null, sms, Client_email);
                                        break;
                                    }
                                }
                                ClientData.removeAcquiredServer(sv);
                                sv.TurnOn("Requesitado");
                                ClientData.addAcquiredServer(sv, null);//adiciona o server aos adquiridos de um cliente
                                break;
                            }
                        }
                    }
                }
            }
        };x.start();
    }

    //distribui os servers leiloados pelos clientes
    static void deal_sale_servers(String Server_name) {
        //atribui um cliente a este servidor
        Thread x = new Thread() {

            public void run() {

                Server sv;
                while ((sv = ServerData.checkAnyServer("Leiloado", Server_name)) == null) {//enquanto não houver nenhum server livre do tipo Server_name espera

                    System.out.println("Adormece");
                    ServerData.await(Server_name);//espera que algum server volte
                    System.out.println("Acordei");

                }

                if (ClientData.getSale_servers().containsKey(sv.getName())) {//ver se ainda tem clientes há espera

                    String Client_email = "";//cliente name para o cliente que der mais pelo server
                    Float tmp = 0F;
                    for (Pair<String, Float> aux : ClientData.getSale_servers().get(sv.getName())) { //vou ver quem dá mais
                        if (aux.getValue() > tmp) {
                            tmp = aux.getValue();
                            Client_email = aux.getKey(); //Cliente que dá mais dinheiro pelo server
                        }
                    }

                    sv.TurnOn("Leiloado");
                    ClientData.addAcquiredServer(sv, Client_email); //adiciona o server ao cliente
                    String temp = ("Cliente " + Client_email + " ganhou o leiloamento do tipo do server : " + sv.getName());
                    System.out.println(temp);
                    ClientData.addClients_msg("all", temp, Client_email);//manda mensagem para todos os clientes
                    ClientData.removeSaleServer(Server_name);//remove o server x do leilao porque já tem cliente
                }
            }
        };x.start();
    }

}

