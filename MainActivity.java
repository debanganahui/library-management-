package com.example.librarymanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private DBHelper db;
    private String currentUser = null;
    private String currentRole = null;
    private LinearLayout root, content;
    private final int BLUE = Color.rgb(35, 74, 132);
    private final int DARK = Color.rgb(24, 54, 95);
    private final int LIGHT = Color.rgb(244, 247, 251);
    private static final long LOAN_DAYS = 14;
    private static final double FINE_PER_DAY = 2.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(this);
        showLogin();
    }

    private void setupPage(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LIGHT);

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextSize(22);
        heading.setTextColor(Color.WHITE);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(16, 22, 16, 22);
        heading.setBackgroundColor(DARK);
        root.addView(heading, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(18, 16, 18, 24);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private TextView label(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.rgb(35, 45, 60));
        v.setTextSize(15);
        v.setPadding(2, 12, 2, 5);
        return v;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setTextSize(16);
        e.setHint(hint);
        e.setPadding(12, 8, 12, 8);
        content.addView(e, new LinearLayout.LayoutParams(-1, -2));
        return e;
    }

    private Button button(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLUE));
        b.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 10, 0, 4);
        content.addView(b, lp);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void showLogin() {
        currentUser = null;
        currentRole = null;
        setupPage("LIBRARY MANAGEMENT SYSTEM");
        TextView intro = label("Login to continue");
        intro.setTextSize(18);
        intro.setTypeface(null, Typeface.BOLD);
        content.addView(intro);

        EditText username = input("Username");
        EditText password = input("Password");
        password.setInputType(129);

        button("Login", () -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString();
            String role = db.authenticate(u, p);
            if (role == null) {
                message("Invalid username or password");
            } else {
                currentUser = u;
                currentRole = role;
                showDashboard();
            }
        });

        button("Register as Student", () -> showStudentRegistration());

        TextView demo = label("Demo admin: admin / admin123\nDemo student: student / student123\nChange demo passwords before real use.");
        demo.setTextSize(13);
        content.addView(demo);
    }

    private void showStudentRegistration() {
        setupPage("STUDENT REGISTRATION");
        EditText name = input("Full name");
        EditText username = input("Choose username");
        EditText password = input("Choose password (minimum 6 characters)");
        password.setInputType(129);
        button("Create Student Account", () -> {
            String n = name.getText().toString().trim();
            String u = username.getText().toString().trim();
            String p = password.getText().toString();
            if (n.isEmpty() || u.isEmpty() || p.length() < 6) {
                message("Enter name, username and a password of at least 6 characters");
                return;
            }
            if (db.registerStudent(n, u, p)) {
                message("Registration successful. Please log in.");
                showLogin();
            } else {
                message("Username already exists");
            }
        });
        button("Back to Login", this::showLogin);
    }

    private void showDashboard() {
        setupPage((currentRole.equals("ADMIN") ? "ADMIN DASHBOARD" : "STUDENT DASHBOARD"));
        TextView welcome = label("Welcome, " + currentUser + " (" + currentRole + ")");
        welcome.setTextSize(17);
        welcome.setTypeface(null, Typeface.BOLD);
        content.addView(welcome);

        if ("ADMIN".equals(currentRole)) {
            button("Add New Book", this::showAddBook);
            button("View / Manage All Books", this::showAllBooks);
            button("View All Issued Books", this::showAllLoans);
            button("View Student Accounts", this::showStudents);
        } else {
            button("Browse Available Books", this::showAvailableBooks);
            button("My Issued Books / Return Book", this::showMyLoans);
            button("My Account", () -> showMyAccount());
        }
        button("Logout", this::showLogin);
    }

    private void showAddBook() {
        setupPage("ADD NEW BOOK");
        EditText title = input("Book title");
        EditText author = input("Author name");
        EditText isbn = input("ISBN (optional)");
        EditText copies = input("Number of copies");
        copies.setInputType(2);
        button("Save Book", () -> {
            String t = title.getText().toString().trim();
            String a = author.getText().toString().trim();
            String i = isbn.getText().toString().trim();
            int c;
            try { c = Integer.parseInt(copies.getText().toString().trim()); }
            catch (Exception ex) { c = 0; }
            if (t.isEmpty() || a.isEmpty() || c < 1) {
                message("Enter title, author and a valid copy count");
                return;
            }
            if (db.addBook(t, a, i, c)) {
                message("Book added successfully");
                showDashboard();
            } else message("Could not add book");
        });
        button("Back", this::showDashboard);
    }

    private void showAllBooks() {
        setupPage("ALL BOOKS");
        EditText search = input("Search by title, author or ISBN");
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);
        Runnable refresh = () -> {
            list.removeAllViews();
            Cursor c = db.getBooks(search.getText().toString());
            if (c.getCount() == 0) list.addView(label("No books found."));
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String title = c.getString(1);
                String author = c.getString(2);
                String isbn = c.getString(3);
                int total = c.getInt(4), available = c.getInt(5);
                TextView item = label(title + "\nAuthor: " + author + "\nISBN: " + isbn +
                        "\nAvailable: " + available + " / " + total);
                item.setBackgroundColor(Color.WHITE);
                item.setPadding(12, 12, 12, 12);
                list.addView(item);
                Button remove = new Button(this);
                remove.setText("Delete book");
                list.addView(remove);
                remove.setOnClickListener(v -> new AlertDialog.Builder(this)
                        .setTitle("Delete book")
                        .setMessage("Delete " + title + "? Books with active issues cannot be deleted.")
                        .setPositiveButton("Delete", (d, w) -> {
                            if (db.deleteBook(id)) message("Book deleted");
                            else message("Cannot delete: this book has active issues");
                            refresh.run();
                        }).setNegativeButton("Cancel", null).show());
            }
            c.close();
        };
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int count,int after){}
            public void onTextChanged(CharSequence s,int st,int before,int count){refresh.run();}
            public void afterTextChanged(android.text.Editable e){}
        });
        refresh.run();
        button("Back", this::showDashboard);
    }

    private void showAvailableBooks() {
        setupPage("AVAILABLE BOOKS");
        EditText search = input("Search books");
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list);
        Runnable refresh = () -> {
            list.removeAllViews();
            Cursor c = db.getBooks(search.getText().toString());
            boolean found = false;
            while (c.moveToNext()) {
                int id = c.getInt(0), available = c.getInt(5);
                if (available <= 0) continue;
                found = true;
                String title = c.getString(1);
                TextView item = label(title + "\nAuthor: " + c.getString(2) +
                        "\nAvailable copies: " + available);
                item.setBackgroundColor(Color.WHITE);
                item.setPadding(12,12,12,12);
                list.addView(item);
                Button issue = new Button(this);
                issue.setText("Issue this book");
                list.addView(issue);
                issue.setOnClickListener(v -> {
                    if (db.issueBook(id, currentUser)) {
                        message("Book issued. Return within 14 days.");
                        showAvailableBooks();
                    } else message("Could not issue book. Check availability or existing issue.");
                });
            }
            if (!found) list.addView(label("No available books found."));
            c.close();
        };
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int count,int after){}
            public void onTextChanged(CharSequence s,int st,int before,int count){refresh.run();}
            public void afterTextChanged(android.text.Editable e){}
        });
        refresh.run();
        button("Back", this::showDashboard);
    }

    private void showMyLoans() {
        setupPage("MY ISSUED BOOKS");
        Cursor c = db.getLoansForStudent(currentUser);
        if (c.getCount() == 0) content.addView(label("You have no issued books."));
        while (c.moveToNext()) {
            int loanId = c.getInt(0);
            String title = c.getString(1);
            String issueDate = c.getString(2);
            String dueDate = c.getString(3);
            TextView item = label("Book: " + title + "\nIssued: " + issueDate +
                    "\nDue: " + dueDate + "\nCurrent fine: " +
                    String.format(Locale.getDefault(), "₹%.2f", db.calculateFine(dueDate)));
            item.setBackgroundColor(Color.WHITE);
            item.setPadding(12,12,12,12);
            content.addView(item);
            button("Return: " + title, () -> {
                double fine = db.calculateFine(dueDate);
                new AlertDialog.Builder(this)
                        .setTitle("Return book")
                        .setMessage("Calculated late fine: ₹" + String.format(Locale.getDefault(),"%.2f",fine) +
                                "\nReturn this book now?")
                        .setPositiveButton("Confirm Return", (d,w) -> {
                            db.returnBook(loanId);
                            message("Book returned. Fine recorded: ₹" +
                                    String.format(Locale.getDefault(),"%.2f",fine));
                            showMyLoans();
                        }).setNegativeButton("Cancel",null).show();
            });
        }
        c.close();
        button("Back", this::showDashboard);
    }

    private void showAllLoans() {
        setupPage("ALL ISSUED BOOKS");
        Cursor c = db.getAllLoans();
        if (c.getCount() == 0) content.addView(label("No books are currently issued."));
        while (c.moveToNext()) {
            String text = "Student: " + c.getString(1) + "\nBook: " + c.getString(2) +
                    "\nIssued: " + c.getString(3) + "\nDue: " + c.getString(4) +
                    "\nCurrent fine: ₹" + String.format(Locale.getDefault(),"%.2f",db.calculateFine(c.getString(4)));
            TextView item = label(text);
            item.setBackgroundColor(Color.WHITE);
            item.setPadding(12,12,12,12);
            content.addView(item);
        }
        c.close();
        button("Back", this::showDashboard);
    }

    private void showStudents() {
        setupPage("STUDENT ACCOUNTS");
        Cursor c = db.getStudents();
        if (c.getCount() == 0) content.addView(label("No student accounts."));
        while (c.moveToNext()) content.addView(label("Name: " + c.getString(0) +
                "\nUsername: " + c.getString(1)));
        c.close();
        button("Back", this::showDashboard);
    }

    private void showMyAccount() {
        setupPage("MY ACCOUNT");
        content.addView(label("Username: " + currentUser + "\nRole: STUDENT"));
        button("Back", this::showDashboard);
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }

    // Local SQLite database
    private static class DBHelper extends android.database.sqlite.SQLiteOpenHelper {
        DBHelper(Activity context) {
            super(context, "LibraryManagement.db", null, 1);
        }

        @Override public void onCreate(android.database.sqlite.SQLiteDatabase db) {
            db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL)");
            db.execSQL("CREATE TABLE books(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, isbn TEXT, total INTEGER NOT NULL, available INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE loans(id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER NOT NULL, username TEXT NOT NULL, issue_date TEXT NOT NULL, due_date TEXT NOT NULL, return_date TEXT, fine REAL DEFAULT 0)");
            db.execSQL("INSERT INTO users(name,username,password,role) VALUES('Administrator','admin','admin123','ADMIN')");
            db.execSQL("INSERT INTO users(name,username,password,role) VALUES('Demo Student','student','student123','STUDENT')");
            db.execSQL("INSERT INTO books(title,author,isbn,total,available) VALUES('The Alchemist','Paulo Coelho','9780061122415',5,5)");
            db.execSQL("INSERT INTO books(title,author,isbn,total,available) VALUES('Wings of Fire','A. P. J. Abdul Kalam','9788173711466',3,3)");
            db.execSQL("INSERT INTO books(title,author,isbn,total,available) VALUES('Clean Code','Robert C. Martin','9780132350884',2,2)");
        }

        @Override public void onUpgrade(android.database.sqlite.SQLiteDatabase db,int oldVersion,int newVersion) {}

        String authenticate(String username, String password) {
            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT role FROM users WHERE username=? AND password=?",
                    new String[]{username,password});
            String role = c.moveToFirst() ? c.getString(0) : null;
            c.close();
            return role;
        }

        boolean registerStudent(String name,String username,String password) {
            android.content.ContentValues v = new android.content.ContentValues();
            v.put("name",name); v.put("username",username); v.put("password",password); v.put("role","STUDENT");
            return getWritableDatabase().insert("users",null,v) != -1;
        }

        boolean addBook(String title,String author,String isbn,int copies) {
            android.content.ContentValues v = new android.content.ContentValues();
            v.put("title",title); v.put("author",author); v.put("isbn",isbn);
            v.put("total",copies); v.put("available",copies);
            return getWritableDatabase().insert("books",null,v) != -1;
        }

        Cursor getBooks(String query) {
            String q = "%" + query + "%";
            return getReadableDatabase().rawQuery(
                    "SELECT id,title,author,isbn,total,available FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY title",
                    new String[]{q,q,q});
        }

        boolean deleteBook(int id) {
            Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM loans WHERE book_id=? AND return_date IS NULL",
                    new String[]{String.valueOf(id)});
            c.moveToFirst(); boolean active = c.getInt(0)>0; c.close();
            if (active) return false;
            return getWritableDatabase().delete("books","id=?",new String[]{String.valueOf(id)}) > 0;
        }

        boolean issueBook(int bookId,String username) {
            android.database.sqlite.SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            try {
                Cursor active = database.rawQuery("SELECT COUNT(*) FROM loans WHERE book_id=? AND username=? AND return_date IS NULL",
                        new String[]{String.valueOf(bookId),username});
                active.moveToFirst(); boolean already = active.getInt(0)>0; active.close();
                if (already) return false;
                Cursor book = database.rawQuery("SELECT available FROM books WHERE id=?",new String[]{String.valueOf(bookId)});
                if (!book.moveToFirst() || book.getInt(0)<=0) { book.close(); return false; }
                book.close();

                String issue = dateAt(0), due = dateAt((int)LOAN_DAYS);
                android.content.ContentValues loan = new android.content.ContentValues();
                loan.put("book_id",bookId); loan.put("username",username);
                loan.put("issue_date",issue); loan.put("due_date",due);
                long id = database.insert("loans",null,loan);
                if (id == -1) return false;

                database.execSQL("UPDATE books SET available=available-1 WHERE id=?",new Object[]{bookId});
                database.setTransactionSuccessful();
                return true;
            } finally { database.endTransaction(); }
        }

        Cursor getLoansForStudent(String username) {
            return getReadableDatabase().rawQuery(
                    "SELECT loans.id,books.title,loans.issue_date,loans.due_date FROM loans JOIN books ON books.id=loans.book_id WHERE loans.username=? AND loans.return_date IS NULL ORDER BY loans.id DESC",
                    new String[]{username});
        }

        Cursor getAllLoans() {
            return getReadableDatabase().rawQuery(
                    "SELECT loans.id,loans.username,books.title,loans.issue_date,loans.due_date FROM loans JOIN books ON books.id=loans.book_id WHERE loans.return_date IS NULL ORDER BY loans.due_date",
                    null);
        }

        Cursor getStudents() {
            return getReadableDatabase().rawQuery("SELECT name,username FROM users WHERE role='STUDENT' ORDER BY name",null);
        }

        boolean returnBook(int loanId) {
            android.database.sqlite.SQLiteDatabase database = getWritableDatabase();
            database.beginTransaction();
            try {
                Cursor c = database.rawQuery("SELECT book_id,due_date FROM loans WHERE id=? AND return_date IS NULL",
                        new String[]{String.valueOf(loanId)});
                if (!c.moveToFirst()) { c.close(); return false; }
                int bookId = c.getInt(0);
                String due = c.getString(1);
                c.close();
                double fine = calculateFine(due);
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("return_date",dateAt(0));
                values.put("fine",fine);
                database.update("loans",values,"id=? AND return_date IS NULL",new String[]{String.valueOf(loanId)});
                database.execSQL("UPDATE books SET available=available+1 WHERE id=?",new Object[]{bookId});
                database.setTransactionSuccessful();
                return true;
            } finally { database.endTransaction(); }
        }

        double calculateFine(String dueDate) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US);
                sdf.setLenient(false);
                long due = sdf.parse(dueDate).getTime();
                long today = sdf.parse(dateAt(0)).getTime();
                long lateDays = Math.max(0,TimeUnit.MILLISECONDS.toDays(today-due));
                return lateDays * 2.0;
            } catch (Exception e) { return 0.0; }
        }

        private String dateAt(int plusDays) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR,plusDays);
            return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.getTime());
        }
    }
}
