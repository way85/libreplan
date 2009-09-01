package org.navalplanner.web.calendars;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * This interface contains the operations to create/edit a {@link BaseCalendar}.
 * The creation/edition process of a {@link BaseCalendar} is conversational.
 *
 * <strong>Conversation state</strong>: the {@link BaseCalendar} instance.
 *
 * <strong>Non conversational steps</strong>: <code>getBaseCalendars</code> (to
 * return all base calendars).
 *
 * <strong>Conversation protocol:</strong>
 * <ul>
 * <li>
 * Initial conversation steps: <code>initCreate</code> (to create a
 * {@link BaseCalendar}) or (exclusive) <code>initEdit</code> (to edit an
 * existing {@link BaseCalendar}).</li>
 * <li>
 * Intermediate conversation steps: <code>getBaseCalendar</code> (to return the
 * {@link BaseCalendar} being edited/created).</li>
 * <li>
 * Final conversational steps: <code>confirmSave</code> (to save the
 * {@link BaseCalendar} being edited/created), <code>confirmRemove</code> (to
 * remove the {@link BaseCalendarModel}) or (exclusive) <code>cancel</code> (to
 * discard changes).</li>
 * </ul>
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IBaseCalendarModel {


    /*
     * Non conversational steps
     */

    List<BaseCalendar> getBaseCalendars();


    /*
     * Initial conversation steps
     */

    void initCreate();

    void initEdit(BaseCalendar baseCalendar);

    void initRemove(BaseCalendar baseCalendar);

    void initCreateDerived(BaseCalendar baseCalendar);

    void initCreateCopy(BaseCalendar baseCalendar);

    /*
     * Intermediate conversation steps
     */

    BaseCalendar getBaseCalendar();

    boolean isEditing();

    void setSelectedDay(Date date);

    Date getSelectedDay();

    DayType getTypeOfDay();

    DayType getTypeOfDay(LocalDate date);

    Integer getHoursOfDay();

    void createException(Integer hours);

    Integer getHours(Days day);

    void setHours(Days day, Integer hours);

    Boolean isDefault(Days day);

    void setDefault(Days day);

    void unsetDefault(Days day);

    boolean isExceptional();

    void removeException();

    boolean isDerived();

    List<BaseCalendar> getPossibleParentCalendars();

    BaseCalendar getParent();

    void setParent(BaseCalendar parent);

    boolean isParent();

    Date getExpiringDate();

    void setExpiringDate(Date date);

    Date getDateValidFrom();

    void setDateValidFrom(Date date);

    List<CalendarData> getHistoryVersions();

    void createNewVersion(Date date);

    boolean isLastVersion();

    String getName();

    LocalDate getValidFrom(CalendarData calendarData);

    /*
     * Final conversation steps
     */

    void confirmSave() throws ValidationException;

    void confirmRemove();

    void cancel();

}
