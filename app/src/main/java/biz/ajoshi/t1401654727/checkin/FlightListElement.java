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

    public FlightListElement(Cursor c, int idIndex, int timeStampIndex, int fNameIndex,
                             int lNameIndex, int hasCheckedInIndex, int originIndex,
                             int destinationIndex, int displayTimeIndex, int gateIndex,
                             int positionIndex, int confCodeIndex, int attemptsIndex) {
        this.id = c.getString(idIndex);
        this.timeStamp = c.getLong(timeStampIndex);
        this.fName = c.getString(fNameIndex);
        this.lName = c.getString(lNameIndex);
        this.confCode = c.getString(confCodeIndex);
        this.hasCheckedIn = c.getInt(hasCheckedInIndex) == 1;
        this.origin = c.getString(originIndex);
        this.destination = c.getString(destinationIndex);
        this.displayTime = c.getString(displayTimeIndex);
        this.attempts = c.getInt(attemptsIndex);
        this.gate = c.getString(gateIndex);
        this.position = c.getString(positionIndex);
    }
}
