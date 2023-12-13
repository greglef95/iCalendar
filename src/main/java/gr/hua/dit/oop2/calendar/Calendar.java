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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


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
            DateStart dtStart = event.getDateStart();
            DateEnd dtEnd = event.getDateEnd();
            Description description = event.getDescription();

            System.out.println("--------------------");
            System.out.println("Title: " + summary.getValue());
            System.out.println("Description: " + description.getValue());
            System.out.println("Start Date and Time: " + dtStart.getValue());
            if (dtEnd != null) {
                System.out.println("End Date and Time: " + dtEnd.getValue());
            }
            System.out.println("--------------------");
        }

        for (VTodo task : tasks) {
            Summary summary = task.getSummary();
            DateDue dtDue = task.getDateDue();
            Description description = task.getDescription();
            Status status = task.getStatus();

            System.out.println("--------------------");
            System.out.println("Title: " + summary.getValue());
            System.out.println("Description: " + description.getValue());
            System.out.println("Deadline of the task: " + dtDue.getValue());
            System.out.println("Status of the task: " + status.getValue());
            System.out.println("--------------------");

        }
    }

    // fetch the events from the current day
    public void findDayEvents(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {

            for (VEvent event : ical.getEvents()) {
                // get the start date and time of the event
                LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());

                // check if the end date of the event is null which means that it's a simple event and not an appointment
                if (event.getDateEnd() != null) {
                    // get the end date and time of the event
                    LocalDateTime eventEndDateTime = convertDateEndToLocalDateTime(event.getDateEnd());

                    // check if the event start or end date is equal with the current and add the event in the events list
                    if (eventStartDateTime.toLocalDate().isEqual(dateTimeNow.toLocalDate()) ||
                            eventEndDateTime.toLocalDate().isEqual(dateTimeNow.toLocalDate())) {
                        events.add(event);
                    }
                } else {
                    // check only if the start date (the end date might be null because it's optional)
                    if (eventStartDateTime.toLocalDate().isEqual(dateTimeNow.toLocalDate())) {
                        events.add(event);
                    }
                }

            }
            for (VTodo task : ical.getTodos()) {
                // get the Due date of the task
                LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());
                // check if the due date is equal to the current date and add the task in the tasks list
                if (taskDueDateTime.toLocalDate().isEqual(dateTimeNow.toLocalDate())) {
                    tasks.add(task);
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Today's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // view the events from the current day till the end of the week
    public void findWeekEvents(String filePath) throws IOException {

        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {

            // set the start date and time as the current date and time
            LocalDateTime startDate = dateTimeNow.with(dateTimeNow.getDayOfWeek()).withHour(dateTimeNow.getHour())
                    .withMinute(dateTimeNow.getMinute()).withSecond(dateTimeNow.getSecond());

            // set the end date and Sunday
            LocalDateTime endOfWeek = dateTimeNow.with(DayOfWeek.SUNDAY).plusDays(1).withHour(23).withMinute(59).withSecond(59);

            for (VEvent event : ical.getEvents()) {
                // get the start date and time of the event
                LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());

                // check if the end date of the event is null which means that it's a simple event and not an appointment
                if (event.getDateEnd() != null) {
                    // get the end date and time of the event
                    LocalDateTime eventEndDateTime = convertDateEndToLocalDateTime(event.getDateEnd());

                    /* check if the event start or end date is after our start date and before Sunday and
                     add the event in the events list */
                    if ((eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfWeek)) ||
                            (eventEndDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfWeek))) {
                        events.add(event);
                    }
                } else {
                    // check only if the start date (the end date might be null because it's optional)
                    if ((eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfWeek))) {
                        events.add(event);
                    }
                }
            }
            for (VTodo task : ical.getTodos()) {
                // get the Due date of the task
                LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());
                /* check if the task due date is after our start date and before Sunday and
                     add the task in the tasks list */
                if (taskDueDateTime.isAfter(startDate) && taskDueDateTime.isBefore(endOfWeek)) {
                    tasks.add(task);
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Weeks's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }

    }

    // view the events from the current month
    public void findMonthEvents(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {
            // set the start date and time as the current date and time
            LocalDateTime startDate = dateTimeNow.withDayOfMonth(dateTimeNow.getDayOfMonth()).withHour(dateTimeNow.getHour())
                    .withMinute(dateTimeNow.getMinute()).withSecond(dateTimeNow.getSecond());
            // set the end date as the last day of the month
            LocalDateTime endOfMonth = dateTimeNow.withDayOfMonth(dateTimeNow.getMonth().length(dateTimeNow.toLocalDate()
                    .isLeapYear())).withHour(23).withMinute(59).withSecond(59);

            for (VEvent event : ical.getEvents()) {
                // get the start date and time of the event
                LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());

                // check if the end date of the event is null which means that it's a simple event and not an appointment
                if (event.getDateEnd() != null) {
                    // get the end date and time of the event
                    LocalDateTime eventEndDateTime = convertDateEndToLocalDateTime(event.getDateEnd());

                    /* check if the event start or end date is after our start date and before the last day of the month
                     and add the event in the events list */
                    if ((eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfMonth)) ||
                            (eventEndDateTime.isAfter(startDate) && eventEndDateTime.isBefore(endOfMonth))) {
                        events.add(event);
                    }
                } else {
                    // check only if the start date (the end date might be null because it's optional)
                    if ((eventStartDateTime.isAfter(startDate) && eventStartDateTime.isBefore(endOfMonth))) {
                        events.add(event);
                    }
                }
            }
            for (VTodo task : ical.getTodos()) {
                // get the Due date of the task
                LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());
                /* check if the task due date is after our start date and before the last day of the month and
                    add the task in the tasks list */
                if (taskDueDateTime.isAfter(startDate) && taskDueDateTime.isBefore(endOfMonth)) {
                    tasks.add(task);
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Month's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // display the events from the start of the day till now
    public void findPastDay(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {

            LocalDateTime startOfDay = dateTimeNow.withDayOfMonth(dateTimeNow.getDayOfMonth()).withHour(0).withMinute(0)
                    .withSecond(0).withNano(0);

            for (VEvent event : ical.getEvents()) {
                // get the start date and time of the event
                LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());

                // check if the end date of the event is null which means that it's a simple event and not an appointment
                if (event.getDateEnd() != null) {
                    // get the end date and time of the event
                    LocalDateTime eventEndDateTime = convertDateEndToLocalDateTime(event.getDateEnd());

                    /* check if the event start or end date is after the start of the day and before the current time
                    and add the event in the events list */
                    if ((eventStartDateTime.isAfter(startOfDay) && eventStartDateTime.isBefore(dateTimeNow)) ||
                            (eventEndDateTime.isAfter(startOfDay) && eventEndDateTime.isBefore(dateTimeNow))) {
                        events.add(event);
                    }
                } else {
                    // check only if the start date (the end date might be null because it's optional)
                    if ((eventStartDateTime.isAfter(startOfDay) && eventStartDateTime.isBefore(dateTimeNow))) {
                        events.add(event);
                    }
                }
            }
            for (VTodo task : ical.getTodos()) {
                // get the Due date of the task
                LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                /* check if the task due date is after the start of the day and before the current time and
                    add the task in the tasks list */
                if (taskDueDateTime.isAfter(startOfDay) && taskDueDateTime.isBefore(dateTimeNow)) {
                    tasks.add(task);
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Pastday's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // display the events from the start of the week till now
    public void findPastWeek(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {
            // set the start date and time as Monday
            LocalDateTime startOfWeek = dateTimeNow.with(DayOfWeek.MONDAY).withHour(0).withMinute(0)
                    .withSecond(0).withNano(0);

            for (VEvent event : ical.getEvents()) {
                // get the start date and time of the event
                LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());

                // check if the end date of the event is null which means that it's a simple event and not an appointment
                if (event.getDateEnd() != null) {
                    // get the end date and time of the event
                    LocalDateTime eventEndDateTime = convertDateEndToLocalDateTime(event.getDateEnd());

                    /* check if the event start or end date is after Monday and before the current time
                     and add the event in the events list */
                    if ((eventStartDateTime.isAfter(startOfWeek) && eventStartDateTime.isBefore(dateTimeNow) ||
                            (eventEndDateTime.isAfter(startOfWeek) && eventEndDateTime.isBefore(dateTimeNow)))) {
                        events.add(event);
                    }
                } else {
                    // check only if the start date (the end date might be null because it's optional)
                    if (eventStartDateTime.isAfter(startOfWeek) && eventStartDateTime.isBefore(dateTimeNow)) {
                        events.add(event);
                    }
                }
            }
            for (VTodo task : ical.getTodos()) {
                // get the Due date of the task
                LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                /* check if the task due date is after Monday and before the current time and
                    add the task in the tasks list */
                if (taskDueDateTime.isAfter(startOfWeek) && taskDueDateTime.isBefore(dateTimeNow)) {
                    tasks.add(task);
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Pastweek's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // display the events from the start of the month till now
    public void findPastMonth(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {
            // set the start date and time as the first day of the month
            LocalDateTime startOfMonth = dateTimeNow.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                    .withNano(0);

            for (VEvent event : ical.getEvents()) {
                // get the start date and time of the event
                LocalDateTime eventStartDateTime = convertDateStartToLocalDateTime(event.getDateStart());

                // check if the end date of the event is null which means that it's a simple event and not an appointment
                if (event.getDateEnd() != null) {
                    // get the end date and time of the event
                    LocalDateTime eventEndDateTime = convertDateEndToLocalDateTime(event.getDateEnd());

                    /* check if the event start or end date is after the first day of the month and before
                     the current time and add the event in the events list */
                    if ((eventStartDateTime.isAfter(startOfMonth) && eventStartDateTime.isBefore(dateTimeNow) ||
                            (eventEndDateTime.isAfter(startOfMonth) && eventEndDateTime.isBefore(dateTimeNow)))) {
                        events.add(event);
                    }
                } else {
                    // check only if the start date (the end date might be null because it's optional)
                    if (eventStartDateTime.isAfter(startOfMonth) && eventStartDateTime.isBefore(dateTimeNow)) {
                        events.add(event);
                    }
                }

            }
            for (VTodo task : ical.getTodos()) {
                // get the Due date of the task
                LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());

                /* check if the task due date is after the first day of the month and before
                    the current time and add the task in the tasks list */
                if (taskDueDateTime.isAfter(startOfMonth) && taskDueDateTime.isBefore(dateTimeNow)) {
                    tasks.add(task);
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Pastmonth's events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }
    }

    // display the events that are task which are not completed and the deadline has not expired
    public void findToDoEvents(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {
            // for each task check if the status is not null to understand that it is a task and that it's value is "not completed"
            for (VTodo task : ical.getTodos()) {

                if (task.getStatus() != null && (task.getStatus().getValue().equals("IN-PROGRESS"))) {

                    if (task.getDateDue() != null) {
                        // get the Due date of the task
                        LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());
                        // check if the task's dateTime is after the current
                        if (dateTimeNow.isBefore(taskDueDateTime)) {
                            tasks.add(task);
                        }
                    }
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Todo tasks");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }

    }

    // display the events that are "tasks" which are not completed and the deadline has expired
    public void findDueEvents(String filePath) throws IOException {
        ICalendar ical = Biweekly.parse(readFile(filePath)).first();

        // initialize the TimeTeller using the TimeService
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime dateTimeNow = timeTeller.now();

        events = new ArrayList<>();
        tasks = new ArrayList<>();

        if (ical != null) {
            // for each task check if the status is not null to understand that it is a task and that it's value is "not completed"
            for (VTodo task : ical.getTodos()) {

                if (task.getStatus() != null && (task.getStatus().getValue().equals("IN-PROGRESS"))) {

                    if (task.getDateDue() != null) {
                        // get the Due date of the task
                        LocalDateTime taskDueDateTime = convertDateDueToLocalDateTime(task.getDateDue());
                        // check if the task's dateTime is after the current
                        if (dateTimeNow.isAfter(taskDueDateTime)) {
                            tasks.add(task);
                        }
                    }
                }
            }
            // stop the time service from the timeteller
            TimeService.stop();

            // call the method that sorts the eventlist and tasklist
            sortedEvents = EventLists.sortByDate(events);
            sortedTasks = EventLists.sortByDate(tasks);
            // call the method that display the sorted lists
            displayEvents(sortedEvents, sortedTasks, "Due events");
        } else {
            System.out.println("Invalid or empty iCal file.");
        }

    }

    // method for create a new event and the file if it does not exist
    public void createEvent(String filePath) throws IOException {
        boolean notFinished = true;

        Scanner scanner = new Scanner(System.in);

        // create new file object
        File file = new File(filePath);

        // check if the file exists and if not create it
        if (!file.exists()) {
            Files.createFile(file.toPath());
            System.out.println("\nCreated the file: " + file);
        } else {
            System.out.println("\nOpened the file: " + filePath);
        }

        // parse the ical file and if the calendar is null create one
        ICalendar ical = Biweekly.parse(file).first();
        if (ical == null) {
            ical = new ICalendar();
        }

        // procedure for the user to create new events from the terminal
        while (notFinished) {

            VEvent event = new VEvent();
            VTodo task = new VTodo();

            System.out.print("\nDo you want to create a task or another event? (task/other): ");
            String eventAnswer = scanner.nextLine().trim().toLowerCase();

            while (!eventAnswer.equals("task") && !eventAnswer.equals("other")) {
                System.out.print("Invalid input. Please enter 'task' or 'other': ");
                eventAnswer = scanner.nextLine().trim().toLowerCase();
            }

            if (eventAnswer.equalsIgnoreCase("other")) {

                System.out.println("\nCreating a new Event ... ");

                // title of the event
                System.out.print("\n-Enter a title for the event: ");
                String title = scanner.nextLine();

                // keep prompting until a non-null and non-empty title is provided
                while (title == null || title.trim().isEmpty()) {
                    System.out.print("-Please enter a non-empty title for the event: ");
                    title = scanner.nextLine();
                }

                event.setSummary(title);

                // description of the event
                System.out.print("-Enter a description for the event: ");
                event.setDescription(scanner.nextLine());

                // start date of the event
                System.out.print("-Enter the start date and time of the event (yyyy-MM-dd HH:mm): ");
                boolean validStartDate = false;
                LocalDateTime startDateTime = null;

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

                // end date for the events that need it (tasks, appointments)
                System.out.print("-Enter the end date and time of the event (optionally, press Enter to skip): ");
                String endDateTimeStr = scanner.nextLine().trim();

                if (!endDateTimeStr.isEmpty()) {
                    boolean validEndDate = false;
                    LocalDateTime endDateTime = null;

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


                // add the new event to the iCalendar
                ical.addEvent(event);

                // write the iCalendar back to the file
                Biweekly.write(ical).go(file);

                System.out.println("\nSuccessfully created a new event!");

            } else {

                System.out.println("\nCreating a new Task ... ");

                // title of the task
                System.out.print("\n-Enter a title for the task: ");
                String title = scanner.nextLine();

                while (title == null || title.trim().isEmpty()) {
                    System.out.print("-Please enter a non-empty title for the task: ");
                    title = scanner.nextLine();
                }

                task.setSummary(title);

                // description of the task
                System.out.print("-Enter a description for the task: ");
                task.setDescription(scanner.nextLine());

                // deadline of the task
                System.out.print("-Enter the deadline of the task (yyyy-MM-dd HH:mm): ");
                boolean validDueDate = false;
                LocalDateTime dueDateTime = null;

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

                // add the new task to the iCalendar
                ical.addTodo(task);

                // write the iCalendar back to the file
                Biweekly.write(ical).go(file);

                System.out.println("\nSuccessfully created a new task!");

            }

            System.out.print("\nDo you want to add more events? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();

            while (!answer.equals("y") && !answer.equals("n")) {
                System.out.print("Invalid input. Please enter 'y' or 'n': ");
                answer = scanner.nextLine().trim().toLowerCase();
            }

            if (answer.equals("n")) {
                notFinished = false;
            }
        }
    }
}
