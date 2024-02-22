package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        // Create new Application
        App database = new App();

        // Connect to database
        database.connect();

        // UC-06
        // Get Employee
        Employee emp = database.getEmployee(255530);
        // Display result
        database.displayEmployee(emp);

        // UC-01
        // Extract all employees salary information
        ArrayList<Employee> employees = database.getAllSalaries();
        // Test the size of the returned data - should be 240124
        System.out.println(employees.size());
        // Display all salaries
        database.printSalaries(employees);

        // UC-04
        // Extract employees salary information of specified role
        ArrayList<Employee> employeesByRole = database.getSalaries("Engineer");
        // Display salaries per role
        database.printSalaries(employeesByRole);

        // UC-02
        // Extract data of specified department and create object
        Department department = database.getDepartment("Sales");
        // Extract salaries of employees from specified department
        ArrayList<Employee> employeesByDept = database.getSalariesByDepartment(department);
        // Display salaries of the employees of the specified department
        database.printSalaries(employeesByDept);

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
     * Retrieves from database details of the employee with provided ID.
     * @param ID
     * @return Employee object, or null if there is an error.
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
     * Prints to console details of the provided Employee.
     * @param emp The employee to print its details.
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
     * Get all the current employees and salaries.
     * @return A list of all employees and salaries, or null if there is an error.
     */
    public ArrayList<Employee> getAllSalaries() {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries "
                            + "WHERE employees.emp_no = salaries.emp_no AND salaries.to_date = '9999-01-01' "
                            + "ORDER BY employees.emp_no ASC ";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Employee> employees = new ArrayList<Employee>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    /**
     * Prints a list of employees and their salaries.
     * @param employees The list of employees to print.
     */
    public void printSalaries(ArrayList<Employee> employees) {
        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list
        for (Employee emp : employees) {
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    /**
     * Get all the current employees and salaries for specified role title.
     * @return A list of all employees and salaries by a role, or null if there is an error.
     */
    public ArrayList<Employee> getSalaries(String role) {
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries, titles "
                            + "WHERE employees.emp_no = salaries.emp_no AND salaries.to_date = '9999-01-01' "
                            + "AND  employees.emp_no = titles.emp_no "
                            + "AND titles.to_date = '9999-01-01' "
                            + "AND titles.title = '" + role+ "' "
                            + "ORDER BY employees.emp_no ASC ";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Employee> employees = new ArrayList<Employee>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    /**
     * Get department object of the specified name.
     * @param dept_name
     * @return Departemnt object, or null if there is an error.
     */
    public Department getDepartment(String dept_name){
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, "
                            + "titles.title,  salaries.salary, "
                            + "departments.dept_no, departments.dept_name "
                            + "FROM employees, dept_manager, titles, salaries, departments "

                            + "WHERE employees.emp_no = dept_manager.emp_no "
                            + "AND dept_manager.dept_no = departments.dept_no "
                            + "AND employees.emp_no = titles.emp_no "
                            + "AND employees.emp_no = salaries.emp_no "

                            + "AND salaries.to_date = DATE('9999-01-01') "
                            + "AND dept_manager.to_date = DATE('9999-01-01') "
                            + "AND departments.dept_name = '" + dept_name + "' ";

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (rset.next()) {
                Employee emp = new Employee();
                Department dept = new Department();

                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.title = rset.getString("titles.title");
                emp.salary = rset.getInt("salaries.salary");
                emp.dept_name = rset.getString("departments.dept_name");
                emp.manager = ("none");

                dept.dept_no = rset.getString("departments.dept_no");
                dept.dept_name = rset.getString("departments.dept_name");
                dept.manager = emp;
                return dept;
            } else
                return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get department details");
            return null;
        }
    }

    /**
     * Get all current employees and their salaries from specified department.
     * @param dept Department to extract data from database of.
     * @return ArrayList of all employees from the department and their salaries, or null if there is an error.
     */
    public ArrayList<Employee> getSalariesByDepartment(Department dept){
        try {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary " +
                            "FROM employees, salaries, dept_emp, departments " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND employees.emp_no = dept_emp.emp_no " +
                            "AND dept_emp.dept_no = departments.dept_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "AND departments.dept_no = '" + dept.dept_no + "' " +
                            "ORDER BY employees.emp_no ASC ";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Employee> employees = new ArrayList<Employee>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary by department details");
            return null;
        }
    }

}
