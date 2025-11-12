package edu.northeastern.cs5010.model;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a recurrence pattern for repeating events.
 * Defines days of the week on which an event repeats,
 * and either a number of occurrences or an end date.
 */
public final class RecurrenceRule implements Serializable {

  private final Set<DayOfWeek> daysOfWeek;
  private final int occurrences;
  private final LocalDate endDate;

  /**
   * Creates a recurrence rule.
   *
   * @param daysOfWeek  days the event repeats on (required)
   * @param occurrences number of occurrences (0 = unlimited)
   * @param endDate     optional end date
   * @throws IllegalArgumentException if {@code daysOfWeek} is empty or occurrences less than 0
   */
  public RecurrenceRule(Set<DayOfWeek> daysOfWeek, int occurrences, LocalDate endDate) {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("At least one day of the week must be specified.");
    }
    if (occurrences < 0) {
      throw new IllegalArgumentException("Occurrences cannot be negative.");
    }

    this.daysOfWeek = Set.copyOf(new HashSet<>(daysOfWeek));
    this.occurrences = occurrences;
    this.endDate = endDate;
  }

  /**
   * Returns the days of the week on which the event repeats.
   *
   * @return immutable set of {@link DayOfWeek}
   */
  public Set<DayOfWeek> getDaysOfWeek() {
    return daysOfWeek;
  }

  /**
   * Returns the number of occurrences.
   *
   * @return number of times the event repeats (0 = unlimited)
   */
  public int getOccurrences() {
    return occurrences;
  }

  /**
   * Returns the end date of the recurrence, or {@code null} if none.
   *
   * @return end date, or {@code null}
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isFinite() {
    return occurrences > 0 || endDate != null;
  }

  @Override
  public String toString() {
    return String.format("Repeats on %s, %s, until %s",
        daysOfWeek, (occurrences == 0 ? "unlimited" : occurrences + " times"),
        (endDate == null ? "no end date" : endDate));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RecurrenceRule that)) {
      return false;
    }
    return occurrences == that.occurrences
        && Objects.equals(daysOfWeek, that.daysOfWeek)
        && Objects.equals(endDate, that.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(daysOfWeek, occurrences, endDate);
  }
}
