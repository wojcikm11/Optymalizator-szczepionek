package io;

import app.DataControl;
import exception.IdAlreadyExistsException;
import exception.InputDataException;
import exception.InvalidConnectionsException;
import exception.ParticipantNotInFileException;
import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final int PRODUCER = 0;
    private static final int PHARMACY = 1;

    private int line;
    private int[] producersIds;
    private int[] pharmaciesIds;

    private DataControl dataControl;

    public DataControl getDataControl() {
        return dataControl;
    }

    public FileManager() {
        dataControl = new DataControl();
        line = 0;
    }

    private String readLine(BufferedReader bufferedReader) throws IOException {
        line++;
        return bufferedReader.readLine();
    }

    public Participants readData(String fileName) throws NumberFormatException {
        List<Producer> producers = null;
        List<Pharmacy> pharmacies = null;
        List<Connection> connections = null;
        try (
                var fileReader = new FileReader(fileName);
                var reader = new BufferedReader(fileReader);
        ) {
            if (new File(fileName).length() == 0)
                throw new IllegalArgumentException("Podany plik nie może być pusty!");

            int maxLines = 10000;
            if (new File(fileName).length() > maxLines)
                throw new IllegalArgumentException("Podany plik nie może zawierać więcej niż " + maxLines + " " +
                                                   "linijek! Aktualna liczba linijek w pliku: " +
                                                    new File(fileName).length());

            producers = readProducers(reader);
            pharmacies = readPharmacies(reader);
            connections = readConnections(reader, producers, pharmacies);
        } catch (FileNotFoundException e) {
            System.err.println("Nie znaleziono pliku o nazwie " + fileName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Błąd odczytu z pliku " + fileName);
            e.printStackTrace();
            System.exit(1);
        }

        if (!supplyGreaterOrEqualToDemand(producers, pharmacies))
            throw new IllegalArgumentException("Liczba szczepionek produkowanych przez producentów nie może być " +
                    "mniejsza od zapotrzebowania aptek!");

        int insufficientDemandPharmacyid = maxVaccinesLessThanPharmacyDemand(pharmacies, connections);
        if (insufficientDemandPharmacyid != -1)
            throw new IllegalArgumentException("Maksymalna ilość szczepionek, która może zostać dostarczona " +
                    "do apteki o numerze id " + insufficientDemandPharmacyid + " jest " +
                    "mniejsza od jej zapotrzebowania!");

        saveConnections(connections, producers, pharmacies);
        Participants data = new Participants(producers, pharmacies);
        return data;
    }

    private void saveConnections(List<Connection> connections, List<Producer> producers, List<Pharmacy> pharmacies) {
        saveConnectionsInProducers(connections, producers);
        saveConnectionsInPharmacies(connections, pharmacies);
    }

    private void saveConnectionsInPharmacies(List<Connection> connections, List<Pharmacy> pharmacies) {
        for (Pharmacy pharmacy: pharmacies) {
            List<Connection> pharmacyConnections = getConnectionsOfGivenPharmacyId(connections, pharmacy.getId());
            pharmacy.setConnections(pharmacyConnections);
        }
    }

    private List<Connection> getConnectionsOfGivenPharmacyId(List<Connection> connections, int id) {
        return dataControl.getConnectionsOfGivenPharmacyId(connections, id);
    }

    private void saveConnectionsInProducers(List<Connection> connections, List<Producer> producers) {
        for (Producer producer: producers) {
            List<Connection> producerConnections = getConnectionsOfGivenProducerId(connections, producer.getId());
            producer.setConnections(producerConnections);
        }
    }

    private List<Connection> getConnectionsOfGivenProducerId(List<Connection> connections, int id) {
        return dataControl.getConnectionsOfGivenProducerId(connections, id);
    }

    private int maxVaccinesLessThanPharmacyDemand(List<Pharmacy> pharmacies, List<Connection> connections) {
        return dataControl.maxVaccinesLessThanPharmacyDemand(pharmacies, connections);
    }

    private boolean supplyGreaterOrEqualToDemand(List<Producer> producers, List<Pharmacy> pharmacies) {
        return dataControl.supplyGreaterOrEqualToDemand(producers, pharmacies);
    }

    private List<Producer> readProducers(BufferedReader reader) throws IOException, IllegalArgumentException {
        readBlankLines(reader);
        List<Producer> producers = (List<Producer>) readTransactionParticipants(reader, PRODUCER);
        saveProducerIds(producers);
        return producers;
    }

    private void readBlankLines(BufferedReader reader) throws IOException {
        while (readLine(reader).isBlank());
    }

    private List<Pharmacy> readPharmacies(BufferedReader reader) throws IOException, IllegalArgumentException {
        List<Pharmacy> pharmacies = (List<Pharmacy>) readTransactionParticipants(reader, PHARMACY);
        savePharmaciesIds(pharmacies);
        return pharmacies;
    }

    private void saveProducerIds(List<Producer> producers) {
        producersIds = new int[producers.size()];
        for (int i = 0; i < producersIds.length; i++)
            producersIds[i] = producers.get(i).getId();
    }

    private void savePharmaciesIds(List<Pharmacy> pharmacies) {
        pharmaciesIds = new int[pharmacies.size()];
        for (int i = 0; i < pharmaciesIds.length; i++)
            pharmaciesIds[i] = pharmacies.get(i).getId();
    }

    private List<? extends TransactionParticipant> readTransactionParticipants(BufferedReader reader, int type) throws IOException {
        List<TransactionParticipant> transactionParticipants = new ArrayList<>();
        TransactionParticipant transactionParticipant = null;

        while (true) {
            String nextLine = readLine(reader);
            if (nextLine == null || nextLine.stripLeading().startsWith("#"))
                break;

            transactionParticipant = readTransactionParticipant(nextLine, type);

            if (transactionParticipants.contains(transactionParticipant))
                throw new IdAlreadyExistsException("Identyfikatory muszą być unikalne! Duplikacja id w linii " + line);

            transactionParticipants.add(transactionParticipant);
        }
        String message;
        if (transactionParticipants.size() == 0) {
            message = type == PRODUCER ? "W pliku nie znaleziono żadnych producentów." :
                                         "W pliku nie znaleziono żadnych aptek.";

            throw new ParticipantNotInFileException(message);
        }
        return transactionParticipants;
    }

    private TransactionParticipant readTransactionParticipant(String currentLine, int type) {
        int attributesNumber = 3;
        String[] attributes = getAttributes(currentLine, attributesNumber);
        int id;
        String name;
        int productionOrDemand;

        try {
            id = Integer.parseInt(attributes[0].strip());
            productionOrDemand = Integer.parseInt(attributes[2].strip());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Błąd w danie liczbowej w linijce " + line);
        }

        name = attributes[1].strip();
        if (name.contains("|"))
            throw new IllegalArgumentException("Nazwa producenta ani nazwa apteki nie może zawierać znaku \"|\". " +
                    "Błąd w linijce " + line);


        if (id < 0 || productionOrDemand < 0)
            throw new IllegalArgumentException("Wartości liczbowe nie mogą być ujemne! Błąd w linijce " + line);

        if (type == PRODUCER)
            return new Producer(id, name, productionOrDemand);
        else if (type == PHARMACY)
            return new Pharmacy(id, name, productionOrDemand);
        return null;
    }

    private List<Connection> readConnections(BufferedReader reader, List<Producer> producers,
                                             List<Pharmacy> pharmacies) throws IOException {

        List<Connection> connections = new ArrayList<>();
        Connection connection = null;

        while (true) {
            String nextLine = readLine(reader);
            if (nextLine == null)
                break;

            connection = readConnection(nextLine, producers, pharmacies);

            if (connections.contains(connection))
                throw new IdAlreadyExistsException("Zduplikowane połączenia! Zduplikowane identyfikatory " +
                        "producenta i apteki w linii " + line);
            connections.add(connection);
        }
        int connectionsNumber = producersIds.length * pharmaciesIds.length;
        if (connections.size() != connectionsNumber)
            throw new IllegalArgumentException("Nieprawidłowa liczba połączeń! Połączeń w pliku powinno " +
                    "być " + connectionsNumber);
        if (!allCombinationsOfConnectionsExistInFile(connections, connectionsNumber))
            throw new InvalidConnectionsException("Błąd w połączeniach - w pliku powinny znaleźć się wszystkie " +
                    "możliwe kombinacje połączeń producentów i aptek");

        return connections;
    }

    private boolean allCombinationsOfConnectionsExistInFile(List<Connection> connections, int connectionsNumber) {
        int producerIdToCheck;
        int pharmacyIdToCheck;
        boolean combinationFound = false;

        for (int i = 0; i < connectionsNumber; i++) {
            combinationFound = false;
            producerIdToCheck = connections.get(i).getProducer().getId();
            pharmacyIdToCheck = connections.get(i).getPharmacy().getId();
            for (int j = 0; j < producersIds.length; j++) {
                if (combinationFound)
                    break;
                for (int k = 0; k < pharmaciesIds.length; k++) {
                    if (producersIds[j] == producerIdToCheck && pharmaciesIds[k] == pharmacyIdToCheck) {
                        combinationFound = true;
                        break;
                    }
                }
            }
            if (!combinationFound)
                return false;
        }
        return true;
    }

    private Connection readConnection(String currentLine, List<Producer> producers, List<Pharmacy> pharmacies) {
        int attributesNumber = 4;
        String[] attributes = getAttributes(currentLine, attributesNumber);
        int producerId;
        int pharmacyId;
        int maxVaccines;
        double price;

        try {
            producerId = Integer.parseInt(attributes[0].strip());
            pharmacyId = Integer.parseInt(attributes[1].strip());
            maxVaccines = Integer.parseInt(attributes[2].strip());
            price = Double.parseDouble(attributes[3].strip());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Błąd w danie liczbowej w linijce " + line);
        }

        if (producerId < 0 || pharmacyId < 0 || maxVaccines < 0 || price < 0)
            throw new IllegalArgumentException("Wartości liczbowe nie mogą być ujemne! Błąd w linijce " + line);

        Producer producer = getProducerById(producers, producerId);
        Pharmacy pharmacy = getPharmacyById(pharmacies, pharmacyId);

        return new Connection(producer, pharmacy, maxVaccines, price);
    }

    private String[] getAttributes(String currentLine, int attributesNumber) {
        String[] attributes;

        attributes = currentLine.split(" \\| ");
        if (invalidLength(attributes, attributesNumber) || invalidParams(attributes, attributesNumber))
            throw new InputDataException("W podanej linii: " + currentLine + " pewne dane są puste lub nie istnieją. " +
                    "Błąd w linijce " + line);

        return attributes;
    }

    private Producer getProducerById(List<Producer> producers, int id) {
        return dataControl.getProducerById(producers, id, line);
    }

    private Pharmacy getPharmacyById(List<Pharmacy> pharmacies, int id) {
        return dataControl.getPharmacyById(pharmacies, id, line);
    }

    private boolean invalidLength(String[] attributes, int attributesNumber) {
        return attributes.length != attributesNumber;
    }

    private boolean invalidParams(String[] attributes, int attributesNumber) {
        for (int i = 0; i < attributesNumber; i++) {
            if (attributes[i] == null || attributes[i].isBlank())
                return true;
        }
        return false;
    }
}
