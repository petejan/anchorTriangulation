package user.util;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil 
{
  /**
   * Computes the current GMT after the System Time
   * @return current GMT
   */
  public static Date getGMT()
  {
    Date now = new Date();
    Date gmt = null;

    String tzOffset = (new SimpleDateFormat("Z")).format(now);
    int offset = 0;
    try { offset = Integer.parseInt(tzOffset); }
    catch (NumberFormatException nfe)
    { nfe.printStackTrace(); }
    if (offset != 0)
    {
      long value = offset / 100;
      long longDate = now.getTime();
      longDate -= (value * 3600000);
      gmt = new Date(longDate);
    }
    else
      gmt = now;
    return gmt;
  }
}