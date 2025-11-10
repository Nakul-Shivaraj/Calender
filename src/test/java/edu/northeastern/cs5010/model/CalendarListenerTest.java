package edu.northeastern.cs5010.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the observer pattern implementation in CalendarModel.
 */
class CalendarListenerTest {

  private CalendarModel calendar;

  @BeforeEach
  void setUp() {
    calendar = new CalendarModel("Test Calendar", false);
  }

  /**
   * Simple test listener that records events.
   */
  private static class TestListener implements CalendarListener {
    final List<Event> added = new ArrayList<>();
    final List<Event> modified = new ArrayList<>();

    @Override
    public void onEventAdded(Event event) {
      added.add(event);
    }

    @Override
    public void onEventModified(Event event) {
      modified.add(event);
    }
  }

  @Test
  void listenerNotifiedOnAdd() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);

    Event event = new Event(
        "Meeting",
        LocalDate.of(2025, 11, 10),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 10),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    calendar.addEvent(event);

    assertEquals(1, listener.added.size());
    assertEquals(0, listener.modified.size());
  }

  @Test
  void listenerNotifiedOnModify() {
    TestListener listener = new TestListener();

    Event original = new Event(
        "Workshop",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(14, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(15, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    calendar.addEvent(original);
    calendar.addCalendarListener(listener);

    Event updated = new Event(
        "Workshop",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(14, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(16, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    calendar.updateEvent("Workshop", LocalDate.of(2025, 11, 12),
        LocalTime.of(14, 0), updated);

    assertEquals(0, listener.added.size());
    assertEquals(1, listener.modified.size());
  }

  @Test
  void multipleListenersAllNotified() {
    TestListener listener1 = new TestListener();
    TestListener listener2 = new TestListener();

    calendar.addCalendarListener(listener1);
    calendar.addCalendarListener(listener2);

    Event event = new Event(
        "Standup",
        LocalDate.of(2025, 11, 15),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 15),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    calendar.addEvent(event);

    assertEquals(1, listener1.added.size());
    assertEquals(1, listener2.added.size());
  }

  @Test
  void removedListenerNotNotified() {
    TestListener listener = new TestListener();

    calendar.addCalendarListener(listener);
    calendar.removeCalendarListener(listener);

    Event event = new Event(
        "Review",
        LocalDate.of(2025, 11, 20),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 20),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    calendar.addEvent(event);

    assertEquals(0, listener.added.size());
  }

  @Test
  void recurringEventNotifiesForEachInstance() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);

    Event template = new Event(
        "Yoga",
        LocalDate.of(2025, 11, 4),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 4),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        2,
        null
    );

    calendar.addRecurringEvent(template, rule);

    assertEquals(2, listener.added.size());
  }

  @Test
  void registeredListenerRemovalReturnsTrue() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);
    assertTrue(calendar.removeCalendarListener(listener));
  }

  @Test
  void listenerOnlyNotifiedForItsCalendar() {
    CalendarModel calendar2 = new CalendarModel("Other Calendar", false);
    TestListener listener = new TestListener();

    calendar.addCalendarListener(listener);

    Event event1 = new Event(
        "Calendar1 Event",
        LocalDate.of(2025, 11, 10),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 10),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event event2 = new Event(
        "Calendar2 Event",
        LocalDate.of(2025, 11, 11),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 11),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    calendar.addEvent(event1);
    calendar2.addEvent(event2);

    // Listener only registered to calendar, not calendar2
    assertEquals(1, listener.added.size());
    assertEquals("Calendar1 Event", listener.added.get(0).getSubject());
  }

  @Test
  void modifyRecurringEventNotifiesListeners() {
    TestListener listener = new TestListener();

    Event template = new Event(
        "Workout",
        LocalDate.of(2025, 11, 4),
        LocalTime.of(6, 0),
        LocalDate.of(2025, 11, 4),
        LocalTime.of(7, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY),
        2,
        null
    );

    calendar.addRecurringEvent(template, rule);
    calendar.addCalendarListener(listener);

    RecurrenceRule newRule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        3,
        null
    );

    calendar.modifyRecurringEvent("Workout", template, newRule);

    assertEquals(3, listener.added.size());
  }
}
