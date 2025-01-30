#!/usr/bin/env python
# Log data from serial port

import threading
import serial

import argparse
import datetime
import time
import os

parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument("-n", "--nmea", help="device to read GPS from", default="/dev/ttyUSB1")
parser.add_argument("-d", "--device", help="device to read deck box from", default="/dev/ttyUSB0")
parser.add_argument("-s", "--speed", help="speed in bps", default=9600, type=int)
args = parser.parse_args()

outputFilePath = os.path.join(os.path.dirname(__file__), datetime.datetime.now().strftime("%Y-%m-%dT%H.%M.%S") + ".bin")

connected = False

ser_deck = serial.Serial(args.device, args.speed)
ser_nmea = serial.Serial(args.nmea, 9600)

def handle_data(name, data, outFile):
    print(name, data.strip())
    outFile.write(data.encode('utf-8'))
    outFile.flush()

def read_from_port(ser, name, outFile):
    while True:
        #serin = ser.read()
        connected = True

        while True:
           reading = ser.readline().decode()
           handle_data(name, reading, outFile)


def recv(port=50000, addr="239.192.1.100", buf_size=1024):
        """recv([port[, addr[,buf_size]]]) - waits for a datagram and returns the data."""

        # Create the socket
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

        # Set some options to make it multicast-friendly
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
                s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
        except AttributeError:
                pass # Some systems don't support SO_REUSEPORT
        s.setsockopt(socket.SOL_IP, socket.IP_MULTICAST_TTL, 20)
        s.setsockopt(socket.SOL_IP, socket.IP_MULTICAST_LOOP, 1)

        # Bind to the port
        s.bind(('', port))

        # Set some more multicast options
        intf = socket.gethostbyname(socket.gethostname())
        s.setsockopt(socket.SOL_IP, socket.IP_MULTICAST_IF, socket.inet_aton(intf))
        s.setsockopt(socket.SOL_IP, socket.IP_ADD_MEMBERSHIP, socket.inet_aton(addr) + socket.inet_aton(intf))

        # Receive the data, then unregister multicast receive membership, then close the port
        data, sender_addr = s.recvfrom(buf_size)
        s.setsockopt(socket.SOL_IP, socket.IP_DROP_MEMBERSHIP, socket.inet_aton(addr) + socket.inet_aton('0.0.0.0'))
        s.close()
        return data


outputFile = open(outputFilePath, mode='wb')

#outputFile.write(c)
#outputFile.flush()

thread1 = threading.Thread(target=read_from_port, args=(ser_deck, "deck", outputFile))
thread1.start()

thread2 = threading.Thread(target=read_from_port, args=(ser_nmea,"gps", outputFile))
thread2.start()

