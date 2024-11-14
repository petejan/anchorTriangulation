package astro.calc;

/**
 * A Point
 * 
 * @author Olivier LE DIOURIS
 */
public class Point
{
  double latitude;
  double longitude;
  /**
   * Constructor
   */
  public Point(double l, double g)
  {
    this.latitude  = l;
    this.longitude = g;
  }

  public double getL()
  { return this.latitude; }

  public double getG()
  { return this.longitude; }

  public void setL(double l)
  { this.latitude = l; }

  public void setG(double g)
  { this.longitude = g; }
}

 