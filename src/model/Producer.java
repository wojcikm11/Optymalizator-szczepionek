package model;

import java.util.Objects;

public class Producer extends TransactionParticipant implements Comparable<Producer> {
    private int production;
    private int stock;

    public Producer(int id, String name, int production) {
        super(id, name);
        this.production = production;
        this.stock = production;
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
