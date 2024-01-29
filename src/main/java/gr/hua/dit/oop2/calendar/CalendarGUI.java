package gr.hua.dit.oop2.calendar;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CalendarGUI {

    private Map<String, DefaultListModel<String>> eventLists;
    private JComboBox<String> listSelector;
    private JList<String> eventsList;
    private JLabel currentTimeLabel;

    // Added enumeration for sorting options
    private enum SortOption {
        BY_TIME, BY_NAME
    }

    // Current sort option
    private SortOption currentSortOption = SortOption.BY_TIME;

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

        eventsList = new JList<>();
        JScrollPane listScrollPane = new JScrollPane(eventsList);

        currentTimeLabel = new JLabel();
        Timer clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimeLabel();
            }
        });
        clockTimer.start();

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
                    updateEventList(currentListModel.toArray());
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

        JButton changeListButton = new JButton("Change List");
        changeListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showChangeListDialog();
            }
        });

        JButton sortButton = new JButton("Sort Events");
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSortOptionsDialog();
            }
        });

        Timer reminderTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkReminders();
            }
        });
        reminderTimer.start();

        frame.getContentPane().setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(listSelector);
        topPanel.add(loadButton);
        topPanel.add(newEventButton);
        topPanel.add(editEventButton);
        topPanel.add(completeTaskButton);
        topPanel.add(changeListButton);
        topPanel.add(sortButton);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(listScrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(currentTimeLabel, BorderLayout.SOUTH);

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
        DefaultListModel<String> currentListModel = new DefaultListModel<>();
        for (Object event : events) {
            currentListModel.addElement((String) event);
        }
        eventsList.setModel(currentListModel);

        // Sort the events based on the current sort option
        sortEvents(currentListModel);
    }

    private void sortEvents(DefaultListModel<String> model) {
        switch (currentSortOption) {
            case BY_TIME:
                sortByTime(model);
                break;
            case BY_NAME:
                sortByName(model);
                break;
            default:
                // Default to sorting by time
                sortByTime(model);
                break;
        }
    }

    private void sortByTime(DefaultListModel<String> model) {
        List<String> events = Collections.list(model.elements());
        events.sort(Comparator.comparing(this::getEventTime));
        model.clear();
        for (String event : events) {
            model.addElement(event);
        }
    }

    private void sortByName(DefaultListModel<String> model) {
        List<String> events = Collections.list(model.elements());
        events.sort(Comparator.naturalOrder());
        model.clear();
        for (String event : events) {
            model.addElement(event);
        }
    }

    private String getEventTime(String event) {
        // Extract and return the time part of the event (assuming the time is at the beginning)
        return event.substring(0, 5);
    }

    private void updateTimeLabel() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        currentTimeLabel.setText("Current Time: " + timeFormat.format(new Date()));
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

    private void showChangeListDialog() {
        Object[] options = eventLists.keySet().toArray();
        String selectedList = (String) JOptionPane.showInputDialog(null,
                "Select List:", "Change List", JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (selectedList != null) {
            listSelector.setSelectedItem(selectedList);
            updateEventList(eventLists.get(selectedList).toArray());
        }
    }

    private void showSortOptionsDialog() {
        Object[] options = {"Sort by Time", "Sort by Name"};
        int selectedOption = JOptionPane.showOptionDialog(null,
                "Select Sorting Option:", "Sort Events", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (selectedOption == 0) {
            currentSortOption = SortOption.BY_TIME;
        } else if (selectedOption == 1) {
            currentSortOption = SortOption.BY_NAME;
        }

        // Re-sort events based on the new sort option
        DefaultListModel<String> currentListModel = eventLists.get(listSelector.getSelectedItem());
        sortEvents(currentListModel);
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
                    String eventTimeStr = event.substring(0, 5);
                    Date eventTime = dateFormat.parse(eventTimeStr);

                    if (now.before(eventTime)) {
                        long timeDiff = eventTime.getTime() - now.getTime();
                        long minutesUntilEvent = timeDiff / (60 * 1000);

                        if (minutesUntilEvent <= 5) {
                            JOptionPane.showMessageDialog(null, "Reminder: " + event);
                        }
                    }
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }
            }
        }
    }
}