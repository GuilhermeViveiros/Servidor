package Servidor;


import javafx.util.Pair;

import java.io.IOException;

public class ConcurrentDistributer {

    public static void distribute_servers() {
        Thread x = new Thread() { //thread que trata dos servers a serem requesitados
            public void run() {
                while (!ClientData.getRequired_servers().isEmpty()) {

                        for (String x : ClientData.getRequired_servers().keySet()) {//vai a todos os tipos de server requesitados pelos users

                            for (Server sv : ServerData.getServers().get(x)) {//verifica se um dos servers do determinado tipo em questão está em uso



                          //  TAMBEM ME FALTA MUDAR AQUI , CASO O SERVER ESTEJA A LEILAO  , PASSA A NAO ESTAR

                                if (!sv.getInUse()) { //caso o server não esteja em uso
                                    sv.TurnOn("Requesitado");

                                    ClientData.addAcquiredServer(sv,null);//adiciona o server aos adquiridos de um cliente
                                    break;
                                }
                            }
                        }
                }
            }
        };x.start();
    }

    public static void deal_sale_servers()  {

        Thread x = new Thread(){

            public void run(){

                try {
                    Thread.sleep(10000);
                }catch (Exception e){
                    e.printStackTrace();
                }

                //enquanto estiverem servers a leilao
                while(!ClientData.getSale_servers().isEmpty())
                    for (String x : ClientData.getSale_servers().keySet()) {//para cada server que está em leilao

                        //esta parte determina qual o cliente que vai ficar com o server leiloado

                        String Client_email = "";//cliente name para o cliente que der mais pelo server
                        Float tmp = 0F;
                        for (Pair<String, Float> aux : ClientData.getSale_servers().get(x)) { //vou ver quem dá mais
                            if (aux.getValue() > tmp) {
                                System.out.println("VAMOS LA VERRRRRRR  +" + aux.getKey() + "   " + aux.getValue());
                                Client_email = aux.getKey(); //caso o cliente de mais dinheiro pelo Server
                            }
                        }

                        //esta parte determina qual o server a por online do especifico tipo de servidor presente no ciclo
                        for (Server sv : ServerData.getServers().get(x)) {
                            if (!sv.getInUse()) {//se não tiver em uso
                                sv.TurnOn("Leiloado");
                                ClientData.addAcquiredServer(sv, Client_email); //caso algum server esteja disponível de um tipo x ele atribui diretamente


                                ClientData.addSale_Winners(Client_email, x);//informa qual o cliente que ganhou aquele tipo de server
                                ClientData.removeSaleServer(x);//remove o server x do leilao porque já tem cliente
                                break;
                            }


                        }
                    }
            }
        };x.start();
    }


}
