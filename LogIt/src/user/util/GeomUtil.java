package user.util;

import java.text.DecimalFormat;
/**
 * Geometric utilities. Decimal to sexagesimal conversions. Contains
 * only static methods.
 *
 * @author olivier@lediouris.net
 */
public class GeomUtil
{
  /**
   * Converts Sexagesimal to decimal. Return a double.
   * @param degrees the value of the degrees. It's an int
   * @param minutes the value of the minutes. Can be decimal, like 45.67
   */
  public static double SexToDec(String degrees, String minutes) throws RuntimeException
  {
    double deg = 0.0;
    double min = 0.0;

    double ret = 0.0;

    try
    {
      deg = Double.parseDouble(degrees);
      min = Double.parseDouble(minutes);
      min *= (10.0/6.0);
      ret = (deg + (min/100.0));
    }
    catch (NumberFormatException nfe)
    {
      throw new RuntimeException("Bad number");
    }
    return ret;
  }

  public final static int HTML  = 0;
  public final static int SHELL = 1;
  public final static int SWING = 2;

  /**
   * Converts decimal to sexagesimmal. Return an ASCII string
   * @param v the double value to convert.
   */
  public static String DecToSex(double v)
  {
    return DecToSex(v, SHELL);  
  }
  
  public static String DecToSex(double v, int output)
  {
    String s = "";
    double intValue = Math.rint(v);  // floor should be more appropriate
    if (intValue > v)
      intValue -= 1.0;
    double dec = v - intValue;

    int i = (int)intValue;

    dec *= (100 * (6.0/10.0));

    DecimalFormat df = new DecimalFormat("00.00");
    if (output == HTML)
      s = Integer.toString(i) + "&deg;" + df.format(dec) + "'";
    else if (output == SWING)
      s = Integer.toString(i) + '°' + df.format(dec) + "'";
    else
      s = Integer.toString(i) + (char)248 + df.format(dec) + "'";
      
    return s;
  }
  
  public static String Angle2Hour(double angle)
  {
    String hValue = "";
    DecimalFormat nf = new DecimalFormat("00");
    
    double deg = angle;
    // Noon    : 0
    // Midnight: 180
    // 6am:    : 270
    // 6pm     : 90
    deg += 180;
    while (deg < 0.0)
      deg += 360.0;
    while (deg > 360.0)
      deg -= 360.0;
      
    int nbMinArc = (int)Math.rint(deg * 60.0);

    int nbH = nbMinArc / (60 * 15);
    nbMinArc -= (nbH * (60 * 15));
    double dnbM = ((double)(4.0 * nbMinArc / 60.0));
    int nbM = (int)dnbM;
    int nbS = (int)((dnbM - (double)nbM) * 60.0);
    hValue = nf.format(nbH) + ":" + nf.format(nbM) + ":" + nf.format(nbS);
    
    return hValue;
  }
}

