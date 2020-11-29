package app;

import exception.IdAlreadyExistsException;
import exception.InputDataException;
import exception.InvalidConnectionsException;
import io.FileManager;
import io.ResultPrinter;
import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionsControl {

    private ResultPrinter resultPrinter;
    private FileManager fileManager;
    private DataControl dataControl;

    private List<VAMProducer> producers;
    private List<VAMPharmacy> pharmacies;

    public TransactionsControl(String fileName) {
        try {
            fileManager = new FileManager();
            resultPrinter = new ResultPrinter();
            dataControl = fileManager.getDataControl();
            Participants participants = fileManager.readData(fileName);
            initializeLists(participants);
        } catch (IdAlreadyExistsException | IllegalArgumentException | InputDataException
                | InvalidConnectionsException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void initializeLists(Participants participants) {
        List<Producer> producersList = participants.getProducers();
        List<Pharmacy> pharmaciesList = participants.getPharmacies();

        this.producers = new ArrayList<>();
        this.pharmacies = new ArrayList<>();

        for (Producer producer : producersList)
            producers.add(new VAMProducer(producer));

        for (Pharmacy pharmacy : pharmaciesList)
            pharmacies.add(new VAMPharmacy(pharmacy));
    }

    public void minimizeAndPrintCosts() {
        while(!allPharmaciesHaveDemandsProvided()) {
            calculateProducersVAMValues();
            calculatePharmaciesVAMValues();
            sellVaccines();
        }
        resultPrinter.printTransactions(pharmacies);
    }

    private boolean allPharmaciesHaveDemandsProvided() {
        for (VAMPharmacy pharmacy : pharmacies) {
            if (pharmacy.getPharmacy().getVaccinesAmount() < pharmacy.getPharmacy().getDemand())
                return false;
        }
        return true;
    }

    private void calculatePharmaciesVAMValues() {
        for (VAMPharmacy pharmacy : pharmacies) {
            if (pharmacy.getPharmacy().getVaccinesAmount() < pharmacy.getPharmacy().getDemand()) {
                List<Double> availablePharmacyConnectionsCosts = new ArrayList<>();
                for (Connection connection : pharmacy.getPharmacy().getConnections()) {
                    if (connection.getVaccinesSold() <= connection.getMaxVaccines())
                        availablePharmacyConnectionsCosts.add(connection.getPrice());
                }
                setVAMValues(pharmacy, availablePharmacyConnectionsCosts);
            } else {
                pharmacy.setVamValue(0);
            }
        }
    }

    private void calculateProducersVAMValues() {
        for (VAMProducer producer : producers) {
            if (producer.getProducer().getStock() > 0) {
                List<Double> availableProducerConnectionsCosts = new ArrayList<>();
                for (Connection connection : producer.getProducer().getConnections()) {
                    if (connection.getVaccinesSold() <= connection.getMaxVaccines())
                        availableProducerConnectionsCosts.add(connection.getPrice());
                }
                setVAMValues(producer, availableProducerConnectionsCosts);
            } else
                producer.setVamValue(0);
        }
    }

    private void setVAMValues(VAMModel vamModel, List<Double> availableConnectionsCosts) {
        Collections.sort(availableConnectionsCosts);
        if (availableConnectionsCosts.size() > 1)
            vamModel.setVamValue(availableConnectionsCosts.get(1) - availableConnectionsCosts.get(0));
        else if (availableConnectionsCosts.size() == 1)
            vamModel.setVamValue(availableConnectionsCosts.get(0));
        else
            vamModel.setVamValue(0);
    }

    private VAMProducer getProducerWithHighestVAMValue() {
        VAMProducer highestVamProducer = producers.get(0);
        for (VAMProducer producer : producers) {
            if (producer.getVamValue() > highestVamProducer.getVamValue())
                highestVamProducer = producer;
        }
        return highestVamProducer;
    }

    private VAMPharmacy getPharmacyWithHighestVAMValue() {
        VAMPharmacy highestVamPharmacy = pharmacies.get(0);
        for (VAMPharmacy pharmacy : pharmacies) {
            if (pharmacy.getVamValue() > highestVamPharmacy.getVamValue())
                highestVamPharmacy = pharmacy;
        }
        return highestVamPharmacy;
    }

    private void sellVaccines() {
        VAMProducer producerHighestVam = getProducerWithHighestVAMValue();
        VAMPharmacy pharmacyHighestVam = getPharmacyWithHighestVAMValue();

        if (producerHighestVam.getVamValue() >= pharmacyHighestVam.getVamValue())
            sellVaccinesProducer(producerHighestVam);
        else
            sellVaccinesPharmacy(pharmacyHighestVam);
    }

    private void sellVaccinesProducer(VAMProducer producer) {
        List<Connection> producerConnections = producer.getProducer().getConnections();
        computeConnections(producerConnections);
    }

    private void computeConnections(List<Connection> producerConnections) {
        Connection cheapestConnectionPossible = getCheapestConnectionPossible(producerConnections);
        int maxAmountPossibleToSell = Math.min(cheapestConnectionPossible.getPharmacy().getDemand() -
                        cheapestConnectionPossible.getPharmacy().getVaccinesAmount(),
                cheapestConnectionPossible.getProducer().getStock());

        maxAmountPossibleToSell = Math.min(maxAmountPossibleToSell, cheapestConnectionPossible.getMaxVaccines());
        cheapestConnectionPossible.getProducer().subtractStock(maxAmountPossibleToSell);
        cheapestConnectionPossible.getPharmacy().addVaccines(maxAmountPossibleToSell);
        cheapestConnectionPossible.addVaccinesSold(maxAmountPossibleToSell);
    }

    private Connection getCheapestConnectionPossible(List<Connection> connections) {
        Collections.sort(connections);
        for (Connection connection : connections) {
            if (connectionValid(connection))
                return connection;
        }
        return null;
    }

    private boolean connectionValid(Connection connection) {
        return connection.getVaccinesSold() < connection.getMaxVaccines()
                && connection.getPharmacy().getVaccinesAmount() < connection.getPharmacy().getDemand()
                && connection.getProducer().getStock() > 0;
    }

    private void sellVaccinesPharmacy(VAMPharmacy pharmacy) {
        List<Connection> pharmacyConnections = pharmacy.getPharmacy().getConnections();
        computeConnections(pharmacyConnections);
    }
}

