package model;

import java.util.Objects;

public class Pharmacy extends TransactionParticipant implements Comparable<Pharmacy> {
    private int demand;
    private int vaccinesNumber;

    public Pharmacy(int id, String name, int demand) {
        super(id, name);
        this.demand = demand;
        this.vaccinesNumber = 0;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public int getVaccinesNumber() {
        return vaccinesNumber;
    }

    public void setVaccinesNumber(int vaccinesNumber) {
        this.vaccinesNumber = vaccinesNumber;
    }

    @Override
    public int compareTo(Pharmacy p) {
        return Integer.compare(getId(), p.getId());
    }
}