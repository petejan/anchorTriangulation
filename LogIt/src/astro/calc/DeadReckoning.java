package astro.calc;

/**
 * Astronomical Navigation Tools.
 * <br>
 * Dead Reckoning : Estimated Altitude et Azimuth.
 * <br>
 * This is a Java Bean.
 * <br>
 * Input parameters :
 * <ul>
 *   <li type="disc">GHA - Greenwich Hour Angle</li>
 *   <li type="disc">Declination</li>
 *   <li type="disc">Estimated Latitude</li>
 *   <li type="disc">Estimated Longitude</li>
 * </ul>
 * Output data :
 * <ul>
 *   <li type="disc">Estimated Altitude</li>
 *   <li type="disc">Azimuth</li>
 * </ul>
 * Test with :
 * <ul>
 *   <li>GHA = 321&deg;57.9</li>
 *   <li>D = 13&deg;57.5 N</li>
 *   <li>L = 46&deg;38 N</li>
 *   <li>G  = 4&deg;06 W</li>
 * </ul>
 * Result should be
 * <ul>
 *   <li type="disc">Ea 42 02</li>
 *   <li type="disc">Z 119</li>
 * </ul>
 * This call only performs calculations. No user interface is provided.
 * 
 * @author olivier@lediouris.net
 * @version 1.0.0
 */
public class DeadReckoning
{
  private Double dHe = null;
  private Double dZ  = null;

  private double AHG;
  private double D;
  private double L;
  private double G;
  /**
   * Constructor.
   * Call it and then use getHe() and getZ() to retreive result.<br>
   * Call after this one setAHG(), setD(), setL() and setG()
   */
  public DeadReckoning()
  {
  }

  /**
   * Constructor.
   * Call it and then use getHe() and getZ() to retrieve result.
   * @param dAHG Greenwich Hour Angle
   * @param dD Declination
   * @param dL Estimated Latitude
   * @param dG Estimated Longitude
   */
  public DeadReckoning(double dAHG,
                       double dD,
                       double dL,
                       double dG)
  {
    this.AHG = dAHG;
    this.D   = dD;
    this.L   = dL;
    this.G   = dG;
  }

  /**
   * Performs the required calculations, after the AHG, D, L and G.
   * he and Z are after that ready to be retrieved.
   */
  public void calculate()
  {
    double AHL = this.AHG + this.G;
    if (AHL < 0.0)
      AHL = 360.0 + AHL;
    // Formula to solve : sin He = sin L sin D + cos L cos D cos AHL
    double sinL   = Math.sin(this.L * (Math.PI/180.0));
    double sinD   = Math.sin(this.D * (Math.PI/180.0));
    double cosL   = Math.cos(this.L * (Math.PI/180.0));
    double cosD   = Math.cos(this.D * (Math.PI/180.0));
    double cosAHL = Math.cos(AHL    * (Math.PI/180.0));

    double sinHe = (sinL * sinD) + (cosL * cosD * cosAHL);
    double He    = Math.asin(sinHe) * (180.0/Math.PI);
//  System.out.println("Hauteur Estimee : " + GeomUtil.DecToSex(He));
    dHe = new Double(He);

    // Formula to solve : tg Z = sin P / cos L tan D - sin L cos P
    double P = (AHL < 180.0) ? AHL : (360.0 - AHL);
    double sinP = Math.sin(P * (Math.PI/180.0));
    double cosP = Math.cos(P * (Math.PI/180.0));
    double tanD = Math.tan(this.D * (Math.PI/180.0));
    double tanZ = sinP / ((cosL * tanD) - (sinL * cosP));
    double Z = Math.atan(tanZ) * (180.0/Math.PI);

    if (AHL < 180.0) // vers l'West
    {
      if (Z < 0.0) // sud vers nord
        Z = 180.0 - Z;
      else         // Nord vers Sud
        Z = 360.0 - Z;
    }
    else             // vers l'Est
    {
      if (Z < 0.0) // sud vers nord
        Z = 180.0 + Z;
      else         // nord vers sud
        Z = Z;
    }
//  System.out.println("Azimut : " + GeomUtil.DecToSex(Z));
    dZ = new Double(Z);
  }
  /**
   * Returns Hauteur estim&eacute;e after calculation.
   * This value is decimal. Use GeomUtil.DecToSex(getHe()) to read it in DegMinSec.
   * @see user.util.GeomUtil
   */
  public Double getHe()
  { return dHe; }

  /**
   * Returns Azimuth after calculation.
   * This value is decimal. Use GeomUtil.DecToSex(getZ()) to read it in DegMinSec.
   * @see user.util.GeomUtil
   */
  public Double getZ()
  { return dZ; }

  /**
   * Set the AHG before calculation
   * @param ahg the AHG to set
   */
  public void setAHG(double ahg)
  { this.AHG = ahg; }

  /**
   * Set the D before calculation
   * @param d the D to set
   */
  public void setD(double d)
  { this.D = d; }

  /**
   * Set the L before calculation
   * @param the L to set
   */
  public void setL(double l)
  { this.L = l; }

  /**
   * Set the G before calculation
   * @param g the G to set
   */
  public void setG(double g)
  { this.G = g; }
  
  /**
   * Corrections
   */
  double horizonDip = 0.0;
  double refr = 0.0;
  double pa = 0.0;
  
  public final static int UPPER_LIMB = 0;
  public final static int LOWER_LIMB = 1;
  
  public double getHorizonDip()
  { return horizonDip; }
  public double getRefr()
  { return refr; }
  public double getPa()
  { return pa; }
  
  /**
   * Returns the corrected Altitude of a celestial body
   * <br>
   * We left in stand by for now:
   * <ul>
   *   <li type="disc">Oblate Spheroid (Earth is not a sphere)</li>
   *   <li type="disc">Barometric Correction</li>
   * </ul> 
   * 
   * @param calculatedAltitude The one you want to correct
   * @param eyeHeight Height of the eye above water, in meters
   * @param hp Horizontal parallax, in minutes
   * @param sd Semi diameter of the celestial body, in minutes
   * @param limb Upper or Lower limb 
   * 
   * @see DeadReckoning#UPPER_LIMB
   * @see DeadReckoning#LOWER_LIMB
   * 
   * @return the Corrected Altitude
   */
  public double correctedAltitude(double calculatedAltitude,
                                  double eyeHeight, // meters
                                  double hp,
                                  double sd,
                                  int limb)
  {
    double correction = 0.0;
    // Dip of horizon, in minutes
    horizonDip = 1.76 * Math.sqrt(eyeHeight);
    correction -= (horizonDip/60.0);
    double correctedAltitude = calculatedAltitude + correction;
    // Refraction
    refr = 0.97127 * Math.tan((Math.PI/180.0) * (90.0 - correctedAltitude)) -
           0.00137 * Math.pow(Math.tan((Math.PI/180.0) * (90.0 - correctedAltitude)) , 3.0);
    correction -= (refr/60.0);
    correctedAltitude = calculatedAltitude + correction;
    // Barometric correction - stby for now
    
    // Parallax 
    double pa = 0.0; // Parallax in altitude
    pa = Math.asin(Math.sin((Math.PI/180.0) * (hp / 60.0)) *
                   Math.cos((Math.PI/180.0) * correctedAltitude));
    // Earth is not a sphere...
    double ob = 0.0; // Oblate Spheroid
    /* Stby */
    
    pa += ob;
    correction += (pa * (180.0/Math.PI));
    correctedAltitude = calculatedAltitude + correction;
    // Semi diameter
    if (limb == LOWER_LIMB)
      correction += (sd / 60.0); // Lower Limb;
    else if (limb == UPPER_LIMB)
      correction -= (sd / 60.0); // Upper Limb;

    correctedAltitude = calculatedAltitude + correction;
    
    return correctedAltitude;
  } 
}

