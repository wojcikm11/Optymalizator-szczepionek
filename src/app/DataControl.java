package app;

import exception.InvalidConnectionsException;
import model.Connection;
import model.Pharmacy;
import model.Producer;

import java.util.ArrayList;
import java.util.List;

public class DataControl {
    public int maxVaccinesLessThanPharmacyDemand(List<Pharmacy> pharmacies, List<Connection> connections) {
        for (Pharmacy pharmacy : pharmacies) {
            int pharmacyIdToCheck = pharmacy.getId();
            int maxVaccinesForGivenPharmacy = connections.stream().
                    filter(connection -> connection.getPharmacy().getId() == pharmacyIdToCheck).
                    mapToInt(Connection::getMaxVaccines).sum();

            if (maxVaccinesForGivenPharmacy < pharmacy.getDemand())
                return pharmacyIdToCheck;
        }
        return -1;
    }

    public Producer getProducerById(List<Producer> producers, int id, int line) {
        for (Producer producer: producers) {
            if (producer.getId() == id)
                return producer;
        }

        throw new InvalidConnectionsException("W połączeniach znajduje się id producenta, które nie istnieje " +
                "w podanych producentach! Błąd w linijce " + line);
    }

    public Pharmacy getPharmacyById(List<Pharmacy> pharmacies, int id, int line) {
        for (Pharmacy pharmacy: pharmacies) {
            if (pharmacy.getId() == id)
                return pharmacy;
        }

        throw new InvalidConnectionsException("W połączeniach znajduje się id apteki, która nie istnieje " +
                "w podanych aptekach! Błąd w linijce " + line);
    }

    public boolean supplyGreaterOrEqualToDemand(List<Producer> producers, List<Pharmacy> pharmacies) {
        int supply = producers.stream().mapToInt(Producer::getProduction).sum();
        int demand = pharmacies.stream().mapToInt(Pharmacy::getDemand).sum();

        return supply >= demand;
    }

    public List<Connection> getConnectionsOfGivenProducerId(List<Connection> connections, int producerId) {
        List<Connection> producerConnections = new ArrayList<>();
        for (Connection connection : connections)
            if (connection.getProducer().getId() == producerId)
                producerConnections.add(connection);

        return producerConnections;
    }

    public List<Connection> getConnectionsOfGivenPharmacyId(List<Connection> connections, int pharmacyId) {
        List<Connection> pharmacyConnections = new ArrayList<>();
        for (Connection connection : connections)
            if (connection.getPharmacy().getId() == pharmacyId)
                pharmacyConnections.add(connection);

        return pharmacyConnections;
    }
}
