package com.prison.util;

import com.prison.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Database {
    private static Database instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:prison_management.db";
    
    private Database() {
    try {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(DB_URL);
        createTables();
        ensureInmateColumns();
        createUsersTable();          // ADD THIS LINE
        initializeSampleData();
        initializeDefaultAdmin();    // ADD THIS LINE
        System.out.println("Database connected successfully!");
    } catch (Exception e) {
        System.err.println("Database connection failed!");
        e.printStackTrace();
    }
}
    
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
    
    private void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            stmt.execute("CREATE TABLE IF NOT EXISTS inmates (inmate_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, age INTEGER, gender TEXT, crime TEXT, admission_date TEXT, release_date TEXT, status TEXT, cell_number TEXT, behavior_record TEXT)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS staff (staff_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, role TEXT, shift TEXT, contact_info TEXT, username TEXT, password TEXT)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS work_assignments (assignment_id INTEGER PRIMARY KEY AUTOINCREMENT, inmate_id INTEGER, work_type TEXT, assigned_date TEXT, status TEXT, supervisor_name TEXT, FOREIGN KEY (inmate_id) REFERENCES inmates(inmate_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS resources (resource_id INTEGER PRIMARY KEY AUTOINCREMENT, resource_name TEXT NOT NULL, category TEXT, quantity INTEGER, location TEXT, status TEXT)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS medical_checkups (checkup_id INTEGER PRIMARY KEY AUTOINCREMENT, inmate_id INTEGER, checkup_date TEXT, diagnosis TEXT, treatment TEXT, doctor_name TEXT, notes TEXT, FOREIGN KEY (inmate_id) REFERENCES inmates(inmate_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS incident_reports (report_id INTEGER PRIMARY KEY AUTOINCREMENT, inmate_id INTEGER, incident_date TEXT, incident_type TEXT, description TEXT, reported_by TEXT, action_taken TEXT, FOREIGN KEY (inmate_id) REFERENCES inmates(inmate_id))");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS movement_records (movement_id INTEGER PRIMARY KEY AUTOINCREMENT, inmate_id INTEGER, from_location TEXT, to_location TEXT, movement_time TEXT, reason TEXT, authorized_by TEXT, FOREIGN KEY (inmate_id) REFERENCES inmates(inmate_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS released_inmates (archive_id INTEGER PRIMARY KEY AUTOINCREMENT, inmate_id INTEGER NOT NULL, name TEXT NOT NULL, actual_release_date TEXT NOT NULL, released_by TEXT, notes TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS activity_logs (log_id INTEGER PRIMARY KEY AUTOINCREMENT, timestamp TEXT NOT NULL, action TEXT NOT NULL, details TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS court_schedules (schedule_id INTEGER PRIMARY KEY AUTOINCREMENT, inmate_id INTEGER NOT NULL, court_time TEXT NOT NULL, scheduled_by TEXT, created_at TEXT NOT NULL, FOREIGN KEY (inmate_id) REFERENCES inmates(inmate_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS resource_assignments (assignment_id INTEGER PRIMARY KEY AUTOINCREMENT, resource_id INTEGER NOT NULL, inmate_id INTEGER NOT NULL, quantity INTEGER NOT NULL, assigned_at TEXT NOT NULL, assigned_by TEXT, FOREIGN KEY (resource_id) REFERENCES resources(resource_id), FOREIGN KEY (inmate_id) REFERENCES inmates(inmate_id))");
            
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureInmateColumns() {
        Set<String> columns = new HashSet<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(inmates)");
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
            rs.close();

            if (!columns.contains("balance")) {
                stmt.execute("ALTER TABLE inmates ADD COLUMN balance REAL NOT NULL DEFAULT 500");
            }
            if (!columns.contains("last_phone_call_date")) {
                stmt.execute("ALTER TABLE inmates ADD COLUMN last_phone_call_date TEXT");
            }
            if (!columns.contains("last_free_phone_call_date")) {
                stmt.execute("ALTER TABLE inmates ADD COLUMN last_free_phone_call_date TEXT");
            }
            if (!columns.contains("last_paid_phone_call_date")) {
                stmt.execute("ALTER TABLE inmates ADD COLUMN last_paid_phone_call_date TEXT");
            }
            if (!columns.contains("court_date")) {
                stmt.execute("ALTER TABLE inmates ADD COLUMN court_date TEXT");
            }

            stmt.execute("UPDATE inmates SET balance = 500 WHERE balance IS NULL OR balance <= 0");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeSampleData() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inmates");
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
            
            addInmate(new Inmate(0, "John Rana", 35, "Male", "Theft", LocalDate.now().minusMonths(6), LocalDate.now().plusYears(2), "Active", "A-101"));
            addInmate(new Inmate(0, "Jane Smith", 28, "Female", "Fraud", LocalDate.now().minusYears(1), LocalDate.now().plusYears(1), "Active", "B-205"));
            addStaff(new Staff(0, "Admin User", "Administrator", "Day", "admin@prison.com"));
            addStaff(new Staff(0, "Dr. Robert Brown", "Medical Staff", "Day", "doctor@prison.com"));
            addStaff(new Staff(0, "Officer Mike Wilson", "Guard", "Night", "guard@prison.com"));
            addResource(new Resources(0, "Medical Supplies", "Healthcare", 100, "Medical Wing", "Available"));
            addResource(new Resources(0, "Food Rations", "Food", 500, "Kitchen", "Available"));
        } catch (Exception e) {
        }
    }
    
    public void addInmate(Inmate inmate) {
        String sql = "INSERT INTO inmates (name, age, gender, crime, admission_date, release_date, court_date, status, cell_number, balance, last_phone_call_date, last_free_phone_call_date, last_paid_phone_call_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            LocalDate courtDate = calculateCourtDate(inmate.getAdmissionDate(), inmate.getReleaseDate());
            inmate.setCourtDate(courtDate);

            pstmt.setString(1, inmate.getName());
            pstmt.setInt(2, inmate.getAge());
            pstmt.setString(3, inmate.getGender());
            pstmt.setString(4, inmate.getCrime());
            pstmt.setString(5, inmate.getAdmissionDate().toString());
            pstmt.setString(6, inmate.getReleaseDate() != null ? inmate.getReleaseDate().toString() : null);
            pstmt.setString(7, courtDate != null ? courtDate.toString() : null);
            pstmt.setString(8, inmate.getStatus());
            pstmt.setString(9, inmate.getCellNumber());
            double startingBalance = inmate.getBalance() > 0 ? inmate.getBalance() : Inmate.DEFAULT_STARTING_BALANCE;
            pstmt.setDouble(10, startingBalance);
            pstmt.setString(11, inmate.getLastPhoneCallDate() != null ? inmate.getLastPhoneCallDate().toString() : null);
            pstmt.setString(12, inmate.getLastFreePhoneCallDate() != null ? inmate.getLastFreePhoneCallDate().toString() : null);
            pstmt.setString(13, inmate.getLastPaidPhoneCallDate() != null ? inmate.getLastPaidPhoneCallDate().toString() : null);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                inmate.setInmateId(rs.getInt(1));
            }
            inmate.setBalance(startingBalance);
            pstmt.close();
            System.out.println("Inmate added: " + inmate.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Inmate getInmate(int id) {
        String sql = "SELECT * FROM inmates WHERE inmate_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Inmate inmate = extractInmateFromResultSet(rs);
                pstmt.close();
                return inmate;
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Inmate> getAllInmates() {
        List<Inmate> inmates = new ArrayList<>();
        String sql = "SELECT * FROM inmates ORDER BY inmate_id";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                inmates.add(extractInmateFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inmates;
    }
    
    public List<Inmate> getEligibleInmatesForRelease() {
        List<Inmate> inmates = new ArrayList<>();
        String sql = "SELECT * FROM inmates WHERE status = 'Active' AND release_date IS NOT NULL AND release_date <= ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                inmates.add(extractInmateFromResultSet(rs));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inmates;
    }

    public List<Inmate> getAllActiveInmates() {
        List<Inmate> inmates = new ArrayList<>();
        String sql = "SELECT * FROM inmates WHERE status = 'Active' ORDER BY inmate_id";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                inmates.add(extractInmateFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inmates;
    }

    public boolean archiveAndReleaseInmate(Inmate inmate, String releasedBy, String notes) {
        String insertReleasedSql = "INSERT INTO released_inmates (inmate_id, name, actual_release_date, released_by, notes) VALUES (?, ?, ?, ?, ?)";
        String deleteInmateSql = "DELETE FROM inmates WHERE inmate_id = ?";

        try {
            connection.setAutoCommit(false);

            PreparedStatement insertStmt = connection.prepareStatement(insertReleasedSql);
            insertStmt.setInt(1, inmate.getInmateId());
            insertStmt.setString(2, inmate.getName());
            insertStmt.setString(3, LocalDate.now().toString());
            insertStmt.setString(4, releasedBy);
            insertStmt.setString(5, notes);
            int inserted = insertStmt.executeUpdate();
            insertStmt.close();

            PreparedStatement deleteStmt = connection.prepareStatement(deleteInmateSql);
            deleteStmt.setInt(1, inmate.getInmateId());
            int deleted = deleteStmt.executeUpdate();
            deleteStmt.close();

            if (inserted > 0 && deleted > 0) {
                connection.commit();
                connection.setAutoCommit(true);
                return true;
            }

            connection.rollback();
            connection.setAutoCommit(true);
            return false;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }
    
    public void updateInmate(Inmate inmate) {
        String sql = "UPDATE inmates SET name = ?, age = ?, gender = ?, crime = ?, admission_date = ?, release_date = ?, court_date = ?, status = ?, cell_number = ?, balance = ?, last_phone_call_date = ?, last_free_phone_call_date = ?, last_paid_phone_call_date = ? WHERE inmate_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            LocalDate courtDate = inmate.getCourtDate() != null ? inmate.getCourtDate() : calculateCourtDate(inmate.getAdmissionDate(), inmate.getReleaseDate());
            inmate.setCourtDate(courtDate);

            pstmt.setString(1, inmate.getName());
            pstmt.setInt(2, inmate.getAge());
            pstmt.setString(3, inmate.getGender());
            pstmt.setString(4, inmate.getCrime());
            pstmt.setString(5, inmate.getAdmissionDate().toString());
            pstmt.setString(6, inmate.getReleaseDate() != null ? inmate.getReleaseDate().toString() : null);
            pstmt.setString(7, courtDate != null ? courtDate.toString() : null);
            pstmt.setString(8, inmate.getStatus());
            pstmt.setString(9, inmate.getCellNumber());
            pstmt.setDouble(10, inmate.getBalance());
            pstmt.setString(11, inmate.getLastPhoneCallDate() != null ? inmate.getLastPhoneCallDate().toString() : null);
            pstmt.setString(12, inmate.getLastFreePhoneCallDate() != null ? inmate.getLastFreePhoneCallDate().toString() : null);
            pstmt.setString(13, inmate.getLastPaidPhoneCallDate() != null ? inmate.getLastPaidPhoneCallDate().toString() : null);
            pstmt.setInt(14, inmate.getInmateId());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<String> getStaffNamesByRole(String role, String r2) {
        ArrayList<String> names = new ArrayList<>();
        String sql = "SELECT name FROM staff WHERE role = ? OR role = ?";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, role);
            pstmt.setString(2, r2);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
            pstmt.close();
        } 
         catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public void deleteInmate(int id) {
        String sql = "DELETE FROM inmates WHERE inmate_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Inmate extractInmateFromResultSet(ResultSet rs) throws SQLException {
        Inmate inmate = new Inmate();
        inmate.setInmateId(rs.getInt("inmate_id"));
        inmate.setName(rs.getString("name"));
        inmate.setAge(rs.getInt("age"));
        inmate.setGender(rs.getString("gender"));
        inmate.setCrime(rs.getString("crime"));
        String admissionDateStr = rs.getString("admission_date");
        if (admissionDateStr != null) {
            inmate.setAdmissionDate(LocalDate.parse(admissionDateStr));
        }
        String releaseDateStr = rs.getString("release_date");
        if (releaseDateStr != null) {
            inmate.setReleaseDate(LocalDate.parse(releaseDateStr));
        }
        String courtDateStr = rs.getString("court_date");
        if (courtDateStr != null) {
            inmate.setCourtDate(LocalDate.parse(courtDateStr));
        }
        inmate.setStatus(rs.getString("status"));
        inmate.setCellNumber(rs.getString("cell_number"));
        inmate.setBalance(rs.getDouble("balance"));
        String lastCall = rs.getString("last_phone_call_date");
        if (lastCall != null && !lastCall.isEmpty()) {
            inmate.setLastPhoneCallDate(LocalDateTime.parse(lastCall));
        }
        String lastFreeCall = rs.getString("last_free_phone_call_date");
        if (lastFreeCall != null && !lastFreeCall.isEmpty()) {
            inmate.setLastFreePhoneCallDate(LocalDateTime.parse(lastFreeCall));
        }
        String lastPaidCall = rs.getString("last_paid_phone_call_date");
        if (lastPaidCall != null && !lastPaidCall.isEmpty()) {
            inmate.setLastPaidPhoneCallDate(LocalDateTime.parse(lastPaidCall));
        }
        return inmate;
    }
    
    public void addStaff(Staff staff) {
        String sql = "INSERT INTO staff (name, role, shift, contact_info) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getShift());
            pstmt.setString(4, staff.getContactInfo());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                staff.setStaffId(rs.getInt(1));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Staff getStaff(int id) {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Staff staff = extractStaffFromResultSet(rs);
                pstmt.close();
                return staff;
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY staff_id";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                staffList.add(extractStaffFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }
    
    public void updateStaff(Staff staff) {
        String sql = "UPDATE staff SET name = ?, role = ?, shift = ?, contact_info = ? WHERE staff_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getShift());
            pstmt.setString(4, staff.getContactInfo());
            pstmt.setInt(5, staff.getStaffId());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void deleteStaff(int id) {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Staff extractStaffFromResultSet(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setStaffId(rs.getInt("staff_id"));
        staff.setName(rs.getString("name"));
        staff.setRole(rs.getString("role"));
        staff.setShift(rs.getString("shift"));
        staff.setContactInfo(rs.getString("contact_info"));
        return staff;
    }
    
    public void addWorkAssignment(WorkAssignment wa) {
        String sql = "INSERT INTO work_assignments (inmate_id, work_type, assigned_date, status, supervisor_name) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, wa.getInmateId());
            pstmt.setString(2, wa.getWorkType());
            pstmt.setString(3, wa.getAssignedDate().toString());
            pstmt.setString(4, wa.getStatus());
            pstmt.setString(5, wa.getSupervisorName());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                wa.setAssignmentId(rs.getInt(1));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public WorkAssignment getWorkAssignment(int id) {
        String sql = "SELECT * FROM work_assignments WHERE assignment_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                WorkAssignment wa = extractWorkAssignmentFromResultSet(rs);
                pstmt.close();
                return wa;
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<WorkAssignment> getAllWorkAssignments() {
        List<WorkAssignment> assignments = new ArrayList<>();
        String sql = "SELECT * FROM work_assignments ORDER BY assignment_id";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                assignments.add(extractWorkAssignmentFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignments;
    }
    
    public void updateWorkAssignment(WorkAssignment wa) {
        String sql = "UPDATE work_assignments SET inmate_id = ?, work_type = ?, assigned_date = ?, status = ?, supervisor_name = ? WHERE assignment_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, wa.getInmateId());
            pstmt.setString(2, wa.getWorkType());
            pstmt.setString(3, wa.getAssignedDate().toString());
            pstmt.setString(4, wa.getStatus());
            pstmt.setString(5, wa.getSupervisorName());
            pstmt.setInt(6, wa.getAssignmentId());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private WorkAssignment extractWorkAssignmentFromResultSet(ResultSet rs) throws SQLException {
        WorkAssignment wa = new WorkAssignment();
        wa.setAssignmentId(rs.getInt("assignment_id"));
        wa.setInmateId(rs.getInt("inmate_id"));
        wa.setWorkType(rs.getString("work_type"));
        String dateStr = rs.getString("assigned_date");
        if (dateStr != null) {
            wa.setAssignedDate(LocalDate.parse(dateStr));
        }
        wa.setStatus(rs.getString("status"));
        wa.setSupervisorName(rs.getString("supervisor_name"));
        return wa;
    }
    
    public void addResource(Resources r) {
        String sql = "INSERT INTO resources (resource_name, category, quantity, location, status) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, r.getResourceName());
            pstmt.setString(2, r.getCategory());
            pstmt.setInt(3, r.getQuantity());
            pstmt.setString(4, r.getLocation());
            pstmt.setString(5, r.getStatus());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                r.setResourceId(rs.getInt(1));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Resources getResource(int id) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Resources r = extractResourceFromResultSet(rs);
                pstmt.close();
                return r;
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Resources> getAllResources() {
        List<Resources> resourcesList = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY resource_id";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                resourcesList.add(extractResourceFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resourcesList;
    }
    
    public void updateResource(Resources r) {
        String sql = "UPDATE resources SET resource_name = ?, category = ?, quantity = ?, location = ?, status = ? WHERE resource_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, r.getResourceName());
            pstmt.setString(2, r.getCategory());
            pstmt.setInt(3, r.getQuantity());
            pstmt.setString(4, r.getLocation());
            pstmt.setString(5, r.getStatus());
            pstmt.setInt(6, r.getResourceId());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Resources extractResourceFromResultSet(ResultSet rs) throws SQLException {
        Resources r = new Resources();
        r.setResourceId(rs.getInt("resource_id"));
        r.setResourceName(rs.getString("resource_name"));
        r.setCategory(rs.getString("category"));
        r.setQuantity(rs.getInt("quantity"));
        r.setLocation(rs.getString("location"));
        r.setStatus(rs.getString("status"));
        return r;
    }
    
    public void addMedicalCheckup(MedicalCheckup mc) {
        String sql = "INSERT INTO medical_checkups (inmate_id, checkup_date, diagnosis, treatment, doctor_name, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, mc.getInmateId());
            pstmt.setString(2, mc.getCheckupDate().toString());
            pstmt.setString(3, mc.getDiagnosis());
            pstmt.setString(4, mc.getTreatment());
            pstmt.setString(5, mc.getDoctorName());
            pstmt.setString(6, mc.getNotes());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                mc.setCheckupId(rs.getInt(1));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<MedicalCheckup> getAllMedicalCheckups() {
        List<MedicalCheckup> checkups = new ArrayList<>();
        String sql = "SELECT * FROM medical_checkups ORDER BY checkup_id DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                checkups.add(extractMedicalCheckupFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkups;
    }
    
    private MedicalCheckup extractMedicalCheckupFromResultSet(ResultSet rs) throws SQLException {
        MedicalCheckup mc = new MedicalCheckup();
        mc.setCheckupId(rs.getInt("checkup_id"));
        mc.setInmateId(rs.getInt("inmate_id"));
        String dateStr = rs.getString("checkup_date");
        if (dateStr != null) {
            mc.setCheckupDate(LocalDate.parse(dateStr));
        }
        mc.setDiagnosis(rs.getString("diagnosis"));
        mc.setTreatment(rs.getString("treatment"));
        mc.setDoctorName(rs.getString("doctor_name"));
        mc.setNotes(rs.getString("notes"));
        return mc;
    }
    
    public void addIncidentReport(IncidentReport ir) {
        String sql = "INSERT INTO incident_reports (inmate_id, incident_date, incident_type, description, reported_by, action_taken) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, ir.getInmateId());
            pstmt.setString(2, ir.getIncidentDate().toString());
            pstmt.setString(3, ir.getIncidentType());
            pstmt.setString(4, ir.getDescription());
            pstmt.setString(5, ir.getReportedBy());
            pstmt.setString(6, ir.getActionTaken());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                ir.setReportId(rs.getInt(1));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<IncidentReport> getAllIncidentReports() {
        List<IncidentReport> reports = new ArrayList<>();
        String sql = "SELECT ir.*, i.release_date AS inmate_release_date FROM incident_reports ir LEFT JOIN inmates i ON i.inmate_id = ir.inmate_id ORDER BY ir.report_id DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                reports.add(extractIncidentReportFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }
    
    private IncidentReport extractIncidentReportFromResultSet(ResultSet rs) throws SQLException {
        IncidentReport ir = new IncidentReport();
        ir.setReportId(rs.getInt("report_id"));
        ir.setInmateId(rs.getInt("inmate_id"));
        String dateStr = rs.getString("incident_date");
        if (dateStr != null) {
            ir.setIncidentDate(LocalDateTime.parse(dateStr));
        }
        ir.setIncidentType(rs.getString("incident_type"));
        ir.setDescription(rs.getString("description"));
        ir.setReportedBy(rs.getString("reported_by"));
        ir.setActionTaken(rs.getString("action_taken"));
        try {
            ir.setInmateReleaseDate(rs.getString("inmate_release_date"));
        } catch (SQLException ignored) {
        }
        return ir;
    }
    
    public void addMovementRecord(MovementRecord mr) {
        String sql = "INSERT INTO movement_records (inmate_id, from_location, to_location, movement_time, reason, authorized_by) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, mr.getInmateId());
            pstmt.setString(2, mr.getFromLocation());
            pstmt.setString(3, mr.getToLocation());
            pstmt.setString(4, mr.getMovementTime().toString());
            pstmt.setString(5, mr.getReason());
            pstmt.setString(6, mr.getAuthorizedBy());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                mr.setMovementId(rs.getInt(1));
            }
            pstmt.close();

            if ("Court Appearance".equalsIgnoreCase(mr.getReason())) {
                scheduleCourtCase(mr.getInmateId(), mr.getMovementTime(), mr.getAuthorizedBy());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isMovementTimeOccupied(LocalDateTime movementTime, int inmateId) {
        String sql = "SELECT COUNT(*) FROM movement_records WHERE movement_time = ? AND inmate_id != ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, movementTime.toString());
            pstmt.setInt(2, inmateId);
            ResultSet rs = pstmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            pstmt.close();
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean hasCourtScheduleConflict(LocalDateTime targetCourtTime, int inmateId) {
        String sql = "SELECT court_time, inmate_id FROM court_schedules";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int scheduledInmate = rs.getInt("inmate_id");
                if (scheduledInmate == inmateId) {
                    continue;
                }
                LocalDateTime existing = LocalDateTime.parse(rs.getString("court_time"));
                long minutes = Math.abs(java.time.Duration.between(existing, targetCourtTime).toMinutes());
                if (minutes < 60) {
                    stmt.close();
                    return true;
                }
            }
            stmt.close();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean scheduleCourtCase(int inmateId, LocalDateTime courtTime, String scheduledBy) {
        if (hasCourtScheduleConflict(courtTime, inmateId)) {
            return false;
        }

        String sql = "INSERT INTO court_schedules (inmate_id, court_time, scheduled_by, created_at) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, inmateId);
            pstmt.setString(2, courtTime.toString());
            pstmt.setString(3, scheduledBy);
            pstmt.setString(4, LocalDateTime.now().toString());
            int inserted = pstmt.executeUpdate();
            pstmt.close();

            if (inserted > 0) {
                ActivityLogger.log("Court Scheduled", "Inmate #" + inmateId + " scheduled for court at " + courtTime);
            }
            return inserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CourtSchedule> getCourtSchedules() {
        List<CourtSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM court_schedules ORDER BY court_time DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                CourtSchedule schedule = new CourtSchedule();
                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setInmateId(rs.getInt("inmate_id"));
                String ct = rs.getString("court_time");
                if (ct != null) {
                    schedule.setCourtTime(LocalDateTime.parse(ct));
                }
                schedule.setScheduledBy(rs.getString("scheduled_by"));
                schedules.add(schedule);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public boolean assignResourceToInmate(int resourceId, int inmateId, int quantity, String assignedBy) {
        if (quantity <= 0) {
            return false;
        }

        try {
            connection.setAutoCommit(false);

            String resourceSql = "SELECT quantity, resource_name FROM resources WHERE resource_id = ?";
            PreparedStatement resourceStmt = connection.prepareStatement(resourceSql);
            resourceStmt.setInt(1, resourceId);
            ResultSet rs = resourceStmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                resourceStmt.close();
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            int available = rs.getInt("quantity");
            rs.close();
            resourceStmt.close();

            if (available < quantity) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            PreparedStatement updateStmt = connection.prepareStatement("UPDATE resources SET quantity = quantity - ? WHERE resource_id = ?");
            updateStmt.setInt(1, quantity);
            updateStmt.setInt(2, resourceId);
            updateStmt.executeUpdate();
            updateStmt.close();

            PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO resource_assignments (resource_id, inmate_id, quantity, assigned_at, assigned_by) VALUES (?, ?, ?, ?, ?)");
            insertStmt.setInt(1, resourceId);
            insertStmt.setInt(2, inmateId);
            insertStmt.setInt(3, quantity);
            insertStmt.setString(4, LocalDateTime.now().toString());
            insertStmt.setString(5, assignedBy);
            int inserted = insertStmt.executeUpdate();
            insertStmt.close();

            connection.commit();
            connection.setAutoCommit(true);
            return inserted > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }
    
    public List<MovementRecord> getAllMovementRecords() {
        List<MovementRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM movement_records ORDER BY movement_id DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                records.add(extractMovementRecordFromResultSet(rs));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    private MovementRecord extractMovementRecordFromResultSet(ResultSet rs) throws SQLException {
        MovementRecord mr = new MovementRecord();
        mr.setMovementId(rs.getInt("movement_id"));
        mr.setInmateId(rs.getInt("inmate_id"));
        mr.setFromLocation(rs.getString("from_location"));
        mr.setToLocation(rs.getString("to_location"));
        String timeStr = rs.getString("movement_time");
        if (timeStr != null) {
            mr.setMovementTime(LocalDateTime.parse(timeStr));
        }
        mr.setReason(rs.getString("reason"));
        mr.setAuthorizedBy(rs.getString("authorized_by"));
        return mr;
    }

    public List<InmateFinancialRecord> getAllInmateFinancialRecords() {
        List<InmateFinancialRecord> records = new ArrayList<>();
        String sql = "SELECT i.inmate_id, i.name, i.balance AS balance " +
                     "FROM inmates i " +
                     "ORDER BY i.inmate_id";

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                records.add(new InmateFinancialRecord(
                    rs.getInt("inmate_id"),
                    rs.getString("name"),
                    rs.getDouble("balance")
                ));
            }

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    public boolean depositToInmateAccount(int inmateId, double amount) {
        if (amount <= 0) {
            return false;
        }

        String selectSql = "SELECT balance FROM inmates WHERE inmate_id = ?";
        String updateSql = "UPDATE inmates SET balance = ? WHERE inmate_id = ?";

        try {
            connection.setAutoCommit(false);

            double currentBalance;
            PreparedStatement selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setInt(1, inmateId);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                rs.close();
                selectStmt.close();
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            currentBalance = rs.getDouble("balance");
            rs.close();
            selectStmt.close();

            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setDouble(1, currentBalance + amount);
            updateStmt.setInt(2, inmateId);
            int updated = updateStmt.executeUpdate();
            updateStmt.close();

            connection.commit();
            connection.setAutoCommit(true);
            return updated > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean purchaseCommissaryItem(int inmateId, double amount) {
        if (amount <= 0) {
            return false;
        }

        String selectSql = "SELECT balance FROM inmates WHERE inmate_id = ?";
        String updateSql = "UPDATE inmates SET balance = ? WHERE inmate_id = ?";

        try {
            connection.setAutoCommit(false);

            double currentBalance;
            PreparedStatement selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setInt(1, inmateId);
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                selectStmt.close();
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            currentBalance = rs.getDouble("balance");
            rs.close();
            selectStmt.close();

            if (currentBalance < amount) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setDouble(1, currentBalance - amount);
            updateStmt.setInt(2, inmateId);
            int updated = updateStmt.executeUpdate();
            updateStmt.close();

            connection.commit();
            connection.setAutoCommit(true);
            return updated > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    public void markPhoneCallCompleted(int inmateId) {
        String now = LocalDateTime.now().toString();
        String sql = "UPDATE inmates SET last_phone_call_date = ? WHERE inmate_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, now);
            pstmt.setInt(2, inmateId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addActivityLog(String formattedTimestamp, String action, String details) {
        String sql = "INSERT INTO activity_logs (timestamp, action, details) VALUES (?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, formattedTimestamp);
            pstmt.setString(2, action);
            pstmt.setString(3, details);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> autoReleaseDueInmates() {
        List<String> autoReleasedNames = new ArrayList<>();
        String selectSql = "SELECT inmate_id, name FROM inmates WHERE release_date IS NOT NULL AND release_date <= ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(selectSql);
            pstmt.setString(1, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Inmate inmate = new Inmate();
                inmate.setInmateId(rs.getInt("inmate_id"));
                inmate.setName(rs.getString("name"));
                if (archiveAndReleaseInmate(inmate, "AutoReleaseService", "Automatic daily release check")) {
                    autoReleasedNames.add(inmate.getName());
                }
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return autoReleasedNames;
    }

    public int countPhoneCallsInLast7Days(int inmateId, boolean paidOnly) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        String sql;
        if (paidOnly) {
            sql = "SELECT COUNT(*) FROM activity_logs WHERE details LIKE ? AND timestamp >= ? LIMIT 100";
        } else {
            sql = "SELECT COUNT(*) FROM activity_logs WHERE (action = 'Phone Call' OR action = 'Free Phone Call') AND timestamp >= ? LIMIT 100";
        }
        
        try {
            if (paidOnly) {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, "%Paid phone call%");
                pstmt.setString(2, sevenDaysAgo.toString());
                ResultSet rs = pstmt.executeQuery();
                int count = rs.next() ? rs.getInt(1) : 0;
                pstmt.close();
                return count;
            } else {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql + "'Phone Call' AND timestamp >= '" + sevenDaysAgo.toString() + "'");
                int count = rs.next() ? rs.getInt(1) : 0;
                stmt.close();
                return count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public PhoneCallEligibility getPhoneCallEligibility(int inmateId) {
        Inmate inmate = getInmate(inmateId);
        if (inmate == null) {
            return new PhoneCallEligibility(false, false);
        }

        int phoneCallsInWeek = countPhoneCallsInLast7Days(inmateId, false);
        int paidCallsInWeek = countPhoneCallsInLast7Days(inmateId, true);
        
        boolean freeAvailable = phoneCallsInWeek == 0;
        boolean paidAvailable = paidCallsInWeek == 0;
        return new PhoneCallEligibility(freeAvailable, paidAvailable);
    }

    public boolean bookFreePhoneCall(int inmateId) {
        PhoneCallEligibility eligibility = getPhoneCallEligibility(inmateId);
        if (!eligibility.isFreeCallAvailable()) {
            return false;
        }

        String sql = "UPDATE inmates SET last_phone_call_date = ?, last_free_phone_call_date = ? WHERE inmate_id = ?";
        String now = LocalDateTime.now().toString();
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, now);
            pstmt.setString(2, now);
            pstmt.setInt(3, inmateId);
            int updated = pstmt.executeUpdate();
            pstmt.close();
            
            if (updated > 0) {
                Inmate inmate = getInmate(inmateId);
                if (inmate != null) {
                    ActivityLogger.log("Phone Call", "Inmate " + inmate.getName() + " booked free phone call");
                }
            }
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean bookPaidPhoneCall(int inmateId, double callPrice) {
        PhoneCallEligibility eligibility = getPhoneCallEligibility(inmateId);
        if (!eligibility.isPaidCallAvailable()) {
            return false;
        }

        String selectSql = "SELECT balance FROM inmates WHERE inmate_id = ?";
        String updateSql = "UPDATE inmates SET balance = ?, last_phone_call_date = ?, last_paid_phone_call_date = ? WHERE inmate_id = ?";

        try {
            connection.setAutoCommit(false);

            PreparedStatement selectStmt = connection.prepareStatement(selectSql);
            selectStmt.setInt(1, inmateId);
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                selectStmt.close();
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            double currentBalance = rs.getDouble("balance");
            rs.close();
            selectStmt.close();

            if (currentBalance < callPrice) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }

            String now = LocalDateTime.now().toString();
            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setDouble(1, currentBalance - callPrice);
            updateStmt.setString(2, now);
            updateStmt.setString(3, now);
            updateStmt.setInt(4, inmateId);
            int updated = updateStmt.executeUpdate();
            updateStmt.close();

            connection.commit();
            connection.setAutoCommit(true);
            return updated > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    private boolean isOlderThan7Days(LocalDateTime timestamp, LocalDateTime now) {
        if (timestamp == null) {
            return true;
        }
        long days = ChronoUnit.DAYS.between(timestamp, now);
        return days >= 7;
    }

    public int getActiveInmateCount() {
        return getCount("SELECT COUNT(*) FROM inmates");
    }

    public int getAlertsCount() {
        return getCount("SELECT COUNT(*) FROM incident_reports WHERE action_taken IS NULL OR TRIM(action_taken) = ''");
    }

    public int getPendingCourtCasesCount() {
        return getCount("SELECT COUNT(*) FROM movement_records WHERE LOWER(reason) = 'court appearance'");
    }

    public String getCapacitySnapshot() {
        final int totalCapacity = 500;
        int activeCount = getActiveInmateCount();
        return activeCount + " / " + totalCapacity;
    }

    public List<RecentActivity> getRecentActivities(int limit) {
        List<RecentActivity> activities = new ArrayList<>();
        String sql = "SELECT timestamp AS ts, action, details FROM activity_logs ORDER BY log_id DESC LIMIT ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, Math.max(limit, 1));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                activities.add(new RecentActivity(
                    rs.getString("ts"),
                    rs.getString("action"),
                    rs.getString("details")
                ));
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    private LocalDate calculateCourtDate(LocalDate captureDate, LocalDate releaseDate) {
        if (captureDate == null || releaseDate == null || releaseDate.isBefore(captureDate)) {
            return null;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(captureDate, releaseDate);
        return captureDate.plusDays(days / 2);
    }

    private int getCount(String sql) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            int count = rs.next() ? rs.getInt(1) : 0;
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // ========== USER AUTHENTICATION METHODS  ==========

// Create users table
private void createUsersTable() {
    try {
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "status TEXT NOT NULL)");
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

public boolean addUser(User user) {
    String sql = "INSERT INTO users (username, password, email, full_name, role, created_at, status) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try {
        // Insert the user
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword());
        pstmt.setString(3, user.getEmail());
        pstmt.setString(4, user.getFullName());
        pstmt.setString(5, user.getRole());
        pstmt.setString(6, user.getCreatedAt().toString());
        pstmt.setString(7, user.getStatus());
        
        int rowsAffected = pstmt.executeUpdate();
        pstmt.close();
        
        System.out.println("Rows affected: " + rowsAffected);
        
        // If insert was successful
        if (rowsAffected > 0) {
            // Try to get the last inserted ID
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    user.setUserId(newId);
                    System.out.println("User ID set to: " + newId);
                }
                rs.close();
                stmt.close();
            } catch (Exception e) {
                System.out.println("Warning: Could not get user ID, but user was inserted successfully");
                e.printStackTrace();
            }
            
            System.out.println("User added successfully: " + user.getUsername());
            return true;  // ← RETURN TRUE HERE!
        } else {
            System.out.println("No rows affected - insert failed");
            return false;
        }
        
    } catch (SQLException e) {
        System.err.println("SQL Error adding user: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

// Authenticate user
public User authenticateUser(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    try {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            User user = new Admin();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            user.setFullName(rs.getString("full_name"));
            user.setRole(rs.getString("role"));
            
            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                user.setCreatedAt(LocalDateTime.parse(createdAtStr));
            }
            
            user.setStatus(rs.getString("status"));
            
            pstmt.close();
            return user;
        }
        
        pstmt.close();
        return null;
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}

// Check if username exists
public boolean usernameExists(String username) {
    String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
    try {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int count = rs.getInt(1);
            pstmt.close();
            return count > 0;
        }
        
        pstmt.close();
        return false;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// Check if email exists
public boolean emailExists(String email) {
    String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
    try {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, email);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int count = rs.getInt(1);
            pstmt.close();
            return count > 0;
        }
        
        pstmt.close();
        return false;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// Get all users
public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    String sql = "SELECT * FROM users ORDER BY user_id";
    try {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            User user = new Admin();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
            user.setFullName(rs.getString("full_name"));
            user.setRole(rs.getString("role"));
            
            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                user.setCreatedAt(LocalDateTime.parse(createdAtStr));
            }
            
            user.setStatus(rs.getString("status"));
            users.add(user);
        }
        
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return users;
}

// Initialize default admin user
private void initializeDefaultAdmin() {
    try {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        
        if (rs.next() && rs.getInt(1) == 0) {
            // No users exist, create default admin
            User admin = new Admin(0, "admin", "admin123", "admin@prison.com",  "System Administrator", 
                                LocalDateTime.now(), "Active");
            addUser(admin);
            System.out.println("Default admin user created - Username: admin, Password: admin123");
        }
        
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}