package biz.ajoshi.t1401654727.checkin;

import android.database.Cursor;

/**
 * Basic model of an element in a Flight list
 */
public class FlightListElement {
    public String id;
    public long timeStamp;
    public String fName;
    public String lName;
    public String confCode;
    public boolean hasCheckedIn;
    public String origin;
    public String destination;
    public String displayTime;
    public int attempts;
    public String gate;
    public String position;

    /**
     * Constructs a FlightListElement from a cursor. Indices for each field must be sent in, but
     * any field except for id can be skipped by sending in a column index of -1
     * @param c
     * @param idIndex
     * @param timeStampIndex
     * @param fNameIndex
     * @param lNameIndex
     * @param hasCheckedInIndex
     * @param originIndex
     * @param destinationIndex
     * @param displayTimeIndex
     * @param gateIndex
     * @param positionIndex
     * @param confCodeIndex
     * @param attemptsIndex
     */
    public FlightListElement(Cursor c, int idIndex, int timeStampIndex, int fNameIndex,
                             int lNameIndex, int hasCheckedInIndex, int originIndex,
                             int destinationIndex, int displayTimeIndex, int gateIndex,
                             int positionIndex, int confCodeIndex, int attemptsIndex) {
        this.id = c.getString(idIndex);
        if (timeStampIndex != -1) {
            this.timeStamp = c.getLong(timeStampIndex);
        }
        if (fNameIndex != -1) {
            this.fName = c.getString(fNameIndex);
        }
        if (lNameIndex != -1) {
            this.lName = c.getString(lNameIndex);
        }
        if (confCodeIndex != -1) {
            this.confCode = c.getString(confCodeIndex);
        }
        if (hasCheckedInIndex != -1) {
            this.hasCheckedIn = c.getInt(hasCheckedInIndex) == 1;
        }
        if (originIndex != -1) {
            this.origin = c.getString(originIndex);
        }
        if (destinationIndex != -1) {
            this.destination = c.getString(destinationIndex);
        }
        if (displayTimeIndex != -1) {
            this.displayTime = c.getString(displayTimeIndex);
        }
        if (attemptsIndex != -1) {
            this.attempts = c.getInt(attemptsIndex);
        }
        if (gateIndex != -1) {
            this.gate = c.getString(gateIndex);
        }
        if (positionIndex != -1) {
            this.position = c.getString(positionIndex);
        }
    }
}
