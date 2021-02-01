package lk.ac.mrt.cse.dbs.simpleexpensemanager.control;

import android.content.Context;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.DBHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentAccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl.PersistentTransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;

public class PersistentDemoExpenseManager extends ExpenseManager {
    private final Context context;

    public PersistentDemoExpenseManager(Context context) {
        this.context = context;
        setup();
    }

    @Override
    public void setup() {
        DBHelper dbHelper = new DBHelper(context);

        TransactionDAO persistentTransactionDAO = new PersistentTransactionDAO(dbHelper);
        setTransactionsDAO(persistentTransactionDAO);

        AccountDAO persistentAccountDAO = new PersistentAccountDAO(context, dbHelper);
        setAccountsDAO(persistentAccountDAO);

        Account acc_1 = new Account("12345A", "Peoples Bank", "Pasindu Udawatta", 10000.0);
        Account acc_2 = new Account("78945Z", "BoC", "Kamal Perera", 80000.0);
        Account acc_3 = new Account("96324G", "Seylan Bank", "James Bond", 150000.0);
        getAccountsDAO().addAccount(acc_1);
        getAccountsDAO().addAccount(acc_2);
        getAccountsDAO().addAccount(acc_3);

//        Below queries are related to the dummy data
//        INSERT INTO account VALUES ("12345A", "Peoples Bank", "Pasindu Udawatta", 10000.0);
//        INSERT INTO account VALUES ("78945Z", "BoC", "Kamal Perera", 80000.0);
//        INSERT INTO account VALUES ("96324G", "Seylan Bank", "James Bond", 150000.0);
    }
}
