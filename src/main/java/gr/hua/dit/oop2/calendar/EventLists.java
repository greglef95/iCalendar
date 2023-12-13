package gr.hua.dit.oop2.calendar;

import biweekly.component.VEvent;
import biweekly.component.VTodo;
import java.time.LocalDateTime;
import java.util.List;

import static gr.hua.dit.oop2.calendar.Calendar.convertDateStartToLocalDateTime;
import static gr.hua.dit.oop2.calendar.Calendar.convertDateDueToLocalDateTime;

public class EventLists {

    public EventLists() {
        // Constructor (if needed)
    }

    // Sorts the events based on the closer Start/Due date to the current
    public static <T> List<T> sortByDate(List<T> events) {
        events.sort((event1, event2) -> {
            LocalDateTime dateTime1 = getDateTime(event1);
            LocalDateTime dateTime2 = getDateTime(event2);

            // Compare events based on dates
            return dateTime1.compareTo(dateTime2);
        });

        return events;
    }

    // Extracts start/due date from either VEvent or VTodo
    private static <T> LocalDateTime getDateTime(T event) {
        if (event instanceof VEvent) {
            // Convert DateStart to LocalDateTime
            return convertDateStartToLocalDateTime(((VEvent) event).getDateStart());
        } else if (event instanceof VTodo) {
            // Convert DateDue to LocalDateTime
            return convertDateDueToLocalDateTime(((VTodo) event).getDateDue());
        } else {
            throw new IllegalArgumentException("Unsupported event type");
        }
    }
}
