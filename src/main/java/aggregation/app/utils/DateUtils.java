package aggregation.app.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils
{
    public static Date getRoundedHourDate(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
