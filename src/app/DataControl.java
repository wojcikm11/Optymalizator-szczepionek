package app;

import model.Connection;
import model.Pharmacy;
import model.Producer;

import java.util.List;

public class DataControl {
    public int maxVaccinesLessThanPharmacyDemand(List<Pharmacy> pharmacies, List<Connection> connections) {
        for (Pharmacy pharmacy : pharmacies) {
            int pharmacyIdToCheck = pharmacy.getId();
            int maxVaccinesForGivenPharmacy = connections.stream().
                    filter(connection -> connection.getPharmacyId() == pharmacyIdToCheck).
                    mapToInt(Connection::getMaxVaccines).sum();

            if (maxVaccinesForGivenPharmacy < pharmacy.getDemand())
                return pharmacyIdToCheck;
        }
        return -1;
    }

    public boolean supplyGreaterOrEqualToDemand(List<Producer> producers, List<Pharmacy> pharmacies) {
        int supply = producers.stream().mapToInt(Producer::getProduction).sum();
        int demand = pharmacies.stream().mapToInt(Pharmacy::getDemand).sum();

        return supply >= demand;
    }

    public int getSupplyById(List<Producer> producers, int producerId) {
        for (Producer producer: producers)
            if (producer.getId() == producerId)
                return producer.getProduction();
        return -1;
    }

    public int getDemandById(List<Pharmacy> pharmacies, int pharmacyId) {
        for (Pharmacy pharmacy: pharmacies)
            if (pharmacy.getId() == pharmacyId)
                return pharmacy.getDemand();
        return -1;
    }

    public boolean problemBalanced(List<Producer> producers, List<Pharmacy> pharmacies) {
        int supply = producers.stream().mapToInt(Producer::getProduction).sum();
        int demand = pharmacies.stream().mapToInt(Pharmacy::getDemand).sum();

        return supply == demand;
    }

    public double[][] createVector(List<Connection> connections, int[] producersIds, int[] pharmaciesIds,
                                   boolean isBalanced) {

        int pharmaciesNumber = pharmaciesIds.length;
        int producersNumber = producersIds.length;
        double[][] producersPharmacies;

        if (isBalanced)
            producersPharmacies = new double[producersNumber][pharmaciesNumber];
        else {
            int dummyColumn = 1;
            producersPharmacies = new double[producersNumber][pharmaciesNumber + dummyColumn];
        }

        fillVector(connections, producersPharmacies, producersNumber, pharmaciesNumber);
        return producersPharmacies;
    }

    private void fillVector(List<Connection> connections, double[][] producersPharmacies, int producersNumber,
                             int pharmaciesNumber) {
        int k = 0;
        for (int i = 0; i < producersNumber; i++) {
            for (int j = 0; j < pharmaciesNumber; j++) {
                producersPharmacies[i][j] = connections.get(k).getPrice();
                k++;
            }
        }
    }
}
