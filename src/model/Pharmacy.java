package model;

import java.util.List;

public class Pharmacy extends TransactionParticipant implements Comparable<Pharmacy> {
    private int demand;
    private int vaccinesAmount;
    private List<Connection> connections;

    public Pharmacy(int id, String name, int demand) {
        super(id, name);
        this.demand = demand;
        this.vaccinesAmount = 0;
    }

    public int getDemand() {
        return demand;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void addVaccines(int vaccines) {
        vaccinesAmount += vaccines;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public int getVaccinesAmount() {
        return vaccinesAmount;
    }

    public void setVaccinesAmount(int vaccinesAmount) {
        this.vaccinesAmount = vaccinesAmount;
    }

    @Override
    public int compareTo(Pharmacy p) {
        return Integer.compare(getId(), p.getId());
    }
}