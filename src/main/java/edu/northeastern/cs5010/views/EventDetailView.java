package edu.northeastern.cs5010.views;

import edu.northeastern.cs5010.model.CalendarModel;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.model.Visibility;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * View for displaying and editing event details.
 * AI-generated code with manual review.
 */
public class EventDetailView extends JFrame {

  private final CalendarModel calendar;
  private final Event originalEvent;
  private final JTextField subjectField;
  private final JTextField startDateField;
  private final JTextField startTimeField;
  private final JTextField endDateField;
  private final JTextField endTimeField;
  private final JComboBox<Visibility> visibilityBox;
  private final JTextField descriptionField;
  private final JTextField locationField;

  /**
   * Creates a new event detail view for the given event.
   *
   * @param event the event to display and edit
   * @param calendar the calendar containing the event
   */
  public EventDetailView(Event event, CalendarModel calendar) {
    this.originalEvent = event;
    this.calendar = calendar;

    setTitle("Event Details - " + event.getSubject());
    setSize(400, 450);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new GridLayout(10, 2, 10, 10));

    add(new JLabel("Subject:"));
    subjectField = new JTextField(event.getSubject());
    add(subjectField);

    add(new JLabel("Start Date (YYYY-MM-DD):"));
    startDateField = new JTextField(event.getStartDate().toString());
    add(startDateField);

    add(new JLabel("Start Time (HH:MM) or leave empty:"));
    startTimeField = new JTextField(
        event.getStartTime() != null ? event.getStartTime().toString() : "");
    add(startTimeField);

    add(new JLabel("End Date (YYYY-MM-DD):"));
    endDateField = new JTextField(event.getEndDate().toString());
    add(endDateField);

    add(new JLabel("End Time (HH:MM) or leave empty:"));
    endTimeField = new JTextField(
        event.getEndTime() != null ? event.getEndTime().toString() : "");
    add(endTimeField);

    add(new JLabel("Visibility:"));
    visibilityBox = new JComboBox<>(Visibility.values());
    visibilityBox.setSelectedItem(event.getVisibility());
    add(visibilityBox);

    add(new JLabel("Description:"));
    descriptionField = new JTextField(
        event.getDescription() != null ? event.getDescription() : "");
    add(descriptionField);

    add(new JLabel("Location:"));
    locationField = new JTextField(
        event.getLocation() != null ? event.getLocation() : "");
    add(locationField);

    JButton saveButton = new JButton("Save Changes");
    saveButton.addActionListener(e -> saveChanges());
    add(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    add(cancelButton);
  }

  private void saveChanges() {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Subject is required.",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
      LocalDate endDate = LocalDate.parse(endDateField.getText().trim());

      String startTimeText = startTimeField.getText().trim();
      String endTimeText = endTimeField.getText().trim();

      LocalTime startTime = startTimeText.isEmpty() ? null : LocalTime.parse(startTimeText);
      LocalTime endTime = endTimeText.isEmpty() ? null : LocalTime.parse(endTimeText);

      Visibility visibility = (Visibility) visibilityBox.getSelectedItem();

      String description = descriptionField.getText().trim();
      String location = locationField.getText().trim();

      Event updatedEvent = new Event(
          subject,
          startDate,
          startTime,
          endDate,
          endTime,
          visibility,
          description.isEmpty() ? null : description,
          location.isEmpty() ? null : location
      );

      calendar.updateEvent(
          originalEvent.getSubject(),
          originalEvent.getStartDate(),
          originalEvent.getStartTime(),
          updatedEvent
      );

      JOptionPane.showMessageDialog(this, "Event updated successfully!",
          "Success", JOptionPane.INFORMATION_MESSAGE);

      dispose();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}