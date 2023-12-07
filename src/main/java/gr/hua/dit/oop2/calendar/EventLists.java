package gr.hua.dit.oop2.calendar;

import biweekly.component.VEvent;

import java.time.LocalDateTime;
import java.util.List;

import static gr.hua.dit.oop2.calendar.Calendar.convertDateStartToLocalDateTime;

public class EventLists {

    public EventLists() {}

    // method that sorts the events based on which has the closer StartDate to the current
    public static List<VEvent> sortByStartDate(List<VEvent> events) {
        events.sort((event1, event2) -> {

            // use the method from the calendar class to convert the date
            LocalDateTime startDateTime1 = convertDateStartToLocalDateTime(event1.getDateStart());
            LocalDateTime startDateTime2 = convertDateStartToLocalDateTime(event2.getDateStart());

            // compare events based on start dates
            return startDateTime1.compareTo(startDateTime2);
        });

        return events;
    }

}
