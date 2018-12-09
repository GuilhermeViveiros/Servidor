package Servidor;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

public interface Server {

    public String getName();
    public Float getCost();
    public String Info();
    public Calendar getTime();
    public Integer getServerId();
    public boolean getInUse();
    public void TurnOn(String x);
    public String getSaleServer();//Se é requesitado ou leiloado
    public void TurnOff();

}

