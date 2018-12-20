package Servidor;


import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ServerGenerator implements Server{


    private String name; //name of the server
    private Float cost; //cost of the server per hour
    private Calendar begin; //when the server is inUse the begin start counting
    private Integer serverID; //id of the server
    private boolean inUse; //inform if the server are in use
    private String saleSV;//inform if the server request is by a request or sale
    private ReentrantLock lock;

    public ServerGenerator(String name, Float cost,Integer serverID) {
        this.lock = new ReentrantLock();
        this.name = name;
        this.cost = cost;
        this.begin = Calendar.getInstance();
        this.inUse = false;
        this.serverID = serverID;
    }

    public String getName(){
        return this.name;
    }

    public Float getCost(){
        return this.cost;
    }

    public Calendar getTime(){
        return this.begin;
    }

    public void TurnOff()
    {
        this.inUse = false;
        this.saleSV = "";
    }

    public ReentrantLock getLock() {
        return lock;
    }


    public String getSaleServer() {
        return saleSV;
    }

    public void setSaleServer(String saleSV) {
        this.saleSV = saleSV;
    }

    public void TurnOn(String x) {
        this.begin = Calendar.getInstance();
        this.saleSV = x;
        System.out.println("Servidor feito na hora " + this.begin.getTime());
        this.inUse = true;
    }

    public Integer getServerId() {
        return this.serverID;
    }


    public boolean getInUse() {
        return this.inUse;
    }

    public String Info(){
        return (this.name + " " + this.cost );
    }


}

