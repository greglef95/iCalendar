# calendarApp

# Commands for Calendar (calendar)

This console-based calendar application has two main functionalities outlined below.

## 1. Displaying Event Data

The first function takes 2 arguments:

- `day`: Prints events until the end of the day.
- `week`: Prints events until the end of the week.
- `month`: Prints events until the end of the month.
- `pastday`: Prints events from the beginning of the day until now.
- `pastweek`: Prints events from the beginning of the week until now.
- `pastmonth`: Prints events from the beginning of the month until now.
- `todo`: Prints tasks that are unfinished and have not passed the deadline.
- `due`: Prints tasks that are unfinished and have passed the deadline.

## 2. Updating Calendar with New Events

The second function takes 1 argument, the ical file name. For example:

java -jar calendar.jar day mycal.ics

The above command will read the mycal.ics file and print the events until the end of the day.

java -jar calendar.jar due mycal.ics

The above command will print the tasks that have not been completed until now.

If called without further arguments, the application will start the second function, allowing the user to enter new events and update the ical file.

For more details, refer to the usage guide of the application.

actors