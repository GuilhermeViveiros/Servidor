package Servidor;

import Cliente.Client;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Interface {

    private Socket s;
    private PrintWriter pw;
    private BufferedReader br;
    private Client c;
    private final ReentrantLock lock = new ReentrantLock();
    private String[] messages;
    private String aux;


    public Interface(Socket s) throws IOException {
        this.s = s;
        this.pw = new PrintWriter(this.s.getOutputStream(),true);
        this.br = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
        this.c = new Client();
        read();
    }

    //está sempre a ler as mensagens do utilizador
    public void read(){
        Thread read = new Thread(){
            public void run() {
                try {
                    String current;
                    pw.println(Login());
                    while ((current=br.readLine())!=null){
                        System.out.println(current);
                        write(current);
                    }

                    br.close();
                    pw.close();
                    s.close();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        read.start();
    }


    //está resultados para as respostas dos utilizadores
    public void write(String x) throws IOException ,InterruptedException{
        try {
            this.lock.lock();

            this.messages = x.split(" ");
            System.out.println(messages.length);

            switch (messages[0]) {

                case "Registar":
                    if (messages.length == 4) {
                        if((this.c = ClientData.addUser(messages[1], messages[2], messages[3]))!=null){//itentificação estar correta
                            System.out.println("nome , email , pass " + messages[1] + " " + messages[2] + " " + messages[3]);
                            pw.println(Services());
                        }else{
                            pw.println("\nDigitos errados , email já está a ser utilizador.Tente novamente\n\n");
                        }
                    }

                    else {
                        pw.println("\nDigite o seu nome , email e password para o serviço Registar");
                    }
                    break;
                case "Autenticar":

                    if (messages.length > 1) {
                        if((this.c = ClientData.Autentication(messages[1], messages[2]))!=null){//itentificação estar correta

                            pw.println(Services());
                        }else{
                            pw.println("\nDigitos errados.Tente novamente\n\n");
                        }
                    }
                    else pw.println("\nDigite o seu email e password para o serviço Autenticar");
                    break;

                case "Servidores":
                    pw.println(ServerData.showServers());
                    pw.println(Services());
                    break;

                case ("Requesitar"):
                    if (messages.length == 2) {
                        if(!ClientData.RequestServer(c.getEmail(),messages[1])){
                            pw.println("\nDigite um tipo de server correto!\n\n");
                        }else{
                            ConcurrentDistributer.distribute_servers();//começa a distribuir os servers
                            pw.println(Services());
                        }
                    }else pw.println("\nQual o servidor que pretende requesitar?\n\n");
                    break;

                case "MyServers":
                    pw.println(ClientData.showServers(c.getEmail()));
                    pw.println(Services());
                    break;

                case "Saldo" :
                    if(messages.length == 2) pw.println("Valor a pagar  = " + ClientData.Debts(c.getEmail(), Integer.parseInt(messages[1])) );
                    else pw.println("Valor a pagar  = " + ClientData.Debts(c.getEmail()));
                    pw.println(Services());
                    break;

                case "Leave" :
                    if(messages.length == 3) {
                        if(!ClientData.LeaveServer(c.getEmail(),messages[1],Integer.parseInt(messages[2]))) {
                            pw.println("\nTipo/Id do server errados.Tente novamente.\n");
                        }
                        else {
                            pw.println("Removido");
                            pw.println(Services());
                        }
                    }
                    else pw.println("\nDigite o tipo e id do server\n");
                    break;

                case "Leave_Program" :
                    pw.println("\nVolte Sempre!!! :D\n\n");
                    s.shutdownOutput();
                    break;

                case "Leiloar_Server":
                    if(messages.length == 3){
                        if(!ClientData.SaleServer(c.getEmail() , messages[1],(float)Integer.parseInt(messages[2]))){
                            pw.println("\nDigite um server válido.\n");
                        }else {
                            aux = messages[1];
                            pw.println(Services());
                        }
                    }else pw.println("\nDigite qual o server que pretende leiloar e o valor disponivel a pagar pelo mesmo:\n");

                    break;
                default:
                    pw.println("\nUps!Serviço desconhecido : \n" +
                            "Importante : Caso deseje aceder a algum serviço digite -> 'Nome do serviço' sem espaços \n" +
                            "             Caso sejam lhe pedido parametros digite -> 'Nome do serviço' sem espaços e os sucessivos'parametros' \n\n");
                    break;

            }

            Thread tmp = new Thread(){
                public void run(){
                    String tmp;
                    while((tmp = ClientData.getWinner(aux))==null){
                    }pw.println("Cliente " + tmp + " ganhou a lotação do server: " + aux);
                }
            };tmp.start();

        }finally {
            this.lock.unlock();
        }

    }


    public String Login(){

        return ( "\nBem Vindo ! :D\n" +
                "Importante : Caso deseje aceder a algum serviço digite -> 'Nome do serviço' sem espaços\n"+
                "             Caso sejam lhe pedido parametros digite -> 'Nome do serviço' sem espaços e os sucessivos'parametros' \n\n" +
                "Serviços iniciais do sistema : \n\n" +
                "Registar\n" +
                "Autenticar\n"
        );
    }

    /**
     * Informa sobre os serviços presentes no sistema
     * @return
     */
    public static String Services(){

        return ( "\nServiços disponíveis no sistema : \n\n" +
                "1)Servidores\n" +
                "2)Requesitar\n" +
                "3)MyServers\n" +
                "4)Saldo\n" +
                "5)Leave\n" +
                "6)Leiloar_Server\n" +
                "7)Leave_Program \n"
        );
    }


}
