package org.imos.abos;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import ocss.nmea.parser.GeoPos;

import org.eclipse.wb.swing.FocusTraversalOnArray;
import java.awt.Component;
import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.border.EtchedBorder;

@SuppressWarnings("serial")
public class EventDialog extends JFrame implements ActionListener
{
	private JTextField txtTime;
	private JTextField txtLat;
	private JTextField txtLon;
	// private JTextField txtEvent;
	private JComboBox txtEvent;
	ActionEvent buttonEvent;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel panel;
	private JPanel buttonPane;
	private JPanel panelButton;
	private JLabel lblTime;
	private JLabel lblLat;
	private JLabel lblLon;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		try
		{
			EventDialog dialog = new EventDialog(null, new Vector<String>());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setTime(String time)
	{
		txtTime.setText(time);
	}

	public void set(String lat, String lon)
	{
		txtLat.setText(lat);
		txtLon.setText(lon);
	}

	public Date getTime()
	{
		try
		{
			return sdf.parse(txtTime.getText());
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public Event getEvent()
	{
		return new Event(getTime(), getPos(), (String) txtEvent.getSelectedItem());
	}

	public String getLat()
	{
		return txtLat.getText();
	}

	public String getLon()
	{
		return txtLon.getText();
	}

	public GeoPos getPos()
	{
		return pos;
	}

	GeoPos pos = null;

	public ActionEvent getAction()
	{
		return buttonEvent;
	}

	/**
	 * Create the dialog.
	 * 
	 * @param eventLog
	 */
	public EventDialog(JComponent inside, Vector<String> events)
	{
		setTitle("Enter Event Details");

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		setBounds(100, 100, 459, 200);
		getContentPane().setLayout(new BorderLayout());
		setLocationRelativeTo(inside);
		{
			panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			panel.setLayout(new GridLayout(0, 1));
			{
				panelButton = new JPanel();
				panel.add(panelButton, BorderLayout.NORTH);
				panelButton.setLayout(new GridLayout(0, 2, 0, 0));
				{
					lblTime = new JLabel("Time Stamp");
					panelButton.add(lblTime);
				}
				{
					txtTime = new JTextField();
					panelButton.add(txtTime);
					txtTime.setColumns(10);
				}
				{
					lblLat = new JLabel("Latitude");
					panelButton.add(lblLat);
				}
				{
					txtLat = new JTextField();
					panelButton.add(txtLat);
					txtLat.setColumns(10);
				}
				{
					lblLon = new JLabel("Longitude");
					panelButton.add(lblLon);
				}
				{
					txtLon = new JTextField();
					panelButton.add(txtLon);
					txtLon.setColumns(10);
				}
			}
			{
				txtEvent = new JComboBox(events);
				txtEvent.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				txtEvent.setFont(new Font("Dialog", Font.PLAIN, 14));
				txtEvent.setEditable(true);
				panel.add(txtEvent, BorderLayout.CENTER);
			}
			{
				buttonPane = new JPanel();
				panel.add(buttonPane, BorderLayout.SOUTH);
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				{
					okButton = new JButton("OK");
					okButton.setActionCommand("OK");
					okButton.addActionListener(this);
					buttonPane.add(okButton);
					getRootPane().setDefaultButton(okButton);
				}
				{
					cancelButton = new JButton("Cancel");
					cancelButton.setActionCommand("Cancel");
					cancelButton.addActionListener(this);
					buttonPane.add(cancelButton);
				}
			}
		}
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { txtEvent, okButton, cancelButton, txtTime, txtLat, txtLon, getContentPane(), panel, buttonPane, panelButton, lblTime, lblLat, lblLon }));
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		buttonEvent = arg0;
		// System.out.println("EventDialog actionPerformed " + arg0);
		dispose();
	}

	public void setPos(GeoPos newPos)
	{
		pos = newPos;
		if (pos != null)
		{
			txtLat.setText(pos.getLatInDegMinDec());
			txtLon.setText(pos.getLngInDegMinDec());
		}
	}
}
