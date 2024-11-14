package org.imos.abos;

import java.util.ArrayList;

import org.imos.abos.CustomSerialReader;

import ocss.nmea.api.NMEAClient;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;

public class CustomClient extends NMEAClient
{
	public CustomClient(String s, String[] sa)
	{
		super(s, sa);
	}

	public void dataDetectedEvent(NMEAEvent e)
	{
		System.out.println("Received:" + e.getContent());
	}

	private static CustomClient customClient = null;

	public static void main(String[] args)
	{
		String prefix = "GP";
		String[] array = { "GLL", "GSA", "RMC" };
		customClient = new CustomClient(prefix, array);

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				System.out.println("CustomClient:: Shutting down nicely.");
				customClient.stopDataRead();
			}
		});
		customClient.initClient();
		customClient.setReader(new CustomSerialReader(customClient.getListeners()));
		customClient.startWorking();
	}

	private void stopDataRead()
	{
		if (customClient != null)
		{
			ArrayList<NMEAListener> al = customClient.getListeners();

			for (NMEAListener l : al)
				l.stopReading(new NMEAEvent(this));
		}
	}
}