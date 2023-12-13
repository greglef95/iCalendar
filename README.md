# CalendarApp

This Java project, allows users to manage calendars, events, and tasks using `.ics` files.
### Project uses:
1. UTF-8 encoding <li>Maven management.

## Getting Started

### Installation

1. Run Main Class to create .target folder in your project
2. Ensure you have Maven installed. Use the following command to create the necessary JAR file:

```bash
mvn clean install
```
# FAQ
## How-to RUN the CalendarApp?
### View Calendar Events
To view events in the calendar based on different timeframes (day, week, month, pastday, pastweek, pastmonth, due, todo), use the following command:

```bash
java -jar target/CalendarApp-1.0-SNAPSHOT.jar <timeframe> <your-ics-file>
```
Replace <timeframe> with one of the options mentioned above and <your-ics-file> with the .ics file name. If the file is not in the project folder, provide the absolute path.

## Create or Update Calendar
To create a new calendar or update an existing one with events/tasks, use:

```bash
java -jar target/CalendarApp-1.0-SNAPSHOT.jar <your-ics-file>
```
Replace <your-ics-file> with the .ics file name. If the file is not in the project folder, provide the absolute path. If the file doesn't exist, the app will create it and add new events/tasks; otherwise, it will update the existing file.

## Our .ics file
We have our oop2.ics file in main folder

```bash
java -jar target/CalendarApp-1.0-SNAPSHOT.jar month oop2.ics
```

#### Developers
1. 21451 Greg Lefkelis<li>
22068 Ioannis Mavrodimos <li>
22069 Christos Midanis