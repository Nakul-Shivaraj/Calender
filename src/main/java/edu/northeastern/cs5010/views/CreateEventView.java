package edu.northeastern.cs5010.views;

import edu.northeastern.cs5010.model.CalendarModel;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.model.Visibility;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * View for creating new events in a calendar.
 * AI-generated code with manual review.
 */
public class CreateEventView extends JFrame {

  private final CalendarModel calendar;
  private final JTextField subjectField;
  private final JTextField startDateField;
  private final JTextField startTimeField;
  private final JTextField endDateField;
  private final JTextField endTimeField;
  private final JComboBox<Visibility> visibilityBox;
  private final JTextField descriptionField;
  private final JTextField locationField;

  /**
   * Creates a new event creation view for the given calendar.
   *
   * @param calendar the calendar to add events to
   */
  public CreateEventView(CalendarModel calendar) {
    this.calendar = calendar;

    setTitle("Create Event - " + calendar.getTitle());
    setSize(400, 450);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new GridLayout(10, 2, 10, 10));

    add(new JLabel("Subject:"));
    subjectField = new JTextField();
    add(subjectField);

    add(new JLabel("Start Date (YYYY-MM-DD):"));
    startDateField = new JTextField();
    add(startDateField);

    add(new JLabel("Start Time (HH:MM) or leave empty:"));
    startTimeField = new JTextField();
    add(startTimeField);

    add(new JLabel("End Date (YYYY-MM-DD):"));
    endDateField = new JTextField();
    add(endDateField);

    add(new JLabel("End Time (HH:MM) or leave empty:"));
    endTimeField = new JTextField();
    add(endTimeField);

    add(new JLabel("Visibility:"));
    visibilityBox = new JComboBox<>(Visibility.values());
    add(visibilityBox);

    add(new JLabel("Description (optional):"));
    descriptionField = new JTextField();
    add(descriptionField);

    add(new JLabel("Location (optional):"));
    locationField = new JTextField();
    add(locationField);

    JButton saveButton = new JButton("Create Event");
    saveButton.addActionListener(e -> createEvent());
    add(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    add(cancelButton);
  }

  private void createEvent() {
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

      Event event = new Event(
          subject,
          startDate,
          startTime,
          endDate,
          endTime,
          visibility,
          description.isEmpty() ? null : description,
          location.isEmpty() ? null : location
      );

      calendar.addEvent(event);

      JOptionPane.showMessageDialog(this, "Event created successfully!",
          "Success", JOptionPane.INFORMATION_MESSAGE);

      clearFields();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void clearFields() {
    subjectField.setText("");
    startDateField.setText("");
    startTimeField.setText("");
    endDateField.setText("");
    endTimeField.setText("");
    descriptionField.setText("");
    locationField.setText("");
    visibilityBox.setSelectedIndex(0);
  }
}