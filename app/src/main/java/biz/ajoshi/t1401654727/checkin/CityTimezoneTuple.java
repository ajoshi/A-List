package biz.ajoshi.t1401654727.checkin;

/**
 * This object holds city name and a timezone so both are returned by the adapter, but I override
 * toString so the adapter shows the city name.
 */
public class CityTimezoneTuple {
    public String city;
    public String tz;
    public CityTimezoneTuple(String cit, String timeZone) {
        this.city = cit;
        tz = timeZone;
    }

    public String toString() {
        return city;
    }
}
