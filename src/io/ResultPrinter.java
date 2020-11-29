package io;

import model.*;

import java.util.List;

public class ResultPrinter {
    public void printTransactions(List<VAMPharmacy> pharmacies) {
        double sum = 0;
        for (VAMPharmacy pharmacy: pharmacies) {
            for (Connection connection: pharmacy.getPharmacy().getConnections())
                if (connection.getVaccinesSold() != 0) {
                    double cost = connection.getVaccinesSold() * connection.getPrice();
                    System.out.println(connection);
                    sum += cost;
                }
        }
        System.out.println("\nOpłaty całkowite: " + ((int) ((sum + 0.005f) * 100)) / 100f + " zł");
    }
}
