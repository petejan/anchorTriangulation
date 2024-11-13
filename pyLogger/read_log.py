
import sys
import pynmea2
from datetime import datetime, timedelta

filename = sys.argv[1]

msg = None
lat = None
lon = None
ts = None

# output format
# 2024-04-06 04:52:34 UTC,46°47.80' S,141°48.78'E,-46.796589,141.813063,RNG: TX = 11.0 RX = 12.00 time = --.--- Sec.

with open(filename, 'r', encoding='UTF-8') as file:
    while line := file.readline():
        l = line.rstrip()
        #print(line.rstrip())
        if line.startswith("RNG"):
            print(ts.strftime("%Y-%m-%d %H:%M:%S UTC"), ",,,", lat, ",", lon, ",", l)
        else:
            #print(l)
            try:
                msg = pynmea2.parse(l)
                if hasattr(msg, "latitude"):
                    lat = msg.latitude
                    lon = msg.longitude
                    #print(lat, lon)
                if hasattr(msg, "timestamp"):
                    ts = msg.timestamp
                if hasattr(msg, "day"):
                    ts = timedelta(hours=msg.timestamp.hour, minutes=msg.timestamp.minute, seconds=msg.timestamp.second)+ datetime(msg.year, msg.month, msg.day)
            except pynmea2.ParseError as e:
                #print('Parse error: {}'.format(e))
                pass
