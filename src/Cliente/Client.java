package Cliente;


import Servidor.ClientData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


public class Client implements User {
    /**
     * Todos os clientes conhecem o sistema
     * Existe uma interface para cliente
     * Caso um cliente queira requesitar servers tem que pedir ao sistema
     */

    //atributos relacionados com o cliente
    private String email;
    private String name;
    private Integer id;
    private String password;


    public Client() {
        this.id = 0;
        this.email = "";
        this.name = "";
        this.password = "";
    }

    public Client(String name,String email,String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Client(Client x){
        this.id = x.getID();
        this.name = x.getName();
        this.email = x.getEmail();
    }

    @Override
    public void setID(int x) {
        this.id=x;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    @Override
    public void setPassword(String x) {
        this.password = x;
    }

    public String getName(){
        return this.name;
    }

    public int getID(){
        return this.id;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }

    public static void main(String args[]){
        try {
            //Versão para correr o servidor noutro pc
            Socket s = new Socket(InetAddress.getLocalHost().getHostAddress(),9998);
            new ClientConnection(new Client(), s);

            //Socket s = new Socket("localhost",9998);

            //new ClientConnection(new Client(),s);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
