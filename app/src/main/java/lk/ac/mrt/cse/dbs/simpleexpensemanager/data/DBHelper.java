package lk.ac.mrt.cse.dbs.simpleexpensemanager.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.AccountBalanceInsufficientException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class DBHelper extends SQLiteOpenHelper {
    @SuppressLint("SimpleDateFormat")
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DBHelper(Context context) { super(context, "180652A.db", null, 1); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE account(" +
                "account_no TEXT PRIMARY KEY," +
                "bank_name TEXT NOT NULL," +
                "account_holder_name TEXT NOT NULL," +
                "balance REAL NOT NULL CHECK(balance > 0))");
        db.execSQL("CREATE TABLE transaction_table(" +
                "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT NOT NULL," +
                "account_no TEXT NOT NULL," +
                "expense_type TEXT NOT NULL CHECK(expense_type == \"EXPENSE\" OR expense_type == \"INCOME\")," +
                "amount REAL NOT NULL CHECK(amount > 0)," +
                "FOREIGN KEY(account_no) REFERENCES account(account_no))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS account");
        db.execSQL("DROP TABLE IF EXISTS transaction_table");
    }

    public List<String> getAccountNumbersList() {
        List<String> accountNumbersList = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT account_no FROM account", null);
        while (cursor.moveToNext()) {
            accountNumbersList.add(cursor.getString(0));
        }
        return accountNumbersList;
    }

    public List<Account> getAccountsList() {
        List<Account> accountsList = new ArrayList<Account>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account", null);
        while (cursor.moveToNext()) {
            accountsList.add(new Account(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3)));
        }
        return accountsList;
    }

    public Account getAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account WHERE account_no = ?", new String[]{accountNo});
        if (cursor.moveToFirst()) {
            return new Account(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3));
        }
        String msg = "Account " + accountNo + " is invalid.";
        throw new InvalidAccountException(msg);
    }

    public boolean addAccount(Account account) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT account_no FROM account WHERE account_no = ?", new String[]{account.getAccountNo()});
        if (cursor.moveToFirst()) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("account_no", account.getAccountNo());
        contentValues.put("bank_name", account.getBankName());
        contentValues.put("account_holder_name", account.getAccountHolderName());
        contentValues.put("balance", account.getBalance());
        db.insert("account", null, contentValues);
        return true;
    }

    public void removeAccount(String accountNo) throws InvalidAccountException {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account WHERE account_no = ?", new String[]{accountNo});
        if (!cursor.moveToFirst()) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
        db.delete("account", "account_no = ?", new String[]{accountNo});
    }

    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException, AccountBalanceInsufficientException {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account WHERE account_no = ?", new String[]{accountNo});
        if (!cursor.moveToFirst()) {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }
        ContentValues contentValues = new ContentValues();
        double value;
        switch (expenseType) {
            case EXPENSE:
                value = cursor.getDouble(3) - amount;
                if (value <= 0) {
                    String msg = "Account balance is insufficient.";
                    throw new AccountBalanceInsufficientException(msg);
                }
                contentValues.put("balance", value);
                break;
            case INCOME:
                value = cursor.getDouble(3) + amount;
                contentValues.put("balance", value);
                break;
        }
        db.update("account", contentValues, "account_no = ?", new String[]{accountNo});
    }

    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", dateFormat.format(date));
        contentValues.put("account_no", accountNo);
        contentValues.put("expense_type", expenseType.toString());
        contentValues.put("amount", amount);
        db.insert("transaction_table", null, contentValues);
    }

    public List<Transaction> getAllTransactionLogs() {
        List<Transaction> transactionLogs = new LinkedList<Transaction>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transaction_table", null);
        while (cursor.moveToNext()) {
            try {
                transactionLogs.add(new Transaction(
                        new SimpleDateFormat("yyyy-MM-dd").parse(cursor.getString(1)),
                        cursor.getString(2),
                        ExpenseType.valueOf(cursor.getString(3)),
                        cursor.getDouble(4)));
            } catch(ParseException ignored) {}
        }
        return transactionLogs;
    }

    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        List<Transaction> transactions = this.getAllTransactionLogs();
        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        return transactions.subList(size - limit, size);
    }
}