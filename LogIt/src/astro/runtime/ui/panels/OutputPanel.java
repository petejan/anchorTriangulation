package astro.runtime.ui.panels;
import astro.runtime.ui.AppFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.SystemColor;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

public class OutputPanel extends JPanel 
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel7 = new JLabel();
  private JLabel jLabel8 = new JLabel();
  private JLabel jLabel9 = new JLabel();
  private JLabel jLabel10 = new JLabel();
  private JLabel jLabel11 = new JLabel();
  private JLabel jLabel12 = new JLabel();
  private JLabel jLabel13 = new JLabel();
  private JLabel AriesGHA = new JLabel();
  private JLabel SunGHA = new JLabel();
  private JLabel SunDec = new JLabel();
  private JLabel MoonGHA = new JLabel();
  private JLabel MoonDec = new JLabel();
  private JLabel MoonSD = new JLabel();
  private JLabel MoonHP = new JLabel();
  private JLabel PolarisGHA = new JLabel();
  private JLabel PolarisDec = new JLabel();
  private JLabel jLabel14 = new JLabel();
  private JLabel jLabel15 = new JLabel();
  private JLabel EaSun = new JLabel();
  private JLabel ZSun = new JLabel();
  private JLabel jLabel16 = new JLabel();
  private JLabel CaSun = new JLabel();
  private JLabel jLabel17 = new JLabel();
  private JLabel EaMoon = new JLabel();
  private JLabel jLabel19 = new JLabel();
  private JLabel CaMoon = new JLabel();
  private JLabel jLabel21 = new JLabel();
  private JLabel ZMoon = new JLabel();
  private JLabel jLabel23 = new JLabel();
  private JLabel jLabel24 = new JLabel();
  private JLabel SunSD = new JLabel();
  private JLabel SunHP = new JLabel();
  private JLabel jLabel27 = new JLabel();
  private JLabel EaPolaris = new JLabel();
  private JLabel jLabel29 = new JLabel();
  private JLabel ZPolaris = new JLabel();
  private JLabel jLabel31 = new JLabel();
  private JLabel CaPolaris = new JLabel();
  private JPanel jPanel1 = new JPanel();
  private JLabel statusLabel = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();

  public OutputPanel(AppFrame parent)
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
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(475, 342));
    this.setFont(new Font("Tahoma", 1, 11));
    jLabel1.setText("GHA");
    jLabel1.setFont(new Font("Tahoma", 1, 11));
    jLabel2.setText("GHA");
    jLabel2.setFont(new Font("Tahoma", 1, 11));
    jLabel3.setText("Dec");
    jLabel3.setFont(new Font("Tahoma", 1, 11));
    jLabel4.setText("GHA");
    jLabel4.setFont(new Font("Tahoma", 1, 11));
    jLabel5.setText("Dec");
    jLabel5.setFont(new Font("Tahoma", 1, 11));
    jLabel6.setText("sd");
    jLabel6.setFont(new Font("Tahoma", 1, 11));
    jLabel7.setText("hp");
    jLabel7.setFont(new Font("Tahoma", 1, 11));
    jLabel8.setText("GHA");
    jLabel8.setFont(new Font("Tahoma", 1, 11));
    jLabel9.setText("Dec");
    jLabel9.setFont(new Font("Tahoma", 1, 11));
    jLabel10.setText("Aries");
    jLabel10.setFont(new Font("Tahoma", 1, 12));
    jLabel11.setText("Sun");
    jLabel11.setFont(new Font("Tahoma", 1, 12));
    jLabel11.setBackground(SystemColor.control);
    jLabel12.setText("Moon");
    jLabel12.setFont(new Font("Tahoma", 1, 12));
    jLabel13.setText("Polaris");
    jLabel13.setFont(new Font("Tahoma", 1, 12));
    AriesGHA.setText("000*00.00");
    SunGHA.setText("000*00.00");
    SunDec.setText("N 00*00.00");
    MoonGHA.setText("000*00.00");
    MoonDec.setText("N 00*00.00");
    MoonSD.setText("00.000\'");
    MoonHP.setText("00.000\'");
    PolarisGHA.setText("000*00.00");
    PolarisDec.setText("N 00*00.00");
    jLabel14.setText("Est. Alt.");
    jLabel14.setFont(new Font("Tahoma", 1, 11));
    jLabel15.setText("Z.");
    jLabel15.setFont(new Font("Tahoma", 1, 11));
    EaSun.setText("00.00");
    ZSun.setText("000");
    jLabel16.setText("Corr. Alt.");
    jLabel16.setFont(new Font("Tahoma", 1, 11));
    CaSun.setText("00.00");
    jLabel17.setText("Est. Alt.");
    jLabel17.setFont(new Font("Tahoma", 1, 11));
    EaMoon.setText("00.00");
    jLabel19.setText("Corr. Alt.");
    jLabel19.setFont(new Font("Tahoma", 1, 11));
    CaMoon.setText("00.00");
    jLabel21.setText("Z.");
    jLabel21.setFont(new Font("Tahoma", 1, 11));
    ZMoon.setText("000");
    jLabel23.setText("sd");
    jLabel23.setFont(new Font("Tahoma", 1, 11));
    jLabel24.setText("hp");
    jLabel24.setFont(new Font("Tahoma", 1, 11));
    SunSD.setText("00.000\'");
    SunHP.setText("00.000\'");
    jLabel27.setText("Est. Alt");
    jLabel27.setFont(new Font("Tahoma", 1, 11));
    EaPolaris.setText("00.00");
    jLabel29.setText("Z.");
    jLabel29.setFont(new Font("Tahoma", 1, 11));
    ZPolaris.setText("000");
    jLabel31.setText("Corr. Alt.");
    jLabel31.setFont(new Font("Tahoma", 1, 11));
    CaPolaris.setText("00.00");
    jPanel1.setLayout(borderLayout1);
    statusLabel.setText("Status");
    statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    this.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel3, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel4, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel5, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel6, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel7, new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel8, new GridBagConstraints(1, 15, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel9, new GridBagConstraints(2, 15, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel11, new GridBagConstraints(1, 0, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel12, new GridBagConstraints(1, 7, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel13, new GridBagConstraints(1, 14, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(AriesGHA, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
    this.add(SunGHA, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 5), 0, 0));
    this.add(SunDec, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 10), 0, 0));
    this.add(MoonGHA, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 5), 0, 0));
    this.add(MoonDec, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    this.add(MoonSD, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
    this.add(MoonHP, new GridBagConstraints(4, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 10), 0, 0));
    this.add(PolarisGHA, new GridBagConstraints(1, 16, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 5), 0, 0));
    this.add(PolarisDec, new GridBagConstraints(2, 16, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 10), 0, 0));
    this.add(jLabel14, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel15, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(EaSun, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(ZSun, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel16, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(CaSun, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel17, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(EaMoon, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel19, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(CaMoon, new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel21, new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(ZMoon, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel23, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel24, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(SunSD, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(SunHP, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel27, new GridBagConstraints(1, 17, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(EaPolaris, new GridBagConstraints(1, 18, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel29, new GridBagConstraints(2, 17, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(ZPolaris, new GridBagConstraints(2, 18, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel31, new GridBagConstraints(1, 19, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    this.add(CaPolaris, new GridBagConstraints(1, 20, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(statusLabel, BorderLayout.CENTER);
    this.add(jPanel1, new GridBagConstraints(0, 21, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 10, 0), 0, 0));
  }
  
  public void setAriesGHA(String str)
  { AriesGHA.setText(str); }
  public void setSunGHA(String str)
  { SunGHA.setText(str); }
  public void setSunDec(String str)
  { SunDec.setText(str); }
  public void setSunSD(String str)
  { SunSD.setText(str); }
  public void setSunHP(String str)
  { SunHP.setText(str); }
  public void setMoonGHA(String str)
  { MoonGHA.setText(str); }
  public void setMoonDec(String str)
  { MoonDec.setText(str); }
  public void setMoonSD(String str)
  { MoonSD.setText(str); }
  public void setMoonHP(String str)
  { MoonHP.setText(str); }
  public void setPolarisGHA(String str)
  { PolarisGHA.setText(str); }
  public void setPolarisDec(String str)
  { PolarisDec.setText(str); }
  
  public void setEaSun(String str)
  { EaSun.setText(str); }
  public void setZSun(String str)
  { ZSun.setText(str); }
  public void setCaSun(String str)
  { CaSun.setText(str); }
  
  public void setEaMoon(String str)
  { EaMoon.setText(str); }
  public void setZMoon(String str)
  { ZMoon.setText(str); }
  public void setCaMoon(String str)
  { CaMoon.setText(str); }
  
  public void setEaPolaris(String str)
  { EaPolaris.setText(str); }
  public void setZPolaris(String str)
  { ZPolaris.setText(str); }
  public void setCaPolaris(String str)
  { CaPolaris.setText(str); }
  
  public void setStatusLabel(String str)
  { statusLabel.setText(str); }
  
}