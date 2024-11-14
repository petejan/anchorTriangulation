package astro.runtime.ui;
import astro.calc.DeadReckoning;
import astro.data.Data;
import astro.data.ephemerides.CelestialComputer;
import astro.runtime.ui.panels.DatePanel;
import astro.runtime.ui.panels.InputPanel;
import astro.runtime.ui.panels.OutputPanel;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Dimension;
import user.util.GeomUtil;
import user.util.TimeUtil;

public class AppFrame extends JFrame 
{
  private JLabel statusBar = new JLabel();
  private JPanel panelCenter = new JPanel();
  private BorderLayout layoutMain = new BorderLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private DatePanel datePanel = new DatePanel(this);
  private InputPanel inputPanel = new InputPanel(this);
  private OutputPanel outputPanel = new OutputPanel(this);

  public AppFrame()
  {
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.getContentPane().setLayout(layoutMain);
    panelCenter.setLayout(borderLayout1);
    this.setSize(new Dimension(550, 480));
    this.setTitle("Real Time Dead Reckoning");
    statusBar.setText("");
    this.getContentPane().add(statusBar, BorderLayout.NORTH);
    panelCenter.add(datePanel, BorderLayout.NORTH);
    panelCenter.add(inputPanel, BorderLayout.CENTER);
    this.getContentPane().add(panelCenter, BorderLayout.CENTER);
    this.getContentPane().add(outputPanel, BorderLayout.SOUTH);
  }
  
  Thread background = null;
  boolean vazymapoule = false;
  
  public void go()
  {
    vazymapoule = true;
    if (background == null)
    {
      background = new Thread()
        {
          public void run()
          {
            while (vazymapoule)
            {
              setOutput();
              try { Thread.sleep(1000); }
              catch (Exception ignore) {}
            }
          }
        };
    }
    background.start();
  }
  
  public void stop()
  {
    vazymapoule = false;
  }
  
  public void setOutput()
  {
    Date ut = TimeUtil.getGMT();
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    String str = " " + sdf.format(ut) + " UT";
    datePanel.setText(str);
    CelestialComputer cc = new CelestialComputer(ut);
    Data data = cc.getData();
    data.setMinuteDisplay(Data.DMD);
    
    outputPanel.setAriesGHA(Data.formatHA(data.getAR()));
    
    outputPanel.setSunGHA(Data.formatHA(data.getGHAsun()));
    outputPanel.setSunDec(Data.formatDec(data.getDECsun()));
    outputPanel.setSunHP(Data.formatSDHP(data.getHPsun()));
    outputPanel.setSunSD(Data.formatSDHP(data.getSDsun()));
    
    outputPanel.setMoonGHA(Data.formatHA(data.getGHAmoon()));
    outputPanel.setMoonDec(Data.formatDec(data.getDECmoon()));
    outputPanel.setMoonHP(Data.formatSDHP(data.getHPmoon()));
    outputPanel.setMoonSD(Data.formatSDHP(data.getSDmoon()));
    
    outputPanel.setPolarisGHA(Data.formatHA(data.getGHApolaris()));
    outputPanel.setPolarisDec(Data.formatDec(data.getDECpolaris()));
    
    DeadReckoning estime = new DeadReckoning(data.getGHAsun(), 
                                             data.getDECsun(), 
                                             inputPanel.getL(), 
                                             inputPanel.getG());
    estime.calculate();
    double sunCa = estime.correctedAltitude(estime.getHe().doubleValue(),
                                            inputPanel.getEyeHeight(),
                                            data.getHPsun() / 60.0,
                                            data.getSDsun() / 60.0,
                                            DeadReckoning.LOWER_LIMB);
    outputPanel.setEaSun(GeomUtil.DecToSex(estime.getHe().doubleValue(), GeomUtil.SWING));
    outputPanel.setZSun(GeomUtil.DecToSex(estime.getZ().doubleValue(), GeomUtil.SWING));
    outputPanel.setCaSun(GeomUtil.DecToSex(sunCa, GeomUtil.SWING));
    
    estime = new DeadReckoning(data.getGHAmoon(), 
                               data.getDECmoon(), 
                               inputPanel.getL(), 
                               inputPanel.getG());
    estime.calculate();
    double moonCa = estime.correctedAltitude(estime.getHe().doubleValue(),
                                             inputPanel.getEyeHeight(),
                                             data.getHPmoon() / 60.0,
                                             data.getSDmoon() / 60.0,
                                             DeadReckoning.LOWER_LIMB);
    outputPanel.setEaMoon(GeomUtil.DecToSex(estime.getHe().doubleValue(), GeomUtil.SWING));
    outputPanel.setZMoon(GeomUtil.DecToSex(estime.getZ().doubleValue(), GeomUtil.SWING));
    outputPanel.setCaMoon(GeomUtil.DecToSex(moonCa, GeomUtil.SWING));
    
    estime = new DeadReckoning(data.getGHApolaris(), 
                               data.getDECpolaris(), 
                               inputPanel.getL(), 
                               inputPanel.getG());
    estime.calculate();
    double polarisCa = estime.correctedAltitude(estime.getHe().doubleValue(),
                                                inputPanel.getEyeHeight(),
                                                0.0,
                                                0.0,
                                                -1);
    outputPanel.setEaPolaris(GeomUtil.DecToSex(estime.getHe().doubleValue(), GeomUtil.SWING));
    outputPanel.setZPolaris(GeomUtil.DecToSex(estime.getZ().doubleValue(), GeomUtil.SWING));
    outputPanel.setCaPolaris(GeomUtil.DecToSex(polarisCa, GeomUtil.SWING));
    
    // Bonus track: Local Solar Time
    System.out.println("GHA sun:" + Double.toString(data.getGHAsun()));
    System.out.println("Longitude:" + Double.toString(inputPanel.getG()));
    System.out.println("LHA:" + Double.toString(data.getGHAsun() - inputPanel.getG()));
    System.out.println("Local:" + GeomUtil.Angle2Hour(data.getGHAsun() + inputPanel.getG()));
    String localSolarTime = GeomUtil.Angle2Hour(data.getGHAsun() + inputPanel.getG());
    outputPanel.setStatusLabel("Local Solar Time:" + localSolarTime);    
  }
}