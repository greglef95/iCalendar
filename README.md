CalendarApp
===========

The CalendarApp is a Java-based project designed to manage calendars, events, and tasks using .ics files. 

It leverages UTF-8 encoding and Maven for project management.

Getting Started
---------------

### 1. Installation

1.  Run the Main Class to create a `.target` folder in your project.

2.  Ensure you have Maven installed.

3.  Use the following command to create the necessary JAR file: `mvn clean install`


### 2. Run the CalendarApp?

#### View Calendar Events

To view events in the calendar based on different timeframes (day, week, month, pastday, pastweek, pastmonth, due, todo), execute the following command:

`
java -jar target/CalendarApp-1.0-SNAPSHOT.jar <timeframe> <your-ics-file>
`

#### Let's Run our example `oop2.ics` file.
`Just Copy the Code`
```bash
java -jar target/CalendarApp-1.0-SNAPSHOT.jar month oop2.ics
```
Replace `<timeframe>` with one of the options mentioned above and `<your-ics-file>` with the .ics file name. If the file is not in the project folder, provide the absolute path.

### 3. Create or Update Calendar

To create a new calendar or update an existing one with events/tasks, use:

```bash
java -jar target/CalendarApp-1.0-SNAPSHOT.jar <your-ics-file>
```

Replace `<your-ics-file>` with the .ics file name. If the file is not in the project folder, provide the absolute path. If the file doesn't exist, the app will create it and add new events/tasks; otherwise, it will update the existing file.

# -------------------------------------------------------



# Project Structure

1.  EventLists.java: Contains methods for sorting events based on their start or due dates.
2.  Main.java: Houses the main logic of the application, interacting with the Calendar class to perform operations based on user input through command-line arguments.

## Main Components

-   EventLists.java:

    -   Contains a method `sortByDate` that sorts a list of events based on their start or due dates.
    -   Uses generics to handle both VEvent and VTodo objects.
    -   Implements a private method `getDateTime` to extract the start/due date from events.
-   Main.java:

    -   Initializes a Calendar object.
    -   Validates command-line arguments and file paths.
    -   Parses the command-line arguments and directs the calendar operations accordingly.
    -   Catches exceptions related to argument validation and file I/O.

## Functionality

-   Calendar Operations:

    -   Creation: Handles event creation if only a file path is provided.
    -   Query Operations: Supports various queries like events for a day, week, month, past periods, to-do events, and due events based on the provided command-line arguments.
-   Error Handling:

    -   Validates the number of arguments provided.
    -   Checks if the file path ends with ".ics" for compatibility.
    -   Catches and displays specific error messages for argument and file-related issues.



License Information
-------------------

The software developed under the auspices of the Department of Informatics and Telecommunications (DIT) at the University of Athens (HUA).

### HUA Developers
```
21451 Grigorios Lefkelis
22068 Ioannis Mavrodimos
22069 Christos Midanis
```