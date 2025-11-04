package edu.northeastern.cs5010.model;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.time.*;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CalendarModelTest {

  @Test
  void addEventSuccessfully() {
    CalendarModel calendar = new CalendarModel("Work Calendar", true);

    Event meeting = new Event(
        "Team Meeting",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        "Weekly sync",
        "Room 101"
    );
    calendar.addEvent(meeting);
    assertEquals(1, calendar.getEvents().size());
  }

  @Test
  void rejectDuplicateEvents() {
    CalendarModel calendar = new CalendarModel("Work Calendar", true);

    Event e1 = new Event(
        "Standup",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 15),
        Visibility.PUBLIC,
        null,
        null
    );
    calendar.addEvent(e1);

    Event duplicate = new Event(
        "Standup",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 15),
        Visibility.PUBLIC,
        null,
        null
    );
    assertThrows(IllegalArgumentException.class, () -> calendar.addEvent(duplicate));
  }

  @Test
  void rejectConflictingEventsWhenNotAllowed() {
    CalendarModel calendar = new CalendarModel("Work Calendar", false);
    Event e1 = new Event(
        "Meeting A",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event e2 = new Event(
        "Meeting B",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(10, 30),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(11, 30),
        Visibility.PRIVATE,
        null,
        null
    );
    calendar.addEvent(e1);
    assertThrows(IllegalArgumentException.class, () -> calendar.addEvent(e2));
  }

  @Test
  void getEventsOnReturnsCorrectEvents() {
    CalendarModel calendar = new CalendarModel("Personal", true);

    Event lunch = new Event(
        "Lunch",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(12, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(13, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event dinner = new Event(
        "Dinner",
        LocalDate.of(2025, 10, 31),
        LocalTime.of(18, 0),
        LocalDate.of(2025, 10, 31),
        LocalTime.of(19, 0),
        Visibility.PRIVATE,
        null,
        null
    );
    calendar.addEvent(lunch);
    calendar.addEvent(dinner);

    List<Event> events = calendar.getEventsOn(LocalDate.of(2025, 10, 30));
    assertEquals(1, events.size());
    assertEquals("Lunch", events.get(0).getSubject());
  }

  @Test
  void isBusyDetectsConflictCorrectly() {
    CalendarModel calendar = new CalendarModel("Work", true);

    Event event = new Event(
        "Morning Call",
        LocalDate.of(2025, 10, 30),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 10, 30),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );
    calendar.addEvent(event);
    assertTrue(calendar.isBusy(LocalDate.of(2025, 10, 30), LocalTime.of(9, 30)));
    assertFalse(calendar.isBusy(LocalDate.of(2025, 10, 30), LocalTime.of(11, 0)));
  }

  // === Extended coverage tests for rubric-required methods ===

  @Test
  void findEventReturnsCorrectEvent() {
    CalendarModel cal = new CalendarModel("Work", true);
    Event e = new Event(
        "Workshop",
        LocalDate.of(2025, 11, 10),
        LocalTime.of(14, 0),
        LocalDate.of(2025, 11, 10),
        LocalTime.of(15, 0),
        Visibility.PUBLIC,
        null,
        "Room A"
    );
    cal.addEvent(e);

    Event found = cal.findEvent("Workshop", LocalDate.of(2025, 11, 10), LocalTime.of(14, 0));
    assertNotNull(found);
    assertEquals("Workshop", found.getSubject());
  }

  @Test
  void getEventsInRangeIncludesExpectedEvents() {
    CalendarModel cal = new CalendarModel("Personal", true);

    Event e1 = new Event(
        "Conference",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event e2 = new Event(
        "Training",
        LocalDate.of(2025, 11, 6),
        LocalTime.of(11, 0),
        LocalDate.of(2025, 11, 6),
        LocalTime.of(12, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    cal.addEvent(e1);
    cal.addEvent(e2);

    List<Event> result = cal.getEventsInRange(LocalDate.of(2025, 11, 2), LocalDate.of(2025, 11, 4));
    assertEquals(1, result.size());
    assertEquals("Conference", result.get(0).getSubject());
  }

  @Test
  void updateEventSuccessfullyReplacesOldOne() {
    CalendarModel cal = new CalendarModel("Work", true);

    Event old = new Event(
        "Demo",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        "Original demo",
        null
    );
    cal.addEvent(old);

    Event updated = new Event(
        "Demo",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 30),
        Visibility.PUBLIC,
        "Extended demo",
        null
    );

    Event result = cal.updateEvent("Demo",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        updated);

    assertEquals("Extended demo", result.getDescription());
    assertEquals(1, cal.getEvents().size());
  }

  @Test
  void addRecurringEventAndExportCsvWork() throws IOException {
    CalendarModel cal = new CalendarModel("My Calendar", false);

    Event base = new Event(
        "Yoga",
        LocalDate.of(2025, 11, 1),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 1),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        "Morning session",
        "Studio A"
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        3,
        LocalDate.of(2025, 11, 30)
    );

    cal.addRecurringEvent(base, rule);
    assertFalse(cal.getEvents().isEmpty());
    assertEquals("My Calendar", cal.getTitle());
    assertFalse(cal.allowsConflicts());

    var file = Files.createTempFile("calendar", ".csv");
    cal.exportToCsv(file.toString());
    String csv = Files.readString(file);
    assertTrue(csv.contains("Yoga"));
    Files.deleteIfExists(file);
  }

  @Test
  void modifyRecurringEventReplacesOldSeries() {
    CalendarModel cal = new CalendarModel("Workout Calendar", false);

    // Original recurring event
    Event base = new Event(
        "Workout",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        "Morning session",
        "Gym A"
    );

    RecurrenceRule oldRule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        2,
        LocalDate.of(2025, 11, 30)
    );
    cal.addRecurringEvent(base, oldRule);
    int oldCount = cal.getEvents().size();

    // Modified recurring pattern (e.g., add Friday)
    RecurrenceRule newRule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        3,
        LocalDate.of(2025, 11, 30)
    );

    cal.modifyRecurringEvent("Workout", base, newRule);

    // The updated recurrence should overwrite the old one
    assertTrue(cal.getEvents().size() > oldCount);
    assertTrue(cal.getEvents().stream().allMatch(e -> e.getSubject().equals("Workout")));
  }

  @Test
  void updateEventRejectsConflictWhenNotAllowed() {
    CalendarModel cal = new CalendarModel("Strict Calendar", false);

    Event e1 = new Event(
        "Meeting A",
        LocalDate.of(2025, 11, 15),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 15),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );
    Event e2 = new Event(
        "Meeting B",
        LocalDate.of(2025, 11, 15),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 15),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    cal.addEvent(e1);
    cal.addEvent(e2);

    // Try to update Meeting B to overlap with Meeting A
    Event updatedB = new Event(
        "Meeting B",
        LocalDate.of(2025, 11, 15),
        LocalTime.of(9, 30),
        LocalDate.of(2025, 11, 15),
        LocalTime.of(10, 30),
        Visibility.PUBLIC,
        null,
        null
    );

    assertThrows(IllegalArgumentException.class, () ->
        cal.updateEvent("Meeting B", LocalDate.of(2025, 11, 15), LocalTime.of(10, 0), updatedB));
  }

  @Test
  void recurrenceRuleFiniteAndEqualityBehaveCorrectly() {
    LocalDate endDate = LocalDate.of(2025, 12, 31);

    RecurrenceRule finiteRule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        5,
        endDate
    );

    RecurrenceRule infiniteRule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY),
        0,
        null
    );

    // check that isFinite() returns true/false appropriately
    assertTrue(finiteRule.isFinite());
    assertFalse(infiniteRule.isFinite());

    // check equality and toString coverage
    RecurrenceRule sameRule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        5,
        endDate
    );
    assertEquals(finiteRule, sameRule);
    assertTrue(finiteRule.toString().contains("MONDAY"));
  }

  @Test
  void getEventsInRangeRejectsInvalidInput() {
    CalendarModel cal = new CalendarModel("Errors", true);

    assertThrows(IllegalArgumentException.class,
        () -> cal.getEventsInRange(null, LocalDate.now()));

    assertThrows(IllegalArgumentException.class,
        () -> cal.getEventsInRange(LocalDate.now(), null));

    assertThrows(IllegalArgumentException.class,
        () -> cal.getEventsInRange(LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 1)));
  }

  @Test
  void updateEventThrowsIfEventNotFound() {
    CalendarModel cal = new CalendarModel("Update Fail", true);
    Event updated = new Event(
        "Nonexistent",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        "Should fail",
        null
    );

    assertThrows(IllegalArgumentException.class, () ->
        cal.updateEvent("Nonexistent", LocalDate.of(2025, 11, 12), LocalTime.of(10, 0), updated));
  }

  @Test
  void addRecurringEventRejectsMultiDayTemplate() {
    CalendarModel cal = new CalendarModel("Recurring Fail", false);

    Event base = new Event(
        "MultiDay",
        LocalDate.of(2025, 11, 1),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 2),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        "Spans two days",
        null
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY),
        5,
        LocalDate.of(2025, 11, 30)
    );

    assertThrows(IllegalArgumentException.class, () -> cal.addRecurringEvent(base, rule));
  }

  @Test
  void constructorRejectsBlankTitle() {
    assertThrows(IllegalArgumentException.class, () -> new CalendarModel("  ", true));
  }

  @Test
  void addEventRejectsDuplicateEvent() {
    CalendarModel cal = new CalendarModel("Dup Test", true);
    Event e = new Event(
        "Meeting",
        LocalDate.of(2025, 11, 20),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 20),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        "Morning sync",
        null
    );
    cal.addEvent(e);
    assertThrows(IllegalArgumentException.class, () -> cal.addEvent(e));
  }

  @Test
  void addRecurringEventDetectsConflictWhenNotAllowed() {
    CalendarModel cal = new CalendarModel("Recurring Conflict", false);

    Event existing = new Event(
        "Yoga",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        "Existing class",
        null
    );
    cal.addEvent(existing);

    Event base = new Event(
        "Yoga",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        "Recurring conflict",
        null
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY),
        2,
        LocalDate.of(2025, 11, 10)
    );

    assertThrows(IllegalArgumentException.class, () -> cal.addRecurringEvent(base, rule));
  }

  @Test
  void recurrenceRuleRejectsEmptyOrNullDaysOfWeek() {
    // Empty set
    assertThrows(IllegalArgumentException.class, () -> new RecurrenceRule(
        Set.of(),5,LocalDate.of(2025, 11, 30)
    ));

    // Null daysOfWeek
    assertThrows(IllegalArgumentException.class, () -> new RecurrenceRule(
        null,5,LocalDate.of(2025, 11, 30)
    ));
  }

  @Test
  void recurrenceRuleRejectsNegativeOccurrences() {
    assertThrows(IllegalArgumentException.class, () -> new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),-1,LocalDate.of(2025, 11, 30)
    ));
  }

  @Test
  void addRecurringEventRejectsDuplicateInstance() {
    CalendarModel cal = new CalendarModel("Dup Recurrence", true);

    Event base = new Event(
        "Yoga",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        "Morning yoga",
        null
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY),
        2,
        LocalDate.of(2025, 11, 10)
    );

    // First addition OK
    cal.addRecurringEvent(base, rule);

    // Second addition with same pattern → duplicate recurring event
    assertThrows(IllegalArgumentException.class, () -> cal.addRecurringEvent(base, rule));
  }

  @Test
  void addRecurringEventRejectsConflictWhenConflictsNotAllowed() {
    CalendarModel cal = new CalendarModel("No Conflict Recurrence", false);

    // Existing event on Monday 7–8am
    Event existing = new Event(
        "Existing Yoga",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(7, 0),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(8, 0),
        Visibility.PUBLIC,
        null,
        null
    );
    cal.addEvent(existing);

    // Another recurring event overlapping same time
    Event base = new Event(
        "Yoga",
        LocalDate.of(2025, 11, 3),
        LocalTime.of(7, 30),
        LocalDate.of(2025, 11, 3),
        LocalTime.of(8, 30),
        Visibility.PUBLIC,
        "Overlap",
        null
    );

    RecurrenceRule rule = new RecurrenceRule(
        Set.of(DayOfWeek.MONDAY),
        2,
        LocalDate.of(2025, 11, 10)
    );

    assertThrows(IllegalArgumentException.class, () -> cal.addRecurringEvent(base, rule));
  }

  @Test
  void updateEventRejectsDuplicateEvent() {
    CalendarModel cal = new CalendarModel("Dup Update", true);

    Event e1 = new Event(
        "Meeting",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event e2 = new Event(
        "Standup",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 30),
        Visibility.PUBLIC,
        null,
        null
    );

    cal.addEvent(e1);
    cal.addEvent(e2);

    // Try to update e1 to become identical to e2
    Event duplicateUpdate = new Event(
        "Standup",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 30),
        Visibility.PUBLIC,
        null,
        null
    );

    assertThrows(IllegalArgumentException.class, () ->
        cal.updateEvent("Meeting", LocalDate.of(2025, 11, 12),
            LocalTime.of(9, 0), duplicateUpdate));
  }

  @Test
  void updateEventRejectsConflictingChange() {
    CalendarModel cal = new CalendarModel("Conflict Update", false);

    Event e1 = new Event(
        "Event1",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    Event e2 = new Event(
        "Event2",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 0),
        Visibility.PUBLIC,
        null,
        null
    );

    cal.addEvent(e1);
    cal.addEvent(e2);

    // Update e1 to overlap with e2
    Event updated = new Event(
        "Event1",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 30),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(11, 30),
        Visibility.PUBLIC,
        null,
        null
    );

    assertThrows(IllegalArgumentException.class, () ->
        cal.updateEvent("Event1", LocalDate.of(2025, 11, 12),
            LocalTime.of(9, 0), updated));
  }

  @Test
  void findEventReturnsNullWhenNotFound() {
    CalendarModel cal = new CalendarModel("Finder", true);

    Event e = new Event(
        "Existing",
        LocalDate.of(2025, 11, 12),
        LocalTime.of(9, 0),
        LocalDate.of(2025, 11, 12),
        LocalTime.of(10, 0),
        Visibility.PUBLIC,
        null,
        null
    );
    cal.addEvent(e);

    // Query with different time → not found
    assertNull(cal.findEvent("Existing", LocalDate.of(2025, 11, 12), LocalTime.of(11, 0)));
  }

  @Test
  void escapeCsvHandlesCommasQuotesAndNewlines() throws Exception {
    Method escapeCsv = CalendarModel.class.getDeclaredMethod("escapeCsv", String.class);
    escapeCsv.setAccessible(true);

    // Case 1: Contains comma → should wrap in quotes
    String result1 = (String) escapeCsv.invoke(null, (Object) "Hello,World");
    assertEquals("\"Hello,World\"", result1);

    // Case 2: Contains double quotes → should escape and wrap
    String result2 = (String) escapeCsv.invoke(null, (Object) "He said \"Hi\"");
    assertEquals("\"He said \"\"Hi\"\"\"", result2);

    // Case 3: Contains newline → should wrap in quotes
    String result3 = (String) escapeCsv.invoke(null, (Object) "Line1\nLine2");
    assertEquals("\"Line1\nLine2\"", result3);

    // Case 4: Blank input → should return empty string
    String result4 = (String) escapeCsv.invoke(null, (Object) "   ");
    assertEquals("", result4);
  }
}
