package model;

import java.util.Collections;
import java.util.List;

public class Participants {
    private List<Producer> producers;
    private List<Pharmacy> pharmacies;

    public Participants(List<Producer> producers, List<Pharmacy> pharmacies) {
        this.producers = producers;
        this.pharmacies = pharmacies;

        sortLists(this.producers, this.pharmacies);
    }

    private void sortLists(List<Producer> producers, List<Pharmacy> pharmacies) {
        Collections.sort(producers);
        Collections.sort(pharmacies);
    }

    public List<Producer> getProducers() {
        return producers;
    }

    public List<Pharmacy> getPharmacies() {
        return pharmacies;
    }

}

