package edu.northeastern.cs5010.controller;


import edu.northeastern.cs5010.model.CalendarModel;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.views.CreateEventView;
import edu.northeastern.cs5010.views.EventDetailView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the calendar application.
 * Manages calendars and coordinates between model and views.
 */
public class CalendarController {

  private static final String SAVE_FILE = "calendars.dat";

  /**
   * Main entry point for the calendar application.
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    List<CalendarModel> calendars;

    try {
      File file = new File(SAVE_FILE);
      if (file.exists()) {
        calendars = CalendarModel.restoreAllCalendars(SAVE_FILE);
        System.out.println("Restored " + calendars.size() + " calendar(s).");
      } else {
        calendars = new ArrayList<>();
        System.out.println("No saved calendars found. Starting fresh.");
      }
    } catch (IOException e) {
      System.err.println("Error restoring calendars: " + e.getMessage());
      calendars = new ArrayList<>();
    }

    CalendarModel calendar;
    if (calendars.isEmpty()) {
      calendar = new CalendarModel("My Calendar", false);
      calendars.add(calendar);
      System.out.println("Created new calendar: " + calendar.getTitle());
    } else {
      calendar = calendars.getFirst();
      System.out.println("Selected calendar: " + calendar.getTitle());
    }

    CreateEventView createView = new CreateEventView(calendar);
    createView.setVisible(true);

    if (!calendar.getEvents().isEmpty()) {
      Event event = calendar.getEvents().getFirst();
      EventDetailView detailView = new EventDetailView(event, calendar);
      detailView.setVisible(true);
      System.out.println("Opened event detail for: " + event.getSubject());
    } else {
      System.out.println("No events to display in detail view.");
    }

    final List<CalendarModel> finalCalendars = calendars;
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        CalendarModel.saveAllCalendars(finalCalendars, SAVE_FILE);
        System.out.println("Calendars saved successfully.");
      } catch (IOException e) {
        System.err.println("Error saving calendars: " + e.getMessage());
      }
    }));
  }
}