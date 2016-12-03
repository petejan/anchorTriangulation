package ocss.nmea.parser;

import java.io.Serializable;

import java.util.Date;

public class RMC implements Serializable
{
  private GeoPos gp = null;
  private double sog = 0D;
  private double cog = 0D;
  
  private Date rmcDate = null;
  private double declination = -Double.MAX_VALUE;

  public RMC()
  {
  }

  public void setGp(GeoPos gp)
  {
    this.gp = gp;
  }

  public GeoPos getGp()
  {
    return gp;
  }

  public void setSog(double sog)
  {
    this.sog = sog;
  }

  public double getSog()
  {
    return sog;
  }

  public void setCog(double cog)
  {
    this.cog = cog;
  }

  public double getCog()
  {
    return cog;
  }
  
  public String toString()
  {
    String str = "";
    str = gp.toString() + ", " + "SOG:" + sog + ", COG:" + cog;
    if (rmcDate != null)
      str += (" " + rmcDate.toString() + " ");
    if (declination != -Double.MAX_VALUE)
      str += ("D:" + Double.toString(declination));
    
    return str;
  }

  public void setRmcDate(Date rmcDate)
  {
    this.rmcDate = rmcDate;
  }

  public Date getRmcDate()
  {
    return rmcDate;
  }

  public void setDeclination(double declination)
  {
    this.declination = declination;
  }

  public double getDeclination()
  {
    return declination;
  }
}
