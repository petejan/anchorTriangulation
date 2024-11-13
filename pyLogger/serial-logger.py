#!/usr/bin/env python
# Log data from serial port

# Author: Diego Herranz

import argparse
import serial
import datetime
import time
import os

parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument("-n", "--nmea", help="device to read GPS from", default="/dev/ttyUSB1")
parser.add_argument("-d", "--device", help="device to read deck box from", default="/dev/ttyUSB0")
parser.add_argument("-s", "--speed", help="speed in bps", default=9600, type=int)
args = parser.parse_args()

outputFilePath = os.path.join(os.path.dirname(__file__),
                 datetime.datetime.now().strftime("%Y-%m-%dT%H.%M.%S") + ".bin")

with open(outputFilePath, mode='wb') as outputFile:
    ser = serial.Serial(args.device, args.speed)
    ser1 = serial.Serial(args.nmea, 9600)
    print("Logging started. Ctrl-C to stop.") 
    try:
        while True:
            time.sleep(1)
            if ser.inWaiting() > 0:
                c = ser.read(ser.inWaiting())
                #print(c.decode("ascii"))
                outputFile.write(c)
                outputFile.flush()
            if ser1.inWaiting() > 0:
                c = ser1.read(ser1.inWaiting())
                print(c.decode("ascii"))
                outputFile.write(c)
                outputFile.flush()
    except KeyboardInterrupt:
        print("Logging stopped")
