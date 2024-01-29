package gr.hua.dit.oop2.calendar;//package gr.hua.dit.oop2.calendar;
//
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.VBox;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import java.io.File;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CalendarGUI extends Application {
//
//    private List<Event> events = new ArrayList<>();
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Calendar App");
//
//        // UI Components
//        ListView<Event> eventListView = new ListView<>();
//        Button loadButton = new Button("Load Events");
//        Button addButton = new Button("Add Event");
//        Button editButton = new Button("Edit Event");
//        Button completeButton = new Button("Complete Event");
//
//        // Event Listeners
//        loadButton.setOnAction(e -> loadEvents(primaryStage, eventListView));
//        addButton.setOnAction(e -> addEvent(primaryStage, eventListView));
//        editButton.setOnAction(e -> editEvent(primaryStage, eventListView));
//        completeButton.setOnAction(e -> completeEvent(eventListView));
//
//        // Layout
//        VBox vbox = new VBox(loadButton, eventListView, addButton, editButton, completeButton);
//        Scene scene = new Scene(vbox, 400, 300);
//
//        // Reminder thread
//        Thread reminderThread = new Thread(() -> {
//            while (true) {
//                checkReminders();
//                try {
//                    Thread.sleep(60000); // Check every minute
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//        reminderThread.setDaemon(true);
//        reminderThread.start();
//
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    private void loadEvents(Stage primaryStage, ListView<Event> eventListView) {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Open Events File");
//        File file = fileChooser.showOpenDialog(primaryStage);
//
//        if (file != null) {
//            // TODO: Read events from the file and populate the events list
//            // For now, let's add some dummy data
//            events.add(new Event("Event 1", LocalDateTime.now().plusDays(1)));
//            events.add(new Event("Event 2", LocalDateTime.now().plusDays(2)));
//            events.add(new Event("Event 3", LocalDateTime.now().plusDays(3)));
//            updateEventListView(eventListView);
//        }
//    }
//
//    private void addEvent(Stage primaryStage, ListView<Event> eventListView) {
//        // TODO: Implement the logic to add a new event
//        // You can use a dialog to get user input for the new event
//        // For now, let's add a dummy event
//        events.add(new Event("New Event", LocalDateTime.now().plusDays(1)));
//        updateEventListView(eventListView);
//    }
//
//    private void editEvent(Stage primaryStage, ListView<Event> eventListView) {
//        // TODO: Implement the logic to edit an existing event
//        // You can use a dialog to get user input for editing the event
//        // For now, let's edit the first event
//        if (!events.isEmpty()) {
//            Event firstEvent = events.get(0);
//            firstEvent.setName("Edited Event");
//            updateEventListView(eventListView);
//        }
//    }
//
//    private void completeEvent(ListView<Event> eventListView) {
//        // TODO: Implement the logic to mark an event as completed
//        // For now, let's complete the first event
//        if (!events.isEmpty()) {
//            Event firstEvent = events.get(0);
//            firstEvent.setCompleted(true);
//            updateEventListView(eventListView);
//        }
//    }
//
//    private void checkReminders() {
//        LocalDateTime now = LocalDateTime.now();
//        for (Event event : events) {
//            if (!event.isCompleted() && now.isAfter(event.getDateTime())) {
//                System.out.println("Reminder: Event '" + event.getName() + "' is upcoming!");
//            }
//        }
//    }
//
//    private void updateEventListView(ListView<Event> eventListView) {
//        eventListView.getItems().clear();
//        eventListView.getItems().addAll(events);
//    }
//
//    private static class Event {
//        private String name;
//        private LocalDateTime dateTime;
//        private boolean completed;
//
//        public Event(String name, LocalDateTime dateTime) {
//            this.name = name;
//            this.dateTime = dateTime;
//            this.completed = false;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public LocalDateTime getDateTime() {
//            return dateTime;
//        }
//
//        public boolean isCompleted() {
//            return completed;
//        }
//
//        public void setCompleted(boolean completed) {
//            this.completed = completed;
//        }
//
//        @Override
//        public String toString() {
//            return name + " - " + dateTime + (completed ? " (Completed)" : "");
//        }
//    }
//}
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CalendarGUI {

    private Map<String, DefaultListModel<String>> eventLists;
    private JComboBox<String> listSelector;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalendarGUI app = new CalendarGUI();
            app.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Event Loader App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        eventLists = new HashMap<>();
        eventLists.put("Default", new DefaultListModel<>());

        listSelector = new JComboBox<>(eventLists.keySet().toArray(new String[0]));
        listSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedList = (String) listSelector.getSelectedItem();
                updateEventList(eventLists.get(selectedList).toArray());
            }
        });

        JButton loadButton = new JButton("Load Events");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    List<String> events = loadEventsFromFile(selectedFile);

                    String selectedList = (String) listSelector.getSelectedItem();
                    DefaultListModel<String> currentListModel = eventLists.get(selectedList);
                    currentListModel.clear();
                    for (String event : events) {
                        currentListModel.addElement(event);
                    }
                }
            }
        });

        JButton newEventButton = new JButton("New Event");
        newEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNewEventDialog();
            }
        });

        JButton editEventButton = new JButton("Edit Event");
        editEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEditEventDialog();
            }
        });

        JButton completeTaskButton = new JButton("Complete Task");
        completeTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completeTask();
            }
        });

        Timer reminderTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkReminders();
            }
        });
        reminderTimer.start();

        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(listSelector);
        frame.getContentPane().add(loadButton);
        frame.getContentPane().add(newEventButton);
        frame.getContentPane().add(editEventButton);
        frame.getContentPane().add(completeTaskButton);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private List<String> loadEventsFromFile(File file) {
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void updateEventList(Object[] events) {
        eventLists.get(listSelector.getSelectedItem()).clear();
        for (Object event : events) {
            eventLists.get(listSelector.getSelectedItem()).addElement((String) event);
        }
    }

    private void showNewEventDialog() {
        JDialog newEventDialog = new JDialog();
        newEventDialog.setTitle("New Event");

        JTextField eventNameField = new JTextField(20);
        JButton addButton = new JButton("Add Event");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String eventName = eventNameField.getText();
                if (!eventName.isEmpty()) {
                    String selectedList = (String) listSelector.getSelectedItem();
                    eventLists.get(selectedList).addElement(eventName);
                    newEventDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(newEventDialog, "Event name cannot be empty.");
                }
            }
        });

        newEventDialog.setLayout(new FlowLayout());
        newEventDialog.add(new JLabel("Event Name:"));
        newEventDialog.add(eventNameField);
        newEventDialog.add(addButton);

        newEventDialog.setSize(300, 150);
        newEventDialog.setLocationRelativeTo(null);
        newEventDialog.setVisible(true);
    }

    private void showEditEventDialog() {
        String selectedList = (String) listSelector.getSelectedItem();
        DefaultListModel<String> currentListModel = eventLists.get(selectedList);

        if (currentListModel.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No events to edit.");
            return;
        }

        String selectedEvent = (String) JOptionPane.showInputDialog(null,
                "Select Event to Edit:", "Edit Event", JOptionPane.QUESTION_MESSAGE,
                null, currentListModel.toArray(), currentListModel.get(0));

        if (selectedEvent != null) {
            String editedEvent = JOptionPane.showInputDialog("Edit Event:", selectedEvent);
            if (editedEvent != null && !editedEvent.isEmpty()) {
                currentListModel.setElementAt(editedEvent, currentListModel.indexOf(selectedEvent));
            }
        }
    }

    private void completeTask() {
        String selectedList = (String) listSelector.getSelectedItem();
        DefaultListModel<String> currentListModel = eventLists.get(selectedList);

        if (currentListModel.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No tasks to complete.");
            return;
        }

        String selectedEvent = (String) JOptionPane.showInputDialog(null,
                "Select Task to Complete:", "Complete Task", JOptionPane.QUESTION_MESSAGE,
                null, currentListModel.toArray(), currentListModel.get(0));

        if (selectedEvent != null) {
            currentListModel.removeElement(selectedEvent);
        }
    }

    private void checkReminders() {
        String selectedList = (String) listSelector.getSelectedItem();
        DefaultListModel<String> currentListModel = eventLists.get(selectedList);

        if (!currentListModel.isEmpty()) {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            for (int i = 0; i < currentListModel.getSize(); i++) {
                String event = currentListModel.getElementAt(i);
                try {
                    String eventTimeStr = event.substring(0, 5); // Assuming time is at the beginning (e.g., "12:30 Event")
                    Date eventTime = dateFormat.parse(eventTimeStr);

                    if (now.before(eventTime)) {
                        long timeDiff = eventTime.getTime() - now.getTime();
                        long minutesUntilEvent = timeDiff / (60 * 1000);

                        if (minutesUntilEvent <= 5) {
                            JOptionPane.showMessageDialog(null, "Reminder: " + event);
                        }
                    }
                } catch (Exception e) {
                    // Handle parsing exceptions or unexpected event format
                }
            }
        }
    }
}
