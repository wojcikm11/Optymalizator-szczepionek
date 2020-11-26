package model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileData {
    private List<Producer> producers;
    private List<Pharmacy> pharmacies;
    private List<Connection> connections;

    private int[] producersIds;
    private int[] pharmaciesIds;

    public FileData(List<Producer> producers, List<Pharmacy> pharmacies, List<Connection> connections,
                    int[] producersIds, int[] pharmaciesIds) {
        this.producers = producers;
        this.pharmacies = pharmacies;
        this.connections = connections;
        this.producersIds = producersIds;
        this.pharmaciesIds = pharmaciesIds;

        sortLists(this.producers, this.pharmacies, this.connections);
        sortArrays(this.producersIds, this.pharmaciesIds);
    }

    private void sortLists(List<Producer> producers, List<Pharmacy> pharmacies, List<Connection> connections) {
        Collections.sort(producers);
        Collections.sort(pharmacies);
        Collections.sort(connections);
    }

    private void sortArrays(int[] producersIds, int[] pharmaciesIds) {
        Arrays.sort(producersIds);
        Arrays.sort(pharmaciesIds);
    }

    public List<Producer> getProducers() {
        return producers;
    }

    public List<Pharmacy> getPharmacies() {
        return pharmacies;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public int[] getProducersIds() {
        return producersIds;
    }

    public void setProducersIds(int[] producersIds) {
        this.producersIds = producersIds;
    }

    public int[] getPharmaciesIds() {
        return pharmaciesIds;
    }

    public void setPharmaciesIds(int[] pharmaciesIds) {
        this.pharmaciesIds = pharmaciesIds;
    }
}
