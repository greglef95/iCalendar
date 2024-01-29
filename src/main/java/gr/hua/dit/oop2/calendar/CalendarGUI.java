package gr.hua.dit.oop2.calendar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarGUI extends Application {

    private List<Event> events = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Calendar App");

        // UI Components
        ListView<Event> eventListView = new ListView<>();
        Button loadButton = new Button("Load Events");
        Button addButton = new Button("Add Event");
        Button editButton = new Button("Edit Event");
        Button completeButton = new Button("Complete Event");

        // Event Listeners
        loadButton.setOnAction(e -> loadEvents(primaryStage, eventListView));
        addButton.setOnAction(e -> addEvent(primaryStage, eventListView));
        editButton.setOnAction(e -> editEvent(primaryStage, eventListView));
        completeButton.setOnAction(e -> completeEvent(eventListView));

        // Layout
        VBox vbox = new VBox(loadButton, eventListView, addButton, editButton, completeButton);
        Scene scene = new Scene(vbox, 400, 300);

        // Reminder thread
        Thread reminderThread = new Thread(() -> {
            while (true) {
                checkReminders();
                try {
                    Thread.sleep(60000); // Check every minute
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        reminderThread.setDaemon(true);
        reminderThread.start();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadEvents(Stage primaryStage, ListView<Event> eventListView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Events File");
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            // TODO: Read events from the file and populate the events list
            // For now, let's add some dummy data
            events.add(new Event("Event 1", LocalDateTime.now().plusDays(1)));
            events.add(new Event("Event 2", LocalDateTime.now().plusDays(2)));
            events.add(new Event("Event 3", LocalDateTime.now().plusDays(3)));
            updateEventListView(eventListView);
        }
    }

    private void addEvent(Stage primaryStage, ListView<Event> eventListView) {
        // TODO: Implement the logic to add a new event
        // You can use a dialog to get user input for the new event
        // For now, let's add a dummy event
        events.add(new Event("New Event", LocalDateTime.now().plusDays(1)));
        updateEventListView(eventListView);
    }

    private void editEvent(Stage primaryStage, ListView<Event> eventListView) {
        // TODO: Implement the logic to edit an existing event
        // You can use a dialog to get user input for editing the event
        // For now, let's edit the first event
        if (!events.isEmpty()) {
            Event firstEvent = events.get(0);
            firstEvent.setName("Edited Event");
            updateEventListView(eventListView);
        }
    }

    private void completeEvent(ListView<Event> eventListView) {
        // TODO: Implement the logic to mark an event as completed
        // For now, let's complete the first event
        if (!events.isEmpty()) {
            Event firstEvent = events.get(0);
            firstEvent.setCompleted(true);
            updateEventListView(eventListView);
        }
    }

    private void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (Event event : events) {
            if (!event.isCompleted() && now.isAfter(event.getDateTime())) {
                System.out.println("Reminder: Event '" + event.getName() + "' is upcoming!");
            }
        }
    }

    private void updateEventListView(ListView<Event> eventListView) {
        eventListView.getItems().clear();
        eventListView.getItems().addAll(events);
    }

    private static class Event {
        private String name;
        private LocalDateTime dateTime;
        private boolean completed;

        public Event(String name, LocalDateTime dateTime) {
            this.name = name;
            this.dateTime = dateTime;
            this.completed = false;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        @Override
        public String toString() {
            return name + " - " + dateTime + (completed ? " (Completed)" : "");
        }
    }
}
