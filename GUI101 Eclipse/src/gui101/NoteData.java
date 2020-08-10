package gui101;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class NoteData {
    private String strSample, strID;
    private Long lnDate;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * Initializes Note data with default values for each attribute.
     * strSample - defaults to "New Note"
     * lnDate - defaults to the current time the object was instantiated (in millis)
     * strID - defaults to a unique ID generated by the UUID class
     */
    public NoteData() {
        this.setStrSample("New Note");
        this.setStrID(UUID.randomUUID().toString());
        this.setDate(System.currentTimeMillis());
    }

    /**
     * Initializes Note data with a default value for strID and user defined values for the rest.
     * @param date - the date to assign the note (in millis)
     * @param sample - the string sample/note title to display in the list cell.
     */
    public NoteData(Long date, String sample) {
        this.setStrSample(sample);
        this.setDate(date);
    }

    /**
     * @return the constant date format display for the list cell
     */
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * @return the date assigned to the object (in millis)
     */
    public Long getDate() {
        return lnDate;
    }

    /**
     * @return the unique ID assigned to the object
     */
    public String getStrID() {
        return strID;
    }

    /**
     * @return the string sample/note title assigned to the object
     */
    public String getStrSample() {
        return strSample;
    }

    /**
     * Sets the date the note was modified/created
     * @param lnDate - the date (in millis)
     */
    public void setDate(Long lnDate) {
        this.lnDate = lnDate;
    }

    /**
     * Sets the unique ID for the note
     * @param strID - the unique ID
     */
    public void setStrID(String strID) {
        this.strID = strID;
    }

    /**
     * Sets the string sample/note title
     * @param strSample - the note title
     */
    public void setStrSample(String strSample) {
        this.strSample = strSample;
    }

}
