package biz.ajoshi.t1401654727.checkin;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Basic model of an element in a Flight list
 */
public class FlightListElement implements Parcelable {
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
     *
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

    protected FlightListElement(Parcel in) {
        id = in.readString();
        timeStamp = in.readLong();
        fName = in.readString();
        lName = in.readString();
        confCode = in.readString();
        hasCheckedIn = in.readByte() != 0;
        origin = in.readString();
        destination = in.readString();
        displayTime = in.readString();
        attempts = in.readInt();
        gate = in.readString();
        position = in.readString();
    }

    public static final Creator<FlightListElement> CREATOR = new Creator<FlightListElement>() {
        @Override
        public FlightListElement createFromParcel(Parcel in) {
            return new FlightListElement(in);
        }

        @Override
        public FlightListElement[] newArray(int size) {
            return new FlightListElement[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeLong(timeStamp);
        dest.writeString(fName);
        dest.writeString(lName);
        dest.writeString(confCode);
        dest.writeByte((byte) (hasCheckedIn ? 1 : 0));
        dest.writeString(origin);
        dest.writeString(destination);
        dest.writeString(displayTime);
        dest.writeInt(attempts);
        dest.writeString(gate);
        dest.writeString(position);
    }
}
