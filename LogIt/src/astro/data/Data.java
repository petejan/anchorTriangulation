package astro.data;
import java.text.DecimalFormat;

public class Data 
{
  private final static DecimalFormat df2  = new DecimalFormat("00");
  private final static DecimalFormat df3  = new DecimalFormat("000");
  private final static DecimalFormat df03 = new DecimalFormat("#0.000");
  private final static DecimalFormat df21 = new DecimalFormat("00.0");
  private final static DecimalFormat df22 = new DecimalFormat("00.00");
  private final static DecimalFormat df23 = new DecimalFormat("00.000");
  private final static DecimalFormat df01 = new DecimalFormat("#0.0");    
  private final static DecimalFormat df06 = new DecimalFormat("#0.000000");    
  
  /**
   * Angle output in Degrees, Minutes, and Seconds
   */
  public final static int DMS = 0; // Degrees, Minutes, Seconds
  /**
   * Angle output in Degrees, Minutes, and 100th of minutes
   */
  public final static int DMD = 1; // Degrees, Minutes, Decimal Minutes
  
  private static int minuteDisplay = DMS;
  
  /**
   * Output the latitude of the Geographical Position as Hour Angles
   */
  public final static int HA = 0; // Hour Angle
  /**
   * Output the latitude of the Geographical Position as Right Ascension
   */
  public final static int RA = 1; // Right Ascencion
  
  private static int HorizontalDisplay = HA;
  
  /**
   * GHA of Aries (vernal Point)
   */
  double AR;
  
  /**
   * Polaris GHA
   */
  double GHApolaris;
  /**
   * Polaris SHA
   */
  double SHApolaris;
  /**
   * Polaris Declination
   */
  double DECpolaris;
  
  /**
   * Obliquity of Ecliptic
   */
  double OoE;
  /**
   * true Obliquity of Ecliptic
   */
  double tOoE;
  
  /**
   * Equation of Time
   */
  double EOT;

  /** 
   * Moon illumination
   */
  double illum; // k
  /**
   * Moon Phase
   */
  String quarter = "";
  
  /**
   * Lunar Distance of Sun
   */
  double LDist;

  /**
   * Sun GHA
   */
  double GHAsun;
  /**
   * Sun SHA
   */
  double SHAsun;
  /**
   * Sun Declination
   */
  double DECsun;
  /**
   * Sun semi-diameter
   */
  double SDsun;
  /**
   * Sun horizontal parallax
   */
  double HPsun;

  /**
   * Moon GHA
   */
  double GHAmoon;
  /**
   * Moon SHA
   */
  double SHAmoon;
  /**
   * Moon Declination
   */
  double DECmoon;
  /**
   * Moon semni-diameter
   */
  double SDmoon;
  /**
   * Moon horizontal parallax
   */
  double HPmoon;

  /**
   * Aries true GHA
   */
  double GHAAtrue;
  /**
   * Aries mean GHA
   */
  double GHAAmean;
  /**
   * Equation of Ecliptic
   */
  double EoE;
  
  double delta_psi;
  double delta_eps;
  /**
   * Julian Day
   */
  double JD;
  double JDE;
  /**
   * Day of Week
   */
  String DoW;

//private final static char DEG_SYMBOL = (char)248;
  private final static char DEG_SYMBOL = (char)'°';
  
  public Data()
  {
  }

  /**
   * Format a Hour Angle
   * in degrees, minutes, and seconds.
   */
  public static String formatHA(double x)
  {
    if (HorizontalDisplay == Data.HA)
    {
      double GHAdeg = Math.floor(x);
      double GHAmin = 0.0;
      if (minuteDisplay == Data.DMS)
        GHAmin = Math.floor(60*(x-GHAdeg));	
      else
        GHAmin = 60*(x-GHAdeg);	
      
      double GHAsec = Math.round(3600*(x-GHAdeg-GHAmin/60));
      if (GHAsec==60) 
      { 
        GHAsec=0; 
        GHAmin+=1;
      }
      if (GHAmin==60) 
      {
        GHAmin=0; 
        GHAdeg+=1;
      }
      if (minuteDisplay == Data.DMS)
        return df3.format(GHAdeg)+DEG_SYMBOL+df2.format(GHAmin)+"'"+df2.format(GHAsec)+"\"";
      else
        return df3.format(GHAdeg)+DEG_SYMBOL+df22.format(GHAmin)+"'";
    }
    else
      return formatRA(x);
  }

  public static String formatRA(double x)
  {
    double RA = 360.0 - x;
    double RAhours = Math.floor(RA/15.0);
    double remainder = RA-(15 * RAhours);
    double RAmin = Math.floor(4*remainder);	
    double RAsec = Math.round(4*(remainder-(RAmin/15)));
    if (RAsec==60) 
    { 
      RAsec=0; 
      RAmin+=1;
    }
    if (RAmin==60) 
    {
      RAmin=0; 
      RAhours+=1;
    }
    return df2.format(RAhours)+"h "+df2.format(RAmin)+"m "+df2.format(RAsec)+"s";
  }

  /**
   * Format Mean Obliquity of Ecliptic
   * in degrees, minutes, and seconds (2.3s).
   */
  public static String formatECL(double x)
  {
    double ECLdeg = Math.floor(x);
    double ECLmin = Math.floor(60*(x-ECLdeg));	
    double ECLsec = 3600*(x-ECLdeg-ECLmin/60);
    if (ECLsec==60) 
    {
      ECLsec=0; 
      ECLmin+=1;
    }
    if (ECLmin==60) 
    {
      ECLmin=0; 
      ECLdeg+=1;
    }
    return df2.format(ECLdeg)+DEG_SYMBOL+df2.format(ECLmin)+"'"+df23.format(ECLsec)+"\"";
  }

  /**
   * Format Sideral Time
   * in hours, minutes, and seconds (2.3s).
   */
  public static String formatSidTime(double x)
  {
    double GMSTdecimal = x/15;
    double GMSTh = Math.floor(GMSTdecimal);
    double GMSTmdecimal = 60*(GMSTdecimal-GMSTh);
    double GMSTm = Math.floor(GMSTmdecimal);
    double GMSTsdecimal = 60*(GMSTmdecimal-GMSTm);
    double GMSTs = GMSTsdecimal;

    return df2.format(GMSTh)+"h"+df2.format(GMSTm)+"m"+df23.format(GMSTs)+"s";
  }

  /**
   * Format Declination
   * in degrees, minutes, and seconds.
   */
  public static String formatDec(double x)
  {
    int signDEC = 0;
    String name = "";
    if (x<0)  
    {
      signDEC=-1; 
      name="S";
    }
    else  
    {
      signDEC=1; 
      name="N";
    }
    double DEC = Math.abs(x);
    double DECdeg = Math.floor(DEC);
    double DECmin = 0.0;
    if (minuteDisplay == Data.DMS)
      DECmin = Math.floor(60*(DEC-DECdeg));	
    else
      DECmin = 60*(DEC-DECdeg);
    double DECsec = Math.round(3600*(DEC-DECdeg-DECmin/60));
    if (DECsec==60) 
    {
      DECsec=0; 
      DECmin+=1;
    }
    if (DECmin==60) 
    {
      DECmin=0; 
      DECdeg+=1;
    }
    if (minuteDisplay == Data.DMS)
      return name+" "+df2.format(DECdeg)+DEG_SYMBOL+df2.format(DECmin)+"'"+df2.format(DECsec)+"\"";
    else
      return name+" "+df2.format(DECdeg)+DEG_SYMBOL+df22.format(DECmin)+"'";
  }

  /**
   * Format semi-diameter and horizontal parallax
   * in minutes
   */
  public static String formatSDHP(double x)
  {
 // return df01.format(x)+"\""; // in seconds
    return df01.format(x/60.0)+"'"; // in minutes
  }

  /**
   * Format Equation of Time
   * in minutes and seconds (2.1s)
   */
  public static String formatEoT(double x)
  {
    double EoT = 0.0;
    String ret = "";
    String sign = "";
    if (x<0) 
      sign="-";
    else 
      sign="+";
    EoT = Math.abs(x);	
    double EOTmin = Math.floor(EoT);
    double EOTsec = 60*(EoT-EOTmin);
    ret = sign + df2.format(EOTmin)+"m "+df21.format(EOTsec)+"s";    
    return ret;
  }

  /**
   * Format Equation of Ecliptic
   * in deconds (*.3s)
   */
  public static String formatEoE(double x)
  {
    return df03.format(x) + "s";
  }

  /**
   * Format deltas
   * in seconds
   */
  public static String formatDelta(double x)
  {
    return df03.format(3600*x)+"\"";
  }
  
  /**
   * Format Julian numbers 
   * with 6 decimal positions
   */
  public static String formatJulian(double x)
  {
    return df06.format(x);
  }

  /**
   * Format Moon illumination.
   * Illumination and moon phase
   */
  public static String formatIllum(double x, String s)
  {
    return df01.format(x)+"% "+s;
  }
/*******************************************/

  public double getDECsun()
  {
    return DECsun;
  }

  public void setDECsun(double newDECsun)
  {
    DECsun = newDECsun;
  }

  public double getDECmoon()
  {
    return DECmoon;
  }

  public void setDECmoon(double newDECmoon)
  {
    DECmoon = newDECmoon;
  }

  public double getAR()
  {
    return AR;
  }

  public void setAR(double newAR)
  {
    AR = newAR;
  }

  public double getDECpolaris()
  {
    return DECpolaris;
  }

  public void setDECpolaris(double newDECpolaris)
  {
    DECpolaris = newDECpolaris;
  }

  public String getDoW()
  {
    return DoW;
  }

  public void setDoW(String newDoW)
  {
    DoW = newDoW;
  }

  public double getEOT()
  {
    return EOT;
  }

  public void setEOT(double newEOT)
  {
    EOT = newEOT;
  }

  public double getEoE()
  {
    return EoE;
  }

  public void setEoE(double newEoE)
  {
    EoE = newEoE;
  }

  public double getGHAmoon()
  {
    return GHAmoon;
  }

  public void setGHAmoon(double newGHAmoon)
  {
    GHAmoon = newGHAmoon;
  }

  public double getGHApolaris()
  {
    return GHApolaris;
  }

  public void setGHApolaris(double newGHApolaris)
  {
    GHApolaris = newGHApolaris;
  }

  public double getGHAsun()
  {
    return GHAsun;
  }

  public void setGHAsun(double newGHAsun)
  {
    GHAsun = newGHAsun;
  }

  public double getHPmoon()
  {
    return HPmoon;
  }

  public void setHPmoon(double newHPmoon)
  {
    HPmoon = newHPmoon;
  }

  public double getHPsun()
  {
    return HPsun;
  }

  public void setHPsun(double newHPsun)
  {
    HPsun = newHPsun;
  }

  public double getJD()
  {
    return JD;
  }

  public void setJD(double newJD)
  {
    JD = newJD;
  }

  public double getJDE()
  {
    return JDE;
  }

  public void setJDE(double newJDE)
  {
    JDE = newJDE;
  }

  public double getLDist()
  {
    return LDist;
  }

  public void setLDist(double newLDist)
  {
    LDist = newLDist;
  }

  public double getOoE()
  {
    return OoE;
  }

  public void setOoE(double newOoE)
  {
    OoE = newOoE;
  }

  public double getSDmoon()
  {
    return SDmoon;
  }

  public void setSDmoon(double newSDmoon)
  {
    SDmoon = newSDmoon;
  }

  public double getSDsun()
  {
    return SDsun;
  }

  public void setSDsun(double newSDsun)
  {
    SDsun = newSDsun;
  }

  public double getSHAmoon()
  {
    return SHAmoon;
  }

  public void setSHAmoon(double newSHAmoon)
  {
    SHAmoon = newSHAmoon;
  }

  public double getSHApolaris()
  {
    return SHApolaris;
  }

  public void setSHApolaris(double newSHApolaris)
  {
    SHApolaris = newSHApolaris;
  }

  public double getSHAsun()
  {
    return SHAsun;
  }

  public void setSHAsun(double newSHAsun)
  {
    SHAsun = newSHAsun;
  }

  public double getGHAAmean()
  {
    return GHAAmean;
  }

  public void setGHAAmean(double newGHAAmean)
  {
    GHAAmean = newGHAAmean;
  }

  public double getGHAAtrue()
  {
    return GHAAtrue;
  }

  public void setGHAAtrue(double newGHAAtrue)
  {
    GHAAtrue = newGHAAtrue;
  }

  public double getDelta_eps()
  {
    return delta_eps;
  }

  public void setDelta_eps(double newDelta_eps)
  {
    delta_eps = newDelta_eps;
  }

  public double getDelta_psi()
  {
    return delta_psi;
  }

  public void setDelta_psi(double newDelta_psi)
  {
    delta_psi = newDelta_psi;
  }

  public double getIllum()
  {
    return illum;
  }

  public void setIllum(double newIllum)
  {
    illum = newIllum;
  }

  public double getTOoE()
  {
    return tOoE;
  }

  public void setTOoE(double newTOoE)
  {
    tOoE = newTOoE;
  }

  public String getQuarter()
  {
    return quarter;
  }

  public void setQuarter(String newQuarter)
  {
    quarter = newQuarter;
  }

  public void setMinuteDisplay(int newMinuteDisplay)
  {
    minuteDisplay = newMinuteDisplay;
  }

  public void setHorizontalDisplay(int newHorizontalDisplay)
  {
    HorizontalDisplay = newHorizontalDisplay;
  }
}