package model;

import java.util.Objects;

public class Connection implements Comparable<Connection> {
    private int producerId;
    private int pharmacyId;
    private int maxVaccines;
    private double price;

    public Connection(int producerId, int pharmacyId, int maxVaccines, double price) {
        this.producerId = producerId;
        this.pharmacyId = pharmacyId;
        this.maxVaccines = maxVaccines;
        this.price = price;
    }

    public int getProducerId() {
        return producerId;
    }

    public void setProducerId(int producerId) {
        this.producerId = producerId;
    }

    public int getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(int pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public int getMaxVaccines() {
        return maxVaccines;
    }

    public void setMaxVaccines(int maxVaccines) {
        this.maxVaccines = maxVaccines;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return producerId == that.producerId &&
                pharmacyId == that.pharmacyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(producerId, pharmacyId);
    }

    @Override
    public int compareTo(Connection c) {
        int producerCompare = Integer.compare(producerId, c.getProducerId());
        if (producerCompare != 0)
            return producerCompare;
        return Integer.compare(pharmacyId, c.getPharmacyId());
    }
}