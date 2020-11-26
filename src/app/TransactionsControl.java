package app;

import exception.IdAlreadyExistsException;
import exception.InputDataException;
import exception.InvalidConnectionsException;
import io.FileManager;
import io.ResultPrinter;
import model.Connection;
import model.FileData;
import model.Pharmacy;
import model.Producer;

import java.util.List;

public class TransactionsControl {
    private ResultPrinter resultPrinter;
    private FileManager fileManager;
    private FileData fileData;
    private DataControl dataControl;

    public TransactionsControl(String fileName) {
        try {
            fileManager = new FileManager();
            fileData = fileManager.readData(fileName);
            resultPrinter = new ResultPrinter();
            dataControl = new DataControl();
        } catch (NumberFormatException e) {
            System.err.println("Błąd w danie liczbowej w linijce " + fileManager.getLine());
            System.exit(-1);
        } catch (IdAlreadyExistsException | IllegalArgumentException | InputDataException | InvalidConnectionsException e) {
//            System.err.println(e.getClass().getName());
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public void minimizeCosts() {
        List<Producer> producers = fileData.getProducers();
        List<Pharmacy> pharmacies = fileData.getPharmacies();
        List<Connection> connections = fileData.getConnections();
        int[] pharmaciesIds = fileData.getPharmaciesIds();
        int[] producersIds = fileData.getProducersIds();

        double[][] producersPharmaciesVector;

        if(problemBalanced(producers, pharmacies)) // do zastanowienia czy rozpatrywać w ogóle taki przypadek
            producersPharmaciesVector = createVector(connections, producersIds, pharmaciesIds, true);
        else
            producersPharmaciesVector = createVector(connections, producersIds, pharmaciesIds, false);

        getVaccinesSoldToPharmaciesVector(producersPharmaciesVector, pharmaciesIds, producersIds, producers, pharmacies);

    }

    private double[][] getVaccinesSoldToPharmaciesVector(double[][] producersPharmaciesVector,
                                                         int[] pharmaciesIds, int[] producersIds,
                                                         List<Producer> producers, List<Pharmacy> pharmacies) {

        int[] producersSupply = getProducersSupplyVector(producersIds, producers);
        int[] pharmaciesDemand = getPharmaciesDemandVector(pharmaciesIds, pharmacies);

        double[][] vaccinesPricesVector = producersPharmaciesVector.clone();
        int[] producersSupplyCopy = producersSupply.clone();
        int[] pharmaciesDemandCopy = pharmaciesDemand.clone();

        return leastCostCell(vaccinesPricesVector, producersSupplyCopy, pharmaciesDemandCopy);
    }

    private double[][] leastCostCell(double[][] vaccinesPricesVector, int[] producersSupply, int[] pharmaciesDemand) {
        double[][] vaccinesSoldToPharmaciesVector = new double[vaccinesPricesVector.length][vaccinesPricesVector[0].length];

        while(vectorContainsElementsToBeIgnored(vaccinesPricesVector)) {

            int[] minCellCords = findMinCellCords(vaccinesPricesVector);

            int minProducerRow = minCellCords[0];
            int minPharmacyColumn = minCellCords[1];

            int producerSupply = producersSupply[minProducerRow];
            int pharmacyDemand = pharmaciesDemand[minPharmacyColumn];
            int maxAmountPossible = Math.min(producerSupply, pharmacyDemand);

            if (maxAmountPossible == producerSupply && maxAmountPossible == pharmacyDemand) {
                producersSupply[minProducerRow] = 0;
                pharmaciesDemand[minPharmacyColumn] = 0;
                ignoreRow(vaccinesPricesVector, minProducerRow);
                ignoreColumn(vaccinesPricesVector, minPharmacyColumn);
            } else if (maxAmountPossible == producerSupply) {
                producersSupply[minProducerRow] = 0;
                pharmaciesDemand[minPharmacyColumn] -= maxAmountPossible;
                ignoreRow(vaccinesPricesVector, minProducerRow);
            } else {
                producersSupply[minProducerRow] -= maxAmountPossible;
                pharmaciesDemand[minPharmacyColumn] = 0;
                ignoreColumn(vaccinesPricesVector, minPharmacyColumn);
            }
            vaccinesSoldToPharmaciesVector[minProducerRow][minPharmacyColumn] = maxAmountPossible;
        }
        return vaccinesSoldToPharmaciesVector;
    }

    private boolean vectorContainsElementsToBeIgnored(double[][] producersPharmacies) {
        for (int i = 0; i < producersPharmacies.length; i++) {
            for (int j = 0; j < producersPharmacies[0].length; j++) {
                if (producersPharmacies[i][j] != -1)
                    return true;
            }
        }
        return false;
    }

    private void ignoreColumn(double[][] producersPharmacies, int columnToIgnore) {
        for (int i = 0; i < producersPharmacies.length; i++) {
            producersPharmacies[i][columnToIgnore] = -1;
        }
    }

    private void ignoreRow(double[][] producersPharmacies, int rowToIgnore) {
        for (int i = 0; i < producersPharmacies[0].length; i++) {
            producersPharmacies[rowToIgnore][i] = -1;
        }
    }

    private int[] findMinCellCords(double[][] producersPharmacies) {
        double minValue = producersPharmacies[0][0];
        int[] minCellCords = new int[2];
        int minProducerRow = 0;
        int minPharmacyColumn = 0;

        for (int i = 0; i < producersPharmacies.length; i++) {
            for (int j = 0; j < producersPharmacies[0].length; j++) {
                if (producersPharmacies[i][j] == -1)
                    continue;

                if (producersPharmacies[i][j] < minValue) {
                    minValue = producersPharmacies[i][j];
                    minProducerRow = i;
                    minPharmacyColumn = j;
                }
            }
        }
        minCellCords[0] = minProducerRow;
        minCellCords[1] = minPharmacyColumn;
        return minCellCords;
    }

    private int[] getProducersSupplyVector(int[] producersIds, List<Producer> producers) {
        int[] producersSupply = new int[producersIds.length];
        for (int i = 0; i < producersSupply.length; i++)
            producersSupply[i] = getSupplyById(producers, producersIds[i]);

        return producersSupply;
    }

    private int[] getPharmaciesDemandVector(int[] pharmaciesIds, List<Pharmacy> pharmacies) {
        int[] pharmaciesDemand = new int[pharmaciesIds.length];
        for (int i = 0; i < pharmaciesDemand.length; i++)
            pharmaciesDemand[i] = getDemandById(pharmacies, pharmaciesIds[i]);

        return pharmaciesDemand;
    }

    private int getSupplyById(List<Producer> producers, int producerId) {
        return dataControl.getSupplyById(producers, producerId);
    }

    private int getDemandById(List<Pharmacy> pharmacies, int pharmacyId) {
        return dataControl.getDemandById(pharmacies, pharmacyId);
    }

    private double[][] createVector(List<Connection> connections, int[] producersIds, int[] pharmaciesIds, boolean isBalanced) {
        return dataControl.createVector(connections, producersIds, pharmaciesIds, isBalanced);
    }


    private boolean problemBalanced(List<Producer> producers, List<Pharmacy> pharmacies) {
        return dataControl.problemBalanced(producers, pharmacies);
    }

    private int findSupply(List<Connection> connections) {

    }
}
