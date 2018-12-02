package Servidor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ServersToRent {
    Map<String, List<ServerToRent>> busy;
    Map<String, List<ServerToRent>> notBusy;
    Map<String, List<ServerToRent>> auctioned;
}

class ServerToRent{
    String type;
    Float cost;
    Integer currentOwner;
    LocalDate start;
}