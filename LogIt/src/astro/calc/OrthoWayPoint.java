package astro.calc;

/**
 * A Point and its azimuth to the next one
 *
 * @author Olivier LE DIOURIS
 */
public class OrthoWayPoint
{
  Point p;
  Double z;
  /**
   * Constructor
   */
  public OrthoWayPoint(Point p, Double z)
  {
    this.p = p;
    this.z = z;
  }
   public Point getPoint()
   { return this.p; }
   public Double getZ()
   { return this.z; }
}

