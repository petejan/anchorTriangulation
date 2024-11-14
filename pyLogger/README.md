# pyLogger 

Log data from 2 serial ports

python package requirements:
	pyserial
	pynmea2
	utm
	matplotlib

nmea GPS connected to COM1 and deck box connected to COM2

`python serial-threaded-logger.py -nmea COM1 -d COM2`

ranges from the deck box look like `RNG: TX = 11.0 RX = 12.0 time = 00.135 Sec.'

convert logged file to 'legacy' processor

`python read_log.py 2024-11-11T21.58.37.bin > ranges.txt`

this log file looks like
```
$GPGGA,215855.00,4235.61487,S,14814.46458,E,1,09,1.03,-5.0,M,-7.7,M,,*7E
$GPGSA,A,3,06,17,11,19,14,22,03,24,12,,,,1.87,1.03,1.57*06
$GPGSV,3,1,11,03,06,133,25,06,57,055,30,11,31,004,30,12,40,239,39*7F
$GPGSV,3,2,11,14,13,073,32,17,33,125,37,19,62,133,47,22,33,082,35*7C
$GPGSV,3,3,11,24,55,272,29,25,03,245,34,32,03,210,39*48
$GPGLL,4235.61487,S,14814.46458,E,215855.00,A,A*75
$GPZDA,215855.00,11,11,2024,00,00*6C
```

create triangulation report, there a some constants in anchor.py which may need adjusting for your usage,

```
#ship_transducer_depth=6.3 # m
ship_transducer_depth=2 # m
#release_height_above_seafloor=33.5 # m
#release_height_above_seafloor=56.6 # m EAC
#release_height_above_seafloor=125.1 # m SAZ
#release_height_above_seafloor=46 # m SAZ
release_height_above_seafloor=1 # m BRUVS
soundspeed=1500.0 # local speed m/s
```

`python anchor.py ranges.txt`

should output something like

```
file ranges.txt
std error first pass 18.97
fit =  36.53491130170402  y  23.130081924437086  z1 =  89.25726787968706
std error second pass 1.61
fall back 43.2 (m)
anchor solution lat -42.59337 lon 148.24152
release depth   89.3 anchor depth   92.3
```

and a PDF report

