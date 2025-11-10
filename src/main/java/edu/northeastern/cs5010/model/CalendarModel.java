package edu.northeastern.cs5010.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a calendar that stores multiple events.
 * Supports adding single and recurring events, conflict detection,
 * querying by date or time, and CSV export.
 */
public final class CalendarModel {

  private final String title;
  private final List<Event> events = new ArrayList<>();
  private final boolean allowConflicts;
  private final List<CalendarListener> listeners = new ArrayList<>();

  /**
   * Constructs a calendar with a given title and conflict policy.
   *
   * @param title calendar title (required)
   * @param allowConflicts whether overlapping events are allowed
   * @throws IllegalArgumentException if title is null or blank
   */
  public CalendarModel(String title, boolean allowConflicts) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Calendar title must not be blank.");
    }
    this.title = title;
    this.allowConflicts = allowConflicts;
  }

  /**
   * Adds a single event to the calendar if valid.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if duplicate or conflict detected
   */
  public void addEvent(Event event) {
    Objects.requireNonNull(event, "Event cannot be null.");
    for (Event e : events) {
      if (e.equals(event)) {
        throw new IllegalArgumentException("Duplicate event not allowed.");
      }
      if (!allowConflicts && e.conflictsWith(event)) {
        throw new IllegalArgumentException("Conflicting event not allowed.");
      }
    }
    events.add(event);
    announceEventAdded(event);
  }

  /**
   * Adds a recurring event expanded from a base event and recurrence rule.
   *
   * @param template base event template
   * @param rule     recurrence rule defining repetition pattern
   * @throws IllegalArgumentException if invalid or conflicting
   */
  public void addRecurringEvent(Event template, RecurrenceRule rule) {
    Objects.requireNonNull(template, "Event template cannot be null.");
    Objects.requireNonNull(rule, "Recurrence rule cannot be null.");

    if (!template.getStartDate().equals(template.getEndDate())) {
      throw new IllegalArgumentException(
          "Recurring events must start and end on the same day.");
    }

    LocalDate date = template.getStartDate();
    int added = 0;

    while (true) {
      if (rule.getEndDate() != null && date.isAfter(rule.getEndDate())) {
        break;
      }
      if (rule.getOccurrences() > 0 && added >= rule.getOccurrences()) {
        break;
      }

      if (rule.getDaysOfWeek().contains(date.getDayOfWeek())) {
        Event instance = new Event(
            template.getSubject(),
            date,
            template.getStartTime(),
            date,
            template.getEndTime(),
            template.getVisibility(),
            template.getDescription(),
            template.getLocation()
        );

        for (Event existing : events) {
          if (existing.equals(instance)) {
            throw new IllegalArgumentException(
                "Duplicate recurring event instance: " + instance.getSubject());
          }
          if (!allowConflicts && existing.conflictsWith(instance)) {
            throw new IllegalArgumentException(
                "Recurring event conflicts with existing event: " + instance.getSubject());
          }
        }

        events.add(instance);
        announceEventAdded(instance);
        added++;
      }

      date = date.plusDays(1);
    }
  }

  /**
   * Modifies an existing non-repeating event.
   * Replaces it with an updated version after validation.
   *
   * @param oldSubject subject of the event to modify
   * @param oldStartDate start date of the event to modify
   * @param oldStartTime start time of the event to modify
   * @param updated new event object with desired changes
   * @return the updated {@link Event} after successful replacement
   * @throws IllegalArgumentException if not found or conflicts detected
   */
  public Event updateEvent(String oldSubject, LocalDate oldStartDate,
      LocalTime oldStartTime, Event updated) {
    Objects.requireNonNull(updated, "Updated event cannot be null.");

    // Find the target event
    Event target = null;
    for (Event e : events) {
      if (e.getSubject().equals(oldSubject)
          && e.getStartDate().equals(oldStartDate)
          && Objects.equals(e.getStartTime(), oldStartTime)) {
        target = e;
        break;
      }
    }

    if (target == null) {
      throw new IllegalArgumentException("Event to modify not found.");
    }

    // Temporarily remove to avoid self-conflict
    events.remove(target);

    for (Event e : events) {
      if (e.equals(updated)) {
        throw new IllegalArgumentException("Duplicate event not allowed.");
      }
      if (!allowConflicts && e.conflictsWith(updated)) {
        throw new IllegalArgumentException("Updated event causes a conflict.");
      }
    }

    events.add(updated);
    announceEventModified(updated);
    return updated;
  }

  /**
   * Modifies all recurring event instances for a given subject.
   * Replaces them with new ones generated from the updated rule.
   *
   * @param subject subject of the recurring series
   * @param template new base event template
   * @param newRule new recurrence pattern
   * @throws IllegalArgumentException if invalid or conflicting
   */
  public void modifyRecurringEvent(String subject, Event template, RecurrenceRule newRule) {
    Objects.requireNonNull(subject, "Subject cannot be null.");
    Objects.requireNonNull(template, "Updated event template cannot be null.");
    Objects.requireNonNull(newRule, "New recurrence rule cannot be null.");

    // Remove all existing events in the series
    events.removeIf(e -> e.getSubject().equals(subject));

    // Add the new recurrence
    addRecurringEvent(template, newRule);
  }

  /**
   * Retrieves an event using its unique subject, start date, and start time.
   *
   * @param subject event subject
   * @param startDate start date
   * @param startTime start time (nullable for all-day events)
   * @return matching event or {@code null} if not found
   */
  public Event findEvent(String subject, LocalDate startDate, LocalTime startTime) {
    for (Event e : events) {
      if (e.getSubject().equals(subject)
          && e.getStartDate().equals(startDate)
          && Objects.equals(e.getStartTime(), startTime)) {
        return e;
      }
    }
    return null;
  }

  /**
   * Retrieves all events occurring on a specific date.
   *
   * @param date target date
   * @return list of matching events
   */
  public List<Event> getEventsOn(LocalDate date) {
    List<Event> result = new ArrayList<>();
    for (Event e : events) {
      if (!e.getStartDate().isAfter(date) && !e.getEndDate().isBefore(date)) {
        result.add(e);
      }
    }
    return List.copyOf(result);
  }

  /**
   * Retrieves all events within a date range.
   *
   * @param fromInclusive start date (inclusive)
   * @param toInclusive end date (inclusive)
   * @return list of overlapping events
   * @throws IllegalArgumentException if invalid range
   */
  public List<Event> getEventsInRange(LocalDate fromInclusive, LocalDate toInclusive) {
    if (fromInclusive == null || toInclusive == null) {
      throw new IllegalArgumentException("Date range cannot be null.");
    }
    if (fromInclusive.isAfter(toInclusive)) {
      throw new IllegalArgumentException("Start date must be before or equal to end date.");
    }

    List<Event> result = new ArrayList<>();
    for (Event e : events) {
      boolean overlap = !e.getEndDate().isBefore(fromInclusive)
          && !e.getStartDate().isAfter(toInclusive);
      if (overlap) {
        result.add(e);
      }
    }
    return List.copyOf(result);
  }

  /**
   * Checks if the user is busy at a given date/time.
   *
   * @param date date to check
   * @param time time to check
   * @return {@code true} if an event overlaps with the given time
   */
  public boolean isBusy(LocalDate date, LocalTime time) {
    for (Event e : events) {
      if (!e.getStartDate().isAfter(date) && !e.getEndDate().isBefore(date)) {
        if (e.isAllDay()) {
          return true;
        }
        if (time != null && !time.isBefore(e.getStartTime())
            && time.isBefore(e.getEndTime())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Exports this calendar to a Google Calendar–compatible CSV file.
   * This method was AI-assisted and manually reviewed.
   *
   * @param filePath output CSV file path
   * @throws IOException if an I/O error occurs
   */
  public void exportToCsv(String filePath) throws IOException {
    Objects.requireNonNull(filePath, "File path cannot be null.");

    try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8)) {
      writer.println("Subject,Start Date,Start Time,End Date,End Time,"
          + "All Day Event,Description,Location,Private");

      for (Event e : events) {
        String subject = escapeCsv(e.getSubject());
        String startDate = e.getStartDate().toString();
        String startTime = (e.getStartTime() != null) ? e.getStartTime().toString() : "";
        String endDate = e.getEndDate().toString();
        String endTime = (e.getEndTime() != null) ? e.getEndTime().toString() : "";
        String allDay = e.isAllDay() ? "True" : "False";
        String description = escapeCsv(e.getDescription());
        String location = escapeCsv(e.getLocation());
        String isPrivate = (e.getVisibility() == Visibility.PRIVATE) ? "True" : "False";

        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
            subject, startDate, startTime, endDate, endTime,
            allDay, description, location, isPrivate);
      }
    }
  }

  // Escapes commas/quotes for CSV format
  private static String escapeCsv(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }
    String escaped = text.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }

  /**
   * Registers a listener to receive notifications when events are added or modified.
   *
   * @param listener the listener to register
   * @throws IllegalArgumentException if listener is null
   */
  public void addCalendarListener(CalendarListener listener) {
    Objects.requireNonNull(listener, "Listener cannot be null.");
    listeners.add(listener);
  }

  /**
   * Removes a previously registered listener.
   *
   * @param listener the listener to remove
   * @return true if the listener was found and removed, false otherwise
   */
  public boolean removeCalendarListener(CalendarListener listener) {
    return listeners.remove(listener);
  }

  /**
   * Notifies all registered listeners that an event was added.
   *
   * @param event the event that was added
   */
  private void announceEventAdded(Event event) {
    for (CalendarListener listener : listeners) {
      listener.onEventAdded(event);
    }
  }

  /**
   * Notifies all registered listeners that an event was modified.
   *
   * @param event the event that was modified
   */
  private void announceEventModified(Event event) {
    for (CalendarListener listener : listeners) {
      listener.onEventModified(event);
    }
  }

  public String getTitle() {
    return title;
  }

  public List<Event> getEvents() {
    return List.copyOf(events);
  }

  /**
   * Returns whether this calendar allows event conflicts.
   *
   * @return {@code true} if conflicts are allowed; {@code false} otherwise
   */
  public boolean allowsConflicts() {
    return allowConflicts;
  }
}
