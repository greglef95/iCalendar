package gr.hua.dit.oop2.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import biweekly.property.*;
import gr.hua.dit.oop2.calendar.Calendar;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import javax.swing.*;
import java.io.IOException;

public class CalendarGUI extends JFrame {

    private JTextArea textArea;

    public CalendarGUI() {
        // Set up the frame
        super("iCalendar App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Create a text area to display events and tasks
        textArea = new JTextArea();
        textArea.setEditable(false);

        // Create a scroll pane to hold the text area
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Add the scroll pane to the frame
        getContentPane().add(scrollPane);

        // Load and display events for the current day (you may want to change the file path)
        try {
            displayDayEvents("your_ical_file.ics");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayDayEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display events for the current day
        calendar.findDayEvents(filePath);

        // Get the events and tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VEvent event : calendar.sortedEvents()) {
            displayText.append(getEventString(event)).append("\n");
        }
        for (VTodo task : calendar.sortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the events and tasks in the text area
        textArea.setText(displayText.toString());
    }

    private String getEventString(VEvent event) {
        // Customize this method to format the event information as needed
        return "Event: " + event.getSummary().getValue() +
                "\nStart Date and Time: " + event.getDateStart().getValue() +
                "\nEnd Date and Time: " + (event.getDateEnd() != null ? event.getDateEnd().getValue() : "") +
                "\nDescription: " + event.getDescription().getValue() +
                "\n-----------------------";
    }

    private String getTaskString(VTodo task) {
        // Customize this method to format the task information as needed
        return "Task: " + task.getSummary().getValue() +
                "\nDeadline of the task: " + task.getDateDue().getValue() +
                "\nStatus of the task: " + task.getStatus().getValue() +
                "\nDescription: " + task.getDescription().getValue() +
                "\n-----------------------";
    }

    private void displayMonthEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display events for the current month
        calendar.findMonthEvents(filePath);

        // Get the events and tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VEvent event : calendar.sortedEvents()) {
            displayText.append(getEventString(event)).append("\n");
        }
        for (VTodo task : calendar.sortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the events and tasks in the text area
        textArea.setText(displayText.toString());
    }

    private void displayPastDayEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display events for the past day
        calendar.findPastDay(filePath);

        // Get the events and tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VEvent event : calendar.getSortedEvents()) {
            displayText.append(getEventString(event)).append("\n");
        }
        for (VTodo task : calendar.getSortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the events and tasks in the text area
        textArea.setText(displayText.toString());
    }

    private void displayPastWeekEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display events for the past week
        calendar.findPastWeek(filePath);

        // Get the events and tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VEvent event : calendar.getSortedEvents()) {
            displayText.append(getEventString(event)).append("\n");
        }
        for (VTodo task : calendar.getSortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the events and tasks in the text area
        textArea.setText(displayText.toString());
    }

    private void displayPastMonthEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display events for the past month
        calendar.findPastMonth(filePath);

        // Get the events and tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VEvent event : calendar.getSortedEvents()) {
            displayText.append(getEventString(event)).append("\n");
        }
        for (VTodo task : calendar.getSortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the events and tasks in the text area
        textArea.setText(displayText.toString());
    }

    private void displayToDoEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display todo tasks
        calendar.findToDoEvents(filePath);

        // Get the tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VTodo task : calendar.getSortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the tasks in the text area
        textArea.setText(displayText.toString());
    }

    private void displayDueEvents(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to find and display due events
        calendar.findDueEvents(filePath);

        // Get the tasks as strings
        StringBuilder displayText = new StringBuilder();
        for (VTodo task : calendar.getSortedTasks()) {
            displayText.append(getTaskString(task)).append("\n");
        }

        // Display the tasks in the text area
        textArea.setText(displayText.toString());
    }

    private void createNewEventTask(String filePath) throws IOException {
        // Create an instance of your Calendar class
        Calendar calendar = new Calendar();

        // Call the method to allow the user to create a new event or task
        calendar.createEvent(filePath);

        // Refresh the display after creating a new event/task
        displayAllEvents(filePath);
    }

    private void displayAllEvents(String filePath) throws IOException {
        // Method to display all events, you can implement it as per your requirements
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalendarGUI calendarGUI = new CalendarGUI();
            calendarGUI.setVisible(true);
        });
    }
}
