package io;

import app.DataControl;
import exception.IdAlreadyExistsException;
import exception.InputDataException;
import exception.InvalidConnectionsException;
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

    public FileManager() {
        dataControl = new DataControl();
        line = 0;
    }

    public int getLine() {
        return line;
    }

    public int[] getProducersIds() {
        return producersIds;
    }

    public int[] getPharmaciesIds() {
        return pharmaciesIds;
    }

    private String readLine(BufferedReader bufferedReader) throws IOException {
        line++;
        return bufferedReader.readLine();
    }

    public FileData readData(String fileName) throws NumberFormatException {
        List<Producer> producers = null;
        List<Pharmacy> pharmacies = null;
        List<Connection> connections = null;
        try (
                var fileReader = new FileReader(fileName);
                var reader = new BufferedReader(fileReader);
        ) {
            if (new File(fileName).length() == 0)
                throw new IllegalArgumentException("Podany plik nie może być pusty!");

            producers = readProducers(reader);
            pharmacies = readPharmacies(reader);
            connections = readConnections(reader);
        } catch (FileNotFoundException e) {
            System.err.println("Nie znaleziono pliku o nazwie " + fileName);
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Błąd odczytu z pliku " + fileName);
            e.printStackTrace();
            System.exit(-1);
        }

        if (!supplyGreaterOrEqualToDemand(producers, pharmacies))
            throw new IllegalArgumentException("Liczba szczepionek produkowanych przez producentów nie może być " +
                                               "mniejsza od zapotrzebowania aptek!");

        int insufficientDemandPharmacyid = maxVaccinesLessThanPharmacyDemand(pharmacies, connections);
        if (insufficientDemandPharmacyid != -1)
            throw new IllegalArgumentException("Maksymalna ilość szczepionek, która może zostać dostarczona " +
                                               "do apteki o numerze id " + insufficientDemandPharmacyid + " jest " +
                                               "mniejsza od jej zapotrzebowania!");

        FileData data = new FileData(producers, pharmacies, connections, producersIds, pharmaciesIds);
        return data;
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
            message = type == PRODUCER ? "W pliku nie znaleziono żadnych producentów." : "W pliku nie znaleziono żadnych aptek.";
            throw new IllegalArgumentException(message);
        }
        return transactionParticipants;
    }

    private TransactionParticipant readTransactionParticipant(String currentLine, int type) {
        int attributesNumber = 3;
        String[] attributes = getAttributes(currentLine, attributesNumber);

        int id = Integer.parseInt(attributes[0].strip());
        String name = attributes[1].strip();
        if (name.contains("|"))
            throw new IllegalArgumentException("Nazwa producenta ani nazwa apteki nie może zawierać znaku \"|\". " +
                                               "Błąd w linijce " + line);
        int productionOrDemand = Integer.parseInt(attributes[2].strip());

        if (id < 0 || productionOrDemand < 0)
            throw new IllegalArgumentException("Wartości liczbowe nie mogą być ujemne! Błąd w linijce " + line);

        if (type == PRODUCER)
            return new Producer(id, name, productionOrDemand);
        else if (type == PHARMACY)
            return new Pharmacy(id, name, productionOrDemand);
        return null;
    }

    private List<Connection> readConnections(BufferedReader reader) throws IOException {
        List<Connection> connections = new ArrayList<>();
        Connection connection = null;

        while (true) {
            String nextLine = readLine(reader);
            if (nextLine == null)
                break;

            connection = readConnection(nextLine);

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
            producerIdToCheck = connections.get(i).getProducerId();
            pharmacyIdToCheck = connections.get(i).getPharmacyId();
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

    private Connection readConnection(String currentLine) {
        int attributesNumber = 4;
        String[] attributes = getAttributes(currentLine, attributesNumber);

        int producerId = Integer.parseInt(attributes[0].strip());
        int pharmacyId = Integer.parseInt(attributes[1].strip());
        int maxVaccines = Integer.parseInt(attributes[2].strip());
        double price = Double.parseDouble(attributes[3].strip());

        if (producerId < 0 || pharmacyId < 0 || maxVaccines < 0 || price < 0)
            throw new IllegalArgumentException("Wartości liczbowe nie mogą być ujemne! Błąd w linijce " + line);

        return new Connection(producerId, pharmacyId, maxVaccines, price);
    }

    private String[] getAttributes(String currentLine, int attributesNumber) {
        String[] attributes;

        attributes = currentLine.split(" \\| ");
        if (invalidLength(attributes, attributesNumber) || invalidParams(attributes, attributesNumber))
            throw new InputDataException("W podanej linii: " + currentLine + " pewne dane są puste lub nie istnieją. " +
                                         "Błąd w linijce " + line);

        return attributes;
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
