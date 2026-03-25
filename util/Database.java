package com.prison.util;

import com.prison.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            initializeSampleData();
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
            
            addInmate(new Inmate(0, "John Doe", 35, "Male", "Theft", LocalDate.now().minusMonths(6), LocalDate.now().plusYears(2), "Active", "A-101"));
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
        String sql = "INSERT INTO inmates (name, age, gender, crime, admission_date, release_date, status, cell_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, inmate.getName());
            pstmt.setInt(2, inmate.getAge());
            pstmt.setString(3, inmate.getGender());
            pstmt.setString(4, inmate.getCrime());
            pstmt.setString(5, inmate.getAdmissionDate().toString());
            pstmt.setString(6, inmate.getReleaseDate() != null ? inmate.getReleaseDate().toString() : null);
            pstmt.setString(7, inmate.getStatus());
            pstmt.setString(8, inmate.getCellNumber());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                inmate.setInmateId(rs.getInt(1));
            }
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
    
    public void updateInmate(Inmate inmate) {
        String sql = "UPDATE inmates SET name = ?, age = ?, gender = ?, crime = ?, admission_date = ?, release_date = ?, status = ?, cell_number = ? WHERE inmate_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, inmate.getName());
            pstmt.setInt(2, inmate.getAge());
            pstmt.setString(3, inmate.getGender());
            pstmt.setString(4, inmate.getCrime());
            pstmt.setString(5, inmate.getAdmissionDate().toString());
            pstmt.setString(6, inmate.getReleaseDate() != null ? inmate.getReleaseDate().toString() : null);
            pstmt.setString(7, inmate.getStatus());
            pstmt.setString(8, inmate.getCellNumber());
            pstmt.setInt(9, inmate.getInmateId());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        inmate.setStatus(rs.getString("status"));
        inmate.setCellNumber(rs.getString("cell_number"));
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
        String sql = "SELECT * FROM incident_reports ORDER BY report_id DESC";
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
        } catch (SQLException e) {
            e.printStackTrace();
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
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}