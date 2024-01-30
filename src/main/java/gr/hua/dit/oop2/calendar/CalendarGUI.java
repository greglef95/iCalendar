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
    private JLabel currentDateLabel;

    private enum SortOption {
        BY_TIME, BY_NAME
    }

    private SortOption currentSortOption = SortOption.BY_TIME;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalendarGUI app = new CalendarGUI();
            app.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("iCalendar App by Greg, Giannis and Christos");
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
        currentDateLabel = new JLabel();
        updateDateLabel();
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

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(listSelector);
        topPanel.add(loadButton);
        topPanel.add(newEventButton);
        topPanel.add(editEventButton);
        topPanel.add(completeTaskButton);
        topPanel.add(sortButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(currentDateLabel, BorderLayout.NORTH);
        bottomPanel.add(currentTimeLabel, BorderLayout.SOUTH);


        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(listScrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

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

        sortEvents(currentListModel);
    }

    private void updateTimeLabel() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        currentTimeLabel.setText("Current Time: " + timeFormat.format(new Date()));
    }

    private void updateDateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
        currentDateLabel.setText("Current Date: " + dateFormat.format(new Date()));
    }

    private void showNewEventDialog() {
        JDialog newEventDialog = new JDialog();
        newEventDialog.setTitle("New Event");

        JTextField eventNameField = new JTextField(20);
        JTextField eventTimeField = new JTextField(5);
        JTextField eventDateField = new JTextField(10);

        JButton addButton = new JButton("Add Event");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String eventName = eventNameField.getText();
                String eventTime = eventTimeField.getText();
                String eventDate = eventDateField.getText();

                if (!eventName.isEmpty() && isValidTimeFormat(eventTime) && isValidDateFormat(eventDate)) {
                    String newEvent = eventDate + " " + eventTime + " " + eventName;
                    String selectedList = (String) listSelector.getSelectedItem();
                    DefaultListModel<String> currentListModel = eventLists.get(selectedList);
                    currentListModel.addElement(newEvent);

                    updateEventList(currentListModel.toArray());
                    newEventDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(newEventDialog, "Invalid input. Event name cannot be empty, " +
                            "time should be in HH:mm format, and date should be in yyyy-MM-dd format.");
                }
            }
        });

        newEventDialog.setLayout(new FlowLayout());
        newEventDialog.add(new JLabel("Event Name:"));
        newEventDialog.add(eventNameField);
        newEventDialog.add(new JLabel("Event Time (HH:mm):"));
        newEventDialog.add(eventTimeField);
        newEventDialog.add(new JLabel("Event Date (yyyy-MM-dd):"));
        newEventDialog.add(eventDateField);
        newEventDialog.add(addButton);

        newEventDialog.setSize(350, 150);
        newEventDialog.setLocationRelativeTo(null);
        newEventDialog.setVisible(true);
    }

    private boolean isValidTimeFormat(String time) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            timeFormat.parse(time);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidDateFormat(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);

            dateFormat.parse(date);

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void showEditEventDialog() {
        String selectedList = (String) listSelector.getSelectedItem();
        DefaultListModel<String> currentListModel = eventLists.get(selectedList);

        if (currentListModel.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No events to edit.");
            return;
        }

        String selectedEvent = eventsList.getSelectedValue();

        if (selectedEvent != null) {
            String editedEvent = JOptionPane.showInputDialog("Edit Event:", selectedEvent);

            if (editedEvent != null && !editedEvent.isEmpty()) {
                currentListModel.setElementAt(editedEvent, eventsList.getSelectedIndex());
                updateEventList(currentListModel.toArray());
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

        String selectedEvent = eventsList.getSelectedValue();

        if (selectedEvent != null) {
            currentListModel.removeElement(selectedEvent);
            updateEventList(currentListModel.toArray());
        }
    }
/*
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
*/
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

        DefaultListModel<String> currentListModel = eventLists.get(listSelector.getSelectedItem());
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
        return event.substring(0, 5);
    }

    private void checkReminders() {
        String selectedList = (String) listSelector.getSelectedItem();
        DefaultListModel<String> currentListModel = eventLists.get(selectedList);

        if (!currentListModel.isEmpty()) {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (int i = 0; i < currentListModel.getSize(); i++) {
                String event = currentListModel.getElementAt(i);
                try {
                    String eventTimeStr = event.substring(0, 16);
                    Date eventTime = dateFormat.parse(eventTimeStr);

                    if (now.before(eventTime)) {
                        long timeDiff = eventTime.getTime() - now.getTime();
                        long minutesUntilEvent = timeDiff / (60 * 1000);

                        if (minutesUntilEvent <= 5) {
                            JOptionPane.showMessageDialog(null, "Reminder: " + event);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}