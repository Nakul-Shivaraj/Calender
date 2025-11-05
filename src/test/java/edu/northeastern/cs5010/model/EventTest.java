package edu.northeastern.cs5010.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Event class.
 */
class EventTest {

  @Test
  void createValidEvent() {
    Event event = new Event(
        "Project Demo",
        LocalDate.of(2025, 11, 5),
        LocalTime.of(14, 0),
        LocalDate.of(2025, 11, 5),
        LocalTime.of(15, 0),
        Visibility.PUBLIC,
        "Demo for stakeholders",
        "Zoom"
    );
    assertEquals("Project Demo", event.getSubject());
    assertFalse(event.isAllDay());
  }

  @Test
  void createAllDayEvent() {
    Event event = new Event(
        "Holiday",
        LocalDate.of(2025, 12, 25),
        null,
        LocalDate.of(2025, 12, 25),
        null,
        Visibility.PRIVATE,
        "Christmas Day",
        null
    );
    assertTrue(event.isAllDay());
  }

  @Test
  void invalidEventThrowsExceptions() {
    // Subject blank
    assertThrows(IllegalArgumentException.class, () ->
        new Event("", LocalDate.now(), null, LocalDate.now(), null, Visibility.PUBLIC, null, null));

    // End date before start date
    assertThrows(IllegalArgumentException.class, () ->
        new Event("Error", LocalDate.of(2025, 10, 31), null,
            LocalDate.of(2025, 10, 30), null, Visibility.PUBLIC, null, null));

    // One time null, one not
    assertThrows(IllegalArgumentException.class, () ->
        new Event("Bad", LocalDate.now(), LocalTime.NOON,
            LocalDate.now(), null, Visibility.PUBLIC, null, null));

    // Start time after end time
    assertThrows(IllegalArgumentException.class, () ->
        new Event("Reverse", LocalDate.now(), LocalTime.of(12, 0),
            LocalDate.now(), LocalTime.of(10, 0), Visibility.PUBLIC, null, null));
  }

  @Test
  void conflictDetectionWorks() {
    Event e1 = new Event(
        "Meeting",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event e2 = new Event(
        "Overlap",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 30),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(10, 30),
        Visibility.PRIVATE,
        null,
        null
    );

    Event e3 = new Event(
        "NonOverlap",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(11, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(12, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    assertTrue(e1.conflictsWith(e2));
    assertFalse(e1.conflictsWith(e3));
  }

  @Test
  void allDayConflictsWorkAcrossDays() {
    Event e1 = new Event(
        "Holiday1",
        LocalDate.of(2025, 11, 1),
        null,
        LocalDate.of(2025, 11, 2),
        null,
        Visibility.PUBLIC,
        null,
        null
    );

    Event e2 = new Event(
        "Holiday2",
        LocalDate.of(2025, 11, 2),
        null,
        LocalDate.of(2025, 11, 3),
        null,
        Visibility.PUBLIC,
        null,
        null
    );

    assertTrue(e1.conflictsWith(e2));

    // Non-overlapping all-day events should not conflict
    Event e3 = new Event(
        "Holiday3",
        LocalDate.of(2025, 11, 5),
        null,
        LocalDate.of(2025, 11, 6),
        null,
        Visibility.PUBLIC,
        null,
        null
    );
    assertFalse(e1.conflictsWith(e3));
  }

  @Test
  void constructorRejectsNullDates() {
    assertThrows(IllegalArgumentException.class, () -> new Event(
        "Null Date Test",
        null,
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 10), // endDate
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        "Should fail - null start date",
        null
    ));

    assertThrows(IllegalArgumentException.class, () -> new Event(
        "Null Date Test",
        LocalDate.of(2025, 11, 10), // startDate
        LocalTime.of(9, 0),
        null,
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        "Should fail - null end date",
        null
    ));
  }


  @Test
  void toStringAndEqualityWork() {
    Event e1 = new Event(
        "Call",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(8, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 0),
        Visibility.PUBLIC,
        "Morning call",
        "Office"
    );

    Event e2 = new Event(
        "Call",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(8, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 0),
        Visibility.PUBLIC,
        "Morning call",
        "Office"
    );

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
    assertTrue(e1.toString().contains("Call"));
    assertTrue(e1.toString().contains("2025"));
    assertNotEquals(e1, null);
    assertNotEquals(e1, "random string");
  }
}
