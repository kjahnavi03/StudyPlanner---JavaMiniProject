package StudyPlanner;

import java.util.*;
import java.sql.*;
import javax.swing.JOptionPane;

public class StudyPlannerApp {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<StudyList> sl = new ArrayList<>();
    static final String URL = "jdbc:sqlite:studyplanner.db";

    static class StudyList {
        private String subject;
        private String topic;
        private boolean status;

        public StudyList(String subject, String topic) {
            this.subject = subject;
            this.topic = topic;
            this.status = false;
        }

        public void markAsCompleted() {
            this.status = true;
        }

        public boolean isCompleted() {
            return status;
        }

        public String toString() {
            return "Subject: " + subject +
                   "\nTopic: " + topic +
                   "\nStatus: " + (status ? "Completed." : "Pending.");
        }
    }

    public static void main(String[] args) {
        initializeDB();

        int choice;
        do {
            System.out.println("\n-------------- STUDY PLANNER -----------");
            System.out.println("1) Add Plan");
            System.out.println("2) View Plans");
            System.out.println("3) Mark As Completed");
            System.out.println("4) Show Daily Study Alert");
            System.out.println("5) Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: addTopic(); break;
                case 2: viewTopic(); break;
                case 3: markAsCompleted(); break;
                case 4: showDailyPopup(); break;
                case 5: System.out.println("Thank you!!"); break;
                default: System.out.println("Invalid choice!!");
            }
        } while (choice != 5);
    }

    static void addTopic() {
        System.out.print("Enter Subject: ");
        String subject = sc.nextLine();
        System.out.print("Enter Topic: ");
        String topic = sc.nextLine();

        StudyList newTopic = new StudyList(subject, topic);
        sl.add(newTopic);
        insertPlan(subject, topic);
        System.out.println("Study topic added!");
    }

    static void viewTopic() {
        if (sl.isEmpty()) {
            System.out.println("No topics added yet.");
        } else {
            System.out.println("\n-------- STUDY PLAN -----------");
            for (int i = 0; i < sl.size(); i++) {
                System.out.println("ID: " + (i + 1));
                System.out.println(sl.get(i));
                System.out.println();
            }
        }
    }
    
    static void markAsCompleted() {
        System.out.print("Enter Topic ID to mark as completed: ");
        int id = sc.nextInt();
        sc.nextLine();

        if (id >= 1 && id <= sl.size()) {
            sl.get(id - 1).markAsCompleted();
            markCompletedInDB(id);
            System.out.println("Topic marked as completed!");
        } else {
            System.out.println("Invalid topic ID.");
        }
    }

    static void showDailyPopup() {
        if (sl.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No study topics added yet.", "Daily Alert", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder plan = new StringBuilder("Today's Plan:\n\n");
        for (int i = 0; i < sl.size(); i++) {
            if (!sl.get(i).isCompleted()) {
                plan.append("ID: ").append(i + 1).append("\n");
                plan.append(sl.get(i)).append("\n\n");
            }
        }

        if (plan.toString().equals("Today's Plan:\n\n")) {
            plan.append("All topics completed. Great job!");
        }

        JOptionPane.showMessageDialog(null, plan.toString(), "Daily Study Alert", JOptionPane.INFORMATION_MESSAGE);
    }

    static void initializeDB() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String createTable = "CREATE TABLE IF NOT EXISTS study_plans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "subject TEXT NOT NULL," +
                    "topic TEXT NOT NULL," +
                    "status TEXT NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(createTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void insertPlan(String subject, String topic) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String sql = "INSERT INTO study_plans (subject, topic, status) VALUES (?, ?, 'Pending')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, subject);
            pstmt.setString(2, topic);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void markCompletedInDB(int id) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            String sql = "UPDATE study_plans SET status = 'Completed' WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
