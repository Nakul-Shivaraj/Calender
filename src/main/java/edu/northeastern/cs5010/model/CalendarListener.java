package edu.northeastern.cs5010.model;

/**
 * Listener interface for receiving notifications about calendar events.
 * Implementations can register with a {@link CalendarModel} to be notified
 * when events are added or modified.
 */
public interface CalendarListener {

  /**
   * Called when a new event is added to the calendar.
   *
   * @param event the event that was added
   */
  void onEventAdded(Event event);

  /**
   * Called when an existing event is modified.
   *
   * @param event the event that was modified
   */
  void onEventModified(Event event);
}