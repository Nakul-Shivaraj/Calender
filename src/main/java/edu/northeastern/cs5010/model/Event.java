package edu.northeastern.cs5010.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a single event in a calendar.
 * Each event has required and optional details such as subject,
 * start and end dates, and optional times.
 */
public final class Event {

  private final String subject;
  private final LocalDate startDate;
  private final LocalTime startTime;
  private final LocalDate endDate;
  private final LocalTime endTime;
  private final Visibility visibility;
  private final String description;
  private final String location;

  /**
   * Constructs a new event with the specified details.
   *
   * @param subject      the title or name of the event (required)
   * @param startDate    the start date (required)
   * @param startTime    the start time, or {@code null} if all-day
   * @param endDate      the end date (required)
   * @param endTime      the end time, or {@code null} if all-day
   * @param visibility   visibility level (optional, default PUBLIC)
   * @param description  description text (optional)
   * @param location     event location (optional)
   * @throws IllegalArgumentException if parameters are invalid
   */
  public Event(String subject, LocalDate startDate, LocalTime startTime,
      LocalDate endDate, LocalTime endTime,
      Visibility visibility, String description, String location) {

    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("Subject must not be null or blank.");
    }
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates must not be null.");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before or equal to end date.");
    }
    if ((startTime == null && endTime != null) || (startTime != null && endTime == null)) {
      throw new IllegalArgumentException(
          "Either both times must be null (all-day) or both must be provided.");
    }
    if (startDate.equals(endDate) && startTime != null && !startTime.isBefore(endTime)) {
      throw new IllegalArgumentException("Start time must be before end time on the same day.");
    }

    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endDate = endDate;
    this.endTime = endTime;
    this.visibility = (visibility == null) ? Visibility.PUBLIC : visibility;
    this.description = description;
    this.location = location;
  }

  /**
   * Returns whether the event is an all-day event.
   *
   * @return {@code true} if this event has no start or end time, otherwise {@code false}.
   */
  public boolean isAllDay() {
    return startTime == null;
  }

  /**
   * Checks whether this event conflicts with another event.
   *
   * @param other another event
   * @return {@code true} if time intervals overlap
   */
  public boolean conflictsWith(Event other) {
    if (other == null) {
      return false;
    }

    // All-day events: conflict if their dates overlap
    if (this.isAllDay() && other.isAllDay()) {
      return !(this.endDate.isBefore(other.startDate)
          || this.startDate.isAfter(other.endDate));
    }

    LocalTime thisStart = (this.startTime == null) ? LocalTime.MIN : this.startTime;
    LocalTime thisEnd = (this.endTime == null) ? LocalTime.MAX : this.endTime;
    LocalTime otherStart = (other.startTime == null) ? LocalTime.MIN : other.startTime;
    LocalTime otherEnd = (other.endTime == null) ? LocalTime.MAX : other.endTime;

    boolean datesOverlap = !this.endDate.isBefore(other.startDate)
        && !other.endDate.isBefore(this.startDate);
    boolean timesOverlap = thisEnd.isAfter(otherStart) && otherEnd.isAfter(thisStart);

    return datesOverlap && timesOverlap;
  }

  // ---- Getters ----
  public String getSubject() {
    return subject;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public String toString() {
    return String.format("%s (%s %s - %s %s)",
        subject, startDate, startTime, endDate, endTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event event)) {
      return false;
    }
    return Objects.equals(subject, event.subject)
        && Objects.equals(startDate, event.startDate)
        && Objects.equals(startTime, event.startTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDate, startTime);
  }
}
