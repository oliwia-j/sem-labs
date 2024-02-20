package com.napier.sem;

import java.io.Console;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        // Create new Application
        App database = new App();

        // Connect to database
        database.connect();

        // UC-6
        // Get Employee
        Employee emp = database.getEmployee(255530);
        // Display results
        database.displayEmployee(emp);

        // UC-1
        // Get all salaries
        database.displaySalaries(database.getSalaries());

        // Disconnect from database
        database.disconnect();
    }

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect() {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://db:3306/employees?useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + Integer.toString(i));
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Retrieves from database details of the employee of the provided ID
     * @param ID
     * @return Employee object
     */
    public Employee getEmployee(int ID) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, titles.title, " +
                            "salaries.salary, departments.dept_name, managers.first_name, managers.last_name "
                            + "FROM employees, titles, salaries, dept_emp, departments, dept_manager, " +

                            "(SELECT employees.emp_no, employees.first_name, employees.last_name " +
                            "FROM employees, dept_manager, departments " +
                            "WHERE employees.emp_no = dept_manager.emp_no " +
                            "AND dept_manager.dept_no = departments.dept_no ) as managers "

                            + "WHERE employees.emp_no = " + ID + " "
                            + "AND employees.emp_no = titles.emp_no "
                            + "AND employees.emp_no = salaries.emp_no "
                            + "AND employees.emp_no = dept_emp.emp_no "

                            + "AND dept_emp.dept_no = departments.dept_no "
                            + "AND departments.dept_no = dept_manager.dept_no "

                            + "AND managers.emp_no = dept_manager.emp_no "

                            + "AND titles.to_date = DATE('9999-01-01') "
                            + "AND dept_emp.to_date = DATE('9999-01-01') ";

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.title = rset.getString("titles.title");
                emp.salary = rset.getInt("salaries.salary");
                emp.dept_name = rset.getString("departments.dept_name");
                emp.manager = rset.getString("managers.first_name") + (" ") + rset.getString("managers.last_name");
                return emp;
            } else
                return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * Prints to console details of the provided Employee object.
     * @param emp
     */
    public void displayEmployee(Employee emp) {
        if (emp != null) {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept_name + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }

    /**
     * Retrieves salary details of all employees from database in ascending order by employees' IDs.
     * @return ArrayList with arrays of Strings, each with employee's id, employee's first name, employee's last name
     * and its salary
     */
    public ArrayList<String[]> getSalaries() {

        String[] row = new String[4];
        ArrayList<String[]> data = new ArrayList<>();

        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary " +
                            "FROM employees, salaries " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "ORDER BY employees.emp_no ASC ";

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);

            while(rset.next()){

                row[0] = rset.getString("employees.emp_no");
                row[1] = rset.getString("employees.first_name");
                row[2] = rset.getString("employees.last_name");
                row[3] = rset.getString("salaries.salary");

                // https://www.c-sharpcorner.com/article/how-to-copy-an-array-in-c-sharp/#:~:text=A%3A%20When%20you%20copy%20an,those%20in%20the%20original%20Array.
                data.add(row.clone());
            }

            return data;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    /**
     * Prints to console details of the provided salaries
     * @param salaryDetails
     */
    public void displaySalaries(ArrayList<String[]> salaryDetails){
        if (salaryDetails != null) {

            for (String[] salaryDetail : salaryDetails) {
                for (String s : salaryDetail) {

                    // Printing columns of the same size
                    // https://www.c-sharpcorner.com/article/how-to-copy-an-array-in-c-sharp/#:~:text=A%3A%20When%20you%20copy%20an,those%20in%20the%20original%20Array.
                    System.out.printf("%-20s", s);
                }
                System.out.println();
            }
        }
    }

}
