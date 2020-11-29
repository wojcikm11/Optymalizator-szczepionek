package model;

import java.util.List;

public class Producer extends TransactionParticipant implements Comparable<Producer> {
    private int production;
    private int stock;
    private List<Connection> connections;

    public Producer(int id, String name, int production) {
        super(id, name);
        this.production = production;
        this.stock = production;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public void subtractStock(int vaccinesSold) {
        this.stock -= vaccinesSold;
    }

    public int getProduction() {
        return production;
    }

    public void setProduction(int production) {
        this.production = production;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public int compareTo(Producer p) {
        return Integer.compare(getId(), p.getId());
    }
}
