package gr.hua.dit.oop2.calendar;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Calendar calendar = new Calendar();
        String filePath = "";

        // check length of the given arguments
        if (args.length > 2) {
            System.out.println("Please do not provide more than two arguments.");
            System.exit(1);
        } else if (args.length == 2) {
            filePath = args[1];
        }else {
            filePath = args[0];
        }


        try {
            // check if the file ends with ".ics"
            if (!filePath.endsWith(".ics")) {
                throw new IllegalArgumentException("Please provide an iCal file (with extension .ics).");
            }
            if(args.length == 2) {
                switch (args[0]) {
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
                        System.out.println("Invalid input. Please try one of the following as the first argument if you want to view events:");
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
            } else {
                calendar.createEvent(filePath);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
