package Servidor;

import Cliente.Client;

import java.io.*;
import java.net.Socket;
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

    //le as mensagens relacionados com os algoritmos do servidor
    public void answers(){
        Thread readMsg = new Thread(){
            public void run() {

                while (ClientData.getClients_msg().get(c.getEmail())==null) {//enquanto não tiver mensagem
                    //espera para o 1 caso
                    try {//aqui não existem mensagens
                        System.out.println("Acabaram as mensagens");

                        ClientData.await(c.getEmail());

                        System.out.println("As mensagenes voltaram");
                        //aqui já existem e serão removidas
                        ClientData.getClients_msg().get(c.getEmail()).forEach(s1 -> pw.println(s1));//imprime todas as mensagens
                        ClientData.getClients_msg().put(c.getEmail(), null);//apaga todas as mensagens

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };readMsg.start();
    }


    //está resultados para as respostas dos utilizadores
    public void write(String x) throws IOException ,InterruptedException{
        try {
            this.lock.lock();
            System.out.println("Received: \""  + x + "\" from " + c.getEmail());
            this.messages = x.split(" ");
            System.out.println(messages.length);

            switch (messages[0]) {
                case "Login":
                    if(messages.length == 1)
                        pw.println(Login());
                case "Registar":
                    if (messages.length == 4) {
                        if((this.c = ClientData.addUser(messages[1], messages[2], messages[3]))!=null){//itentificação estar correta
                            System.out.println("nome , email , pass " + messages[1] + " " + messages[2] + " " + messages[3]);
                            answers();
                            pw.println(Services());
                        }else{
                            pw.println("\nDigitos errados , email já está a ser utilizador.Tente novamente\n\n");
                        }
                    }

                    else {
                        pw.println("\n$Request - Digite o seu nome \n$Request - Digite o seu email \n$Request - Digite a sua password\n$SendReply");
                    }
                    break;
                case "Autenticar":
                    if (messages.length > 1) {
                        if((this.c = ClientData.Autentication(messages[1], messages[2]))!=null){//itentificação estar correta
                            answers();
                            pw.println(Services());
                        }else{
                            pw.println("\nDados errados.\n\n" + Login());
                        }
                    }
                    else pw.println("\n$Request - Digite o seu email\n$Request - Digite a sua password\n$SendReply");
                    break;

                case "Servidores":
                    pw.println(ServerData.showServers());
                    pw.println(Services());
                    break;

                case ("Requisitar"):
                    if (messages.length == 2) {
                        if(!ClientData.RequestServer(c.getEmail(),messages[1])){
                            pw.println("\nDigite um tipo de server correto!\n\n" + Services());
                        }else{
                            ConcurrentDistributer.distribute_servers();//começa a distribuir os servers
                            System.out.println(messages[1]);
                            pw.println(Services());
                        }
                    }else pw.println("\n"+ServerData.showServers()+"\n" +
                                     "$Request - Qual o servidor que pretende requesitar?\n" +
                                     "$SendReply\n");
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
                            pw.println("\nTipo/Id do server errados.Tente novamente.\n" + Services());
                        }
                        else {
                            pw.println("Removido");
                            pw.println(Services());
                        }
                    }
                    else pw.println("\n$Request - Digite o tipo do server\n$Request - id do server\n$SendReply\n");
                    break;

                case "Leave_Program" :
                    pw.println("\nVolte Sempre!!!\n$Exit\n");
                    s.shutdownOutput();
                    break;

                case "Leiloar_Server":
                    if(messages.length == 3){
                        if(!ClientData.SaleServer(c.getEmail() , messages[1],(float)Integer.parseInt(messages[2]))){
                            pw.println("\nDigite um server válido.\n" + Services());
                        }else {
                            aux = messages[1];
                            pw.println(Services());
                        }
                    }else pw.println("\n$Request - Digite qual o server que pretende leiloar\n$Request - Quanto deseja pagar pelo mesmo?\n$SendReply\n");

                    break;
                default:
                    pw.println("\nUps!Serviço desconhecido: \n Utilize <optionNumber> ou coloque os dados pedidos caso necessário");
                    break;

            }
        }finally {
            this.lock.unlock();
        }

    }


    public String Login(){
        return ( "$Clear\nBem Vindo !\n" +
                "$Registar - Registar\n" +
                "$Autenticar - Autenticar\n"
        );
    }

    /**
     * Informa sobre os serviços presentes no sistema
     * @return
     */
    public static String Services(){
        return ( "$Clear\nServiços disponíveis no sistema : \n\n" +
                "$Servidores - Servidores\n" +
                "$Requisitar - Requesitar\n" +
                "$MyServers - MyServers\n" +
                "$Saldo - Saldo\n" +
                "$Leave - Leave\n" +
                "$Leiloar_Server - Leiloar_Server\n" +
                "$Leave_Program - Leave_Program\n"
        );
    }

}
