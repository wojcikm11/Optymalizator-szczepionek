import app.TransactionsControl;

public class Main {
    public static void main(String[] args) {
        TransactionsControl transactionsControl = new TransactionsControl("data4.txt");
        transactionsControl.minimizeAndPrintCosts();
    }
}
