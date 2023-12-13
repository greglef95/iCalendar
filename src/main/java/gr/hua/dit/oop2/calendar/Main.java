package gr.hua.dit.oop2.calendar;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Calendar calendar = new Calendar();
        String filePath = "";

        // Check the arguments's number provided
        if (args.length > 2) {
            System.out.println("Please provide at most two arguments.");
            System.exit(1);
        } else if (args.length < 1) {
            System.out.println("Please provide at least one argument.");
            System.exit(1);
        } else {
            filePath = args[args.length - 1];
        }

        try {
            // Check if the file ends with ".ics"
            if (!filePath.endsWith(".ics")) {
                throw new IllegalArgumentException("Please provide an iCal file (with extension .ics).");
            }

            if (args.length == 1) {
                calendar.createEvent(filePath);
            } else {
                String eventType = args[0];
                switch (eventType) {
                    case "day":
                        calendar.findDayEvents(filePath);
                        break;
                    case "week":
                        calendar.findWeekEvents(filePath);
                        break;
                    case "month":
                        calendar.findMonthEvents(filePath);
                        break;
                    case "pastday":
                        calendar.findPastDay(filePath);
                        break;
                    case "pastweek":
                        calendar.findPastWeek(filePath);
                        break;
                    case "pastmonth":
                        calendar.findPastMonth(filePath);
                        break;
                    case "todo":
                        calendar.findToDoEvents(filePath);
                        break;
                    case "due":
                        calendar.findDueEvents(filePath);
                        break;
                    default:
                        System.out.println("Invalid input. Please use one of the following as the first argument:");
                        System.out.println("  - day");
                        System.out.println("  - week");
                        System.out.println("  - month");
                        System.out.println("  - pastday");
                        System.out.println("  - pastweek");
                        System.out.println("  - pastmonth");
                        System.out.println("  - todo");
                        System.out.println("  - due");
                        break;
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
