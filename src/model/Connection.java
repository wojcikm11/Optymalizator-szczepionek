package model;

import java.util.Objects;

public class Connection implements Comparable<Connection> {
    private Producer producer;
    private Pharmacy pharmacy;
    private int vaccinesSold;
    private int maxVaccines;
    private double price;

    public Connection(Producer producer, Pharmacy pharmacy, int maxVaccines, double price) {
        this.producer = producer;
        this.pharmacy = pharmacy;
        this.maxVaccines = maxVaccines;
        this.price = price;
        this.vaccinesSold = 0;
    }

    public void addVaccinesSold(int vaccines) {
        this.vaccinesSold += vaccines;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public int getVaccinesSold() {
        return vaccinesSold;
    }

    public void setVaccinesSold(int vaccinesSold) {
        this.vaccinesSold = vaccinesSold;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
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
        return Objects.equals(producer, that.producer) &&
                Objects.equals(pharmacy, that.pharmacy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(producer, pharmacy);
    }

    @Override
    public int compareTo(Connection c) {
        return Double.compare(price, c.getPrice());
    }

    @Override
    public String toString() {
        return producer.getName() + " -> " + pharmacy.getName()
                + " [Koszt = " + vaccinesSold + " * " + price + " = "
                + ((int) (((price * vaccinesSold) + 0.005f) * 100)) / 100f + " zł]";
    }
}