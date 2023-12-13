package gr.hua.dit.oop2.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import biweekly.property.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import gr.hua.dit.oop2.calendar.TimeService;
import gr.hua.dit.oop2.calendar.TimeTeller;


public class Calendar {

    private List<VEvent> events;

    private List<VEvent> sortedEvents;

    private List<VTodo> tasks;

    private List<VTodo> sortedTasks;

    public Calendar() {
    }

    // read the ical file
    private static String readFile(String filePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        return new String(encoded);
    }

    // change the format from DateStart to LocalDateTime
    static LocalDateTime convertDateStartToLocalDateTime(DateStart dateStart) {
        Date date = dateStart.getValue();
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    // change the format from DateDue to LocalDateTime
    static LocalDateTime convertDateDueToLocalDateTime(DateDue dateDue) {
        Date date = dateDue.getValue();
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    // change the format from DateEnd to LocalDateTime
    static LocalDateTime convertDateEndToLocalDateTime(DateEnd dateEnd) {
        Date date = dateEnd.getValue();
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    // this is the method that is called from the others to display each time the right events
    public void displayEvents(List<VEvent> events, List<VTodo> tasks, String message) {
        System.out.println("\n*** " + message + " ***\n");

        for (VEvent event : events) {
            Summary summary = event.getSummary();
            DateStart dateStart = event.getDateStart();
            DateEnd dateEnd = event.getDateEnd();
            Description description = event.getDescription();

            System.out.println("**********************");
            System.out.println("Title: " + summary.getValue());
            System.out.println("Description: " + description.getValue());
            System.out.println("Start Date and Time: " + dateStart.getValue());
            if (dateEnd != null) {
                System.out.println("End Date and Time: " + dateEnd.getValue());
            }
            System.out.println("**********************");
        }

        for (VTodo task : tasks) {
            Summary summary = task.getSummary();
            DateDue dateDue = task.getDateDue();
            Description description = task.getDescription();
            Status status = task.getStatus();

            System.out.println("**********************");
            System.out.println("Title: " + summary.getValue());
            System.out.println("Description: " + description.getValue());
            System.out.println("Deadline of the task: " + dateDue.getValue());
            System.out.println("Status of the task: " + status.getValue());
            System.out.println("**********************");

        }
    }

    // Method to fetch events for the current day
    public void findDayEvents(String filePath) throws IOException {
        // Fetch events and tasks for the current day and display them

        // Parse the iCalendar file
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // Initialize the TimeTeller using the TimeService to get the current date and time
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now().toLocalDate().atStartOfDay();

        // Filter events for the current day
        events = Optional.ofNullable(ical)
                .map(ICalendar::getEvents)
                .stream()
                .flatMap(Collection::stream)
                .filter(event -> {
                    // Convert start and end dates of the event to LocalDateTime
                    LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());
                    LocalDateTime eventEndDateTime = event.getDateEnd() != null ? convertDateEndToLocalDateTime(event.getDateEnd()) : null;

                    // Get dates for comparison
                    LocalDate startDate = eventStartDateTime.toLocalDate();
                    LocalDate endDate = eventEndDateTime != null ? eventEndDateTime.toLocalDate() : null;
                    LocalDate currentDate = dateTimeNow.toLocalDate();

                    // Filter events falling within the current day
                    return endDate != null ?
                            startDate.isEqual(currentDate) || endDate.isEqual(currentDate) :
                            startDate.isEqual(currentDate);
                })
                .collect(Collectors.toList());

        // Filter tasks for the current day
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> {
                    // Convert the due date of the task to LocalDateTime
                    LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());
                    return taskDueDateTime.toLocalDate().isEqual(dateTimeNow.toLocalDate());
                })
                .collect(Collectors.toList());

        // If iCalendar is not null, stop the TimeService and display the sorted events and tasks
        if (ical != null) {
            TimeService.stop();

            // Sort the events and tasks by date
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);

            // Display the sorted events and tasks for today
            displayEvents(sortedEvents, sortedTasks, "Today's events");
        } else {
            // If iCalendar is null, display an error message
            System.out.println("Invalid or empty iCal file.");
        }
    }


    // Method to fetch events for the current week
    public void findWeekEvents(String filePath) throws IOException {
        // Fetch events and tasks for the current week and display them

        // Parse the iCalendar file
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // Get the current date and time using TimeService and TimeTeller
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        // Filter events for the current week using streams and collect them
        events = Optional.ofNullable(ical)
                .map(ICalendar::getEvents)
                .stream()
                .flatMap(Collection::stream)
                .filter(event -> {
                    // Get start and end date of the event
                    LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());
                    LocalDateTime eventEndDateTime = event.getDateEnd() != null ? convertDateEndToLocalDateTime(event.getDateEnd()) : null;

                    // Calculate the start and end of the current week
                    LocalDateTime startDate = dateTimeNow.with(dateTimeNow.getDayOfWeek()).withHour(dateTimeNow.getHour())
                            .withMinute(dateTimeNow.getMinute()).withSecond(dateTimeNow.getSecond());
                    LocalDateTime endOfWeek = dateTimeNow.with(DayOfWeek.SUNDAY).plusDays(1).withHour(23).withMinute(59).withSecond(59);

                    // Filter events within the current week
                    return (eventEndDateTime != null && ((eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfWeek)) ||
                            (eventEndDateTime.isAfter(startDate) && eventEndDateTime.isBefore(endOfWeek)))) ||
                            (eventEndDateTime == null && eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfWeek));
                })
                .collect(Collectors.toList());

        // Filter tasks for the current week using streams and collect them
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> {
                    // Get the due date of the task
                    LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                    // Calculate the start and end of the current week
                    LocalDateTime startDate = dateTimeNow.with(dateTimeNow.getDayOfWeek()).withHour(dateTimeNow.getHour())
                            .withMinute(dateTimeNow.getMinute()).withSecond(dateTimeNow.getSecond());
                    LocalDateTime endOfWeek = dateTimeNow.with(DayOfWeek.SUNDAY).plusDays(1).withHour(23).withMinute(59).withSecond(59);

                    // Filter tasks due within the current week
                    return taskDueDateTime.isAfter(startDate) && taskDueDateTime.isBefore(endOfWeek);
                })
                .collect(Collectors.toList());

        // If the iCalendar is not null, stop the TimeService, sort the events and tasks, then display them
        if (ical != null) {
            TimeService.stop();

            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            displayEvents(sortedEvents, sortedTasks, "Week's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }


    // Method to fetch events for the current month
    public void findMonthEvents(String filePath) throws IOException {
        // Fetch events and tasks for the current month and display them

        // Parse the iCal file to fetch events and tasks
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // Get current date and time
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        // Filter events occurring within the current month
        events = Optional.ofNullable(ical)
                .map(ICalendar::getEvents)
                .stream()
                .flatMap(Collection::stream)
                .filter(event -> {
                    // Get start and end date/time of the event
                    LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());
                    LocalDateTime eventEndDateTime = event.getDateEnd() != null ? convertDateEndToLocalDateTime(event.getDateEnd()) : null;

                    // Define start and end date/time of the current month
                    LocalDateTime startDate = dateTimeNow.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    LocalDateTime endOfMonth = dateTimeNow.withDayOfMonth(dateTimeNow.getMonth().length(dateTimeNow.toLocalDate().isLeapYear())).withHour(23).withMinute(59).withSecond(59);

                    // Check if the event falls within the current month
                    return (eventEndDateTime != null && ((eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfMonth)) ||
                            (eventEndDateTime.isAfter(startDate) && eventEndDateTime.isBefore(endOfMonth)))) ||
                            (eventEndDateTime == null && eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfMonth));
                })
                .collect(Collectors.toList());

        // Filter tasks occurring within the current month
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> {
                    // Get due date/time of the task
                    LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                    // Define start and end date/time of the current month
                    LocalDateTime startDate = dateTimeNow.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    LocalDateTime endOfMonth = dateTimeNow.withDayOfMonth(dateTimeNow.getMonth().length(dateTimeNow.toLocalDate().isLeapYear())).withHour(23).withMinute(59).withSecond(59);

                    // Check if the task's due date falls within the current month
                    return taskDueDateTime.isAfter(startDate) && taskDueDateTime.isBefore(endOfMonth);
                })
                .collect(Collectors.toList());

        // Stop the time service if the iCal file is not null
        if (ical != null) {
            TimeService.stop();

            // Sort the events and tasks by date
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);

            // Display the sorted events and tasks for the month
            displayEvents(sortedEvents, sortedTasks, "Month's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }


    // Method to fetch events from the start of the day till now
    public void findPastDay(String filePath) throws IOException {
        // Fetch events and tasks from the start of the day till now and display them
        ICalendar ical = Biweekly.parse(readFile(filePath)).first(); // Parse the iCal file

        TimeTeller timeTeller = TimeService.getTeller(); // Initialize TimeTeller
        LocalDateTime dateTimeNow = timeTeller.now(); // Get the current date and time

        // Filter events within the past day
        events = Optional.ofNullable(ical)
                .map(ICalendar::getEvents)
                .stream()
                .flatMap(Collection::stream)
                .filter(event -> {
                    LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());
                    LocalDateTime eventEndDateTime = event.getDateEnd() != null ? convertDateEndToLocalDateTime(event.getDateEnd()) : null;

                    LocalDateTime startOfDay = dateTimeNow.withHour(0).withMinute(0).withSecond(0).withNano(0);

                    return (eventEndDateTime != null && ((eventStartDateTime.isAfter(startOfDay) && eventStartDateTime.isBefore(dateTimeNow)) ||
                            (eventEndDateTime.isAfter(startOfDay) && eventEndDateTime.isBefore(dateTimeNow)))) ||
                            (eventEndDateTime == null && eventStartDateTime.isAfter(startOfDay) && eventStartDateTime.isBefore(dateTimeNow));
                })
                .collect(Collectors.toList());

        // Filter tasks within the past day
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> {
                    LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                    LocalDateTime startOfDay = dateTimeNow.withHour(0).withMinute(0).withSecond(0).withNano(0);

                    return taskDueDateTime.isAfter(startOfDay) && taskDueDateTime.isBefore(dateTimeNow);
                })
                .collect(Collectors.toList());

        // Handle null or empty iCal file
        if (ical != null) {
            TimeService.stop(); // Stop the TimeService

            // Sort events and tasks by date
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);

            // Display events and tasks
            displayEvents(sortedEvents, sortedTasks, "Past day's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // Method to fetch events from the start of the week till now
    public void findPastWeek(String filePath) throws IOException {
        // Fetch events and tasks from the start of the week till now and display them
        ICalendar ical = Biweekly.parse(readFile(filePath)).first(); // Parse the iCal file

        TimeTeller timeTeller = TimeService.getTeller(); // Initialize TimeTeller
        LocalDateTime dateTimeNow = timeTeller.now(); // Get the current date and time

        // Filter events within the past week
        events = Optional.ofNullable(ical)
                .map(ICalendar::getEvents)
                .stream()
                .flatMap(Collection::stream)
                .filter(event -> {
                    LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());
                    LocalDateTime eventEndDateTime = event.getDateEnd() != null ? convertDateEndToLocalDateTime(event.getDateEnd()) : null;

                    LocalDateTime startOfWeek = dateTimeNow.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);

                    return (eventEndDateTime != null && ((eventStartDateTime.isAfter(startOfWeek) && eventStartDateTime.isBefore(dateTimeNow)) ||
                            (eventEndDateTime.isAfter(startOfWeek) && eventEndDateTime.isBefore(dateTimeNow)))) ||
                            (eventEndDateTime == null && eventStartDateTime.isAfter(startOfWeek) && eventStartDateTime.isBefore(dateTimeNow));
                })
                .collect(Collectors.toList());

        // Filter tasks within the past week
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> {
                    LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                    LocalDateTime startOfWeek = dateTimeNow.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);

                    return taskDueDateTime.isAfter(startOfWeek) && taskDueDateTime.isBefore(dateTimeNow);
                })
                .collect(Collectors.toList());

        // Handle null or empty iCal file
        if (ical != null) {
            TimeService.stop(); // Stop the TimeService

            // Sort events and tasks by date
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);

            // Display events and tasks
            displayEvents(sortedEvents, sortedTasks, "Past week's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }


    // Method to fetch events from the start of the month till now
    public void findPastMonth(String filePath) throws IOException {
        // Fetch events and tasks from the start of the month till now and display them
        ICalendar ical = Biweekly.parse(readFile(filePath)).first(); // Parsing the iCalendar file

        TimeTeller timeTeller = TimeService.getTeller(); // Fetching TimeTeller instance
        LocalDateTime dateTimeNow = timeTeller.now(); // Fetching current date and time

        // Filtering events within the given time frame (start of the month till now)
        events = Optional.ofNullable(ical)
                .map(ICalendar::getEvents)
                .stream()
                .flatMap(Collection::stream)
                .filter(event -> {
                    LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());
                    LocalDateTime eventEndDateTime = event.getDateEnd() != null ? convertDateEndToLocalDateTime(event.getDateEnd()) : null;

                    LocalDateTime startOfMonth = dateTimeNow.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

                    return (eventEndDateTime != null && ((eventStartDateTime.isAfter(startOfMonth) && eventStartDateTime.isBefore(dateTimeNow)) ||
                            (eventEndDateTime.isAfter(startOfMonth) && eventEndDateTime.isBefore(dateTimeNow)))) ||
                            (eventEndDateTime == null && eventStartDateTime.isAfter(startOfMonth) && eventStartDateTime.isBefore(dateTimeNow));
                })
                .collect(Collectors.toList()); // Collecting filtered events

        // Filtering tasks within the given time frame (start of the month till now)
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> {
                    LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                    LocalDateTime startOfMonth = dateTimeNow.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

                    return taskDueDateTime.isAfter(startOfMonth) && taskDueDateTime.isBefore(dateTimeNow);
                })
                .collect(Collectors.toList()); // Collecting filtered tasks

        if (ical != null) {
            TimeService.stop(); // Stopping TimeService if iCalendar is not null

            // Sorting and displaying the events and tasks
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            displayEvents(sortedEvents, sortedTasks, "Past month's events");
        } else {
            System.out.println("Invalid or empty iCal file."); // Displaying an error message for invalid or empty iCalendar file
        }
    }

    // Method to display todo tasks based on their completion status and deadline
    public void findToDoEvents(String filePath) throws IOException {
        // Fetch todo tasks based on their completion status and deadline and display them
        ICalendar ical = Biweekly.parse(readFile(filePath)).first(); // Parsing the iCalendar file

        TimeTeller timeTeller = TimeService.getTeller(); // Fetching TimeTeller instance
        LocalDateTime dateTimeNow = timeTeller.now(); // Fetching current date and time

        // Filtering tasks based on their completion status and deadline
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> task.getStatus() != null &&
                        task.getStatus().getValue().equals("IN-PROGRESS") &&
                        task.getDateDue() != null &&
                        dateTimeNow.isBefore(convertDateDueToLocalDateTime(task.getDateDue())))
                .collect(Collectors.toList()); // Collecting filtered tasks

        if (ical != null) {
            TimeService.stop(); // Stopping TimeService if iCalendar is not null

            // Sorting and displaying the tasks
            sortedTasks = EventLists.sortByDate(tasks);
            displayEvents(null, sortedTasks, "Todo tasks");
        } else {
            System.out.println("Invalid or empty iCal file."); // Displaying an error message for invalid or empty iCalendar file
        }
    }


    // Method to display due events based on their completion status and deadline expiration
    public void findDueEvents(String filePath) throws IOException {
        // Read iCal file and fetch the first entry from the file
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // Get the system time
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        // Filter and retrieve expired tasks
        tasks = Optional.ofNullable(ical)
                .map(ICalendar::getTodos)
                .stream()
                .flatMap(Collection::stream)
                .filter(task -> task.getStatus() != null &&
                        task.getStatus().getValue().equals("IN-PROGRESS") &&
                        task.getDateDue() != null &&
                        dateTimeNow.isAfter(convertDateDueToLocalDateTime(task.getDateDue())))
                .collect(Collectors.toList());

        // If ical is not null, stop the time service, sort the tasks, and display the results
        if (ical != null) {
            TimeService.stop();
            sortedTasks = EventLists.sortByDate(tasks);
            displayEvents(null, sortedTasks, "Due events");
        } else {
            // If the file is invalid or empty, print an error message
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // Method to create a new event or task and save it in the iCal file
    public void createEvent(String filePath) throws IOException {
        // Allow the user to create a new event or task and save it in the iCal file

        // Initialize a scanner for user input
        Scanner scanner = new Scanner(System.in);
        File file = new File(filePath);

        // Check if the file exists and create a new one if not
        if (!file.exists()) {
            Files.createFile(file.toPath());
            System.out.println("\nCreated the file: " + file);
        } else {
            System.out.println("\nOpened the file: " + filePath);
        }

        // Parse iCal file or create a new one if it's empty or invalid
        ICalendar ical = Biweekly.parse(file).first();
        if (ical == null) {
            ical = new ICalendar();
        }

        boolean notFinished = true;
        while (notFinished) {
            // Prompt the user to create a task or an event
            System.out.print("\nDo you want to create a task or another event? (task/other): ");
            String eventAnswer = scanner.nextLine().trim().toLowerCase();

            // Loop to validate user input for creating a task or an event
            while (!eventAnswer.equals("task") && !eventAnswer.equals("other")) {
                System.out.print("Invalid input. Please enter 'task' or 'other': ");
                eventAnswer = scanner.nextLine().trim().toLowerCase();
            }

            // Create a new event or task based on user input
            if (eventAnswer.equalsIgnoreCase("other")) {
                createNewEvent(ical, scanner);
            } else {
                createNewTask(ical, scanner);
            }

            // Ask the user if they want to add more events or tasks
            System.out.print("\nDo you want to add more events? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            // Loop to validate user input for adding more events
            while (!answer.equals("y") && !answer.equals("n")) {
                System.out.print("Invalid input. Please enter 'y' or 'n': ");
                answer = scanner.nextLine().trim().toLowerCase();
            }

            // Stop the loop if the user doesn't want to add more events
            if (answer.equals("n")) {
                notFinished = false;
            }
        }

        // Write the events to the iCal file and display a message
        Biweekly.write(ical).go(file);
        System.out.println("\nEvents saved to file.");
    }

    // Method for creating a new event
    private void createNewEvent(ICalendar ical, Scanner scanner) {
        VEvent event = new VEvent();
        System.out.println("\nCreating a new Event ... ");

        // Input for event title
        System.out.print("\n-Enter a title for the event: ");
        String title = scanner.nextLine();

        // Validation for non-empty title
        while (title == null || title.trim().isEmpty()) {
            System.out.print("-Please enter a non-empty title for the event: ");
            title = scanner.nextLine();
        }

        event.setSummary(title);

        // Input for event description
        System.out.print("-Enter a description for the event: ");
        event.setDescription(scanner.nextLine());

        // Input for event start date and time
        System.out.print("-Enter the start date and time of the event (yyyy-MM-dd HH:mm): ");
        boolean validStartDate = false;
        LocalDateTime startDateTime = null;

        // Validation loop for start date input
        while (!validStartDate) {
            try {
                String startDateTimeStr = scanner.nextLine();
                startDateTime = LocalDateTime.parse(startDateTimeStr, java.time.format.DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm"));
                validStartDate = true;
            } catch (DateTimeParseException e) {
                System.err.print("Error parsing date and time. Please enter the correct format " +
                        "(yyyy-MM-dd HH:mm --> year-month-day hours:minutes): ");
            }
        }

        event.setDateStart(new DateStart(Date.from(startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant())));

        // Input for event end date and time (optional)
        System.out.print("-Enter the end date and time of the event (optionally, press Enter to skip): ");
        String endDateTimeStr = scanner.nextLine().trim();

        if (!endDateTimeStr.isEmpty()) {
            boolean validEndDate = false;
            LocalDateTime endDateTime = null;

            // Validation loop for end date input
            while (!validEndDate) {
                try {
                    endDateTime = LocalDateTime.parse(endDateTimeStr, java.time.format.DateTimeFormatter
                            .ofPattern("yyyy-MM-dd HH:mm"));
                    validEndDate = true;
                } catch (DateTimeParseException e) {
                    System.err.print("Error parsing date and time. Please enter the correct format " +
                            "(yyyy-MM-dd HH:mm --> year-month-day hours:minutes): ");
                    endDateTimeStr = scanner.nextLine().trim();
                }
            }

            event.setDateEnd(new DateEnd(Date.from(endDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant())));
        }

        // Adding the new event to the iCalendar
        ical.addEvent(event);
        System.out.println("\nSuccessfully created a new event!");
    }

    // Method for creating a new task
    private void createNewTask(ICalendar ical, Scanner scanner) {
        VTodo task = new VTodo();
        System.out.println("\nCreating a new Task ... ");

        // Input for task title
        System.out.print("\n-Enter a title for the task: ");
        String title = scanner.nextLine();

        // Validation for non-empty title
        while (title == null || title.trim().isEmpty()) {
            System.out.print("-Please enter a non-empty title for the task: ");
            title = scanner.nextLine();
        }

        task.setSummary(title);

        // Input for task description
        System.out.print("-Enter a description for the task: ");
        task.setDescription(scanner.nextLine());

        // Input for task deadline
        System.out.print("-Enter the deadline of the task (yyyy-MM-dd HH:mm): ");
        boolean validDueDate = false;
        LocalDateTime dueDateTime = null;

        // Validation loop for task deadline input
        while (!validDueDate) {
            try {
                String dueDateTimeStr = scanner.nextLine();
                dueDateTime = LocalDateTime.parse(dueDateTimeStr, java.time.format.DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm"));
                validDueDate = true;
            } catch (DateTimeParseException e) {
                System.err.print("Error parsing date and time. Please enter the correct format " +
                        "(yyyy-MM-dd HH:mm --> year-month-day hours:minutes): ");
            }
        }

        task.setDateDue(new DateDue(Date.from(dueDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant())));
        task.setStatus(new Status(Status.IN_PROGRESS));

        // Adding the new task to the iCalendar
        ical.addTodo(task);
        System.out.println("\nSuccessfully created a new task!");
    }


}
