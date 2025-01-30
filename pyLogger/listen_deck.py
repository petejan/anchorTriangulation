import socket
from datetime import datetime, timezone

import threading
import struct
import pynmea2

import re

MCAST_GRP = '224.0.36.0' # GPS multicast group and port
MCAST_PORT = 50102

HOST = '150.229.233.155'    # The remote host, comtrol serial server
PORT = 8000

lat = float("nan")
lon = float("nan")

# thread to read the ranges from the deck box


def deck_box_thread():
    global lat, lon

    range_exp = re.compile(r'RNG: TX = [\d\.]* RX = [\d\.]* time = ([\d\.]*) Sec\.')

    f = open('SOFS-11-Recovery-Release.txt', "w+")
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST, PORT))
        print('connected', s)

        time_arm = False
        line = b''
        while True:
            data = s.recv(1024)
            # print('Received', len(data), repr(data))
            for i in data:
                if time_arm:
                    line_decode = line.decode()
                    current_datetime = datetime.now(timezone.utc)
                    # RNG: TX = 11.0 RX = 12.0 time = 08.807 Sec.
                    time = float("nan")
                    match_range = range_exp.match(line_decode)
                    record = [current_datetime.strftime("%Y-%m-%d %H:%M:%S"), "{:.6f}".format(lat), "{:.6f}".format(lon) ,line_decode]
                    if match_range:
                        #print("range match", match_range.group(1))
                        time = float(match_range.group(1))
                        record.append("{:.1f} m".format(time * 750))  # convert time to m, 1500/2 m/s

                    print(",".join(record))
                    f.write(",".join(record))
                    f.write("\n")
                    time_arm = False
                    line = b''
                if i == 10:
                    time_arm = True
                elif i == 13:
                    pass
                else:
                    line += bytes(chr(i), 'ascii')

            f.flush()

# thread to read multicast gps location, and keep lat/lon updated


def gps_thread():
    global lat, lon

    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((MCAST_GRP, MCAST_PORT))
    mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)

    s.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
    while True:
        msg = s.recv(1024)
        #print(f"gps thread: {msg}")
        line = b''
        for i in msg:
            if i == 10:
                #print(line.decode())
                gps_msg = pynmea2.parse(line.decode())
                #print(gps_msg)
                if hasattr(gps_msg, 'latitude') and hasattr(gps_msg, 'longitude'):
                    #print(gps_msg.latitude, gps_msg.longitude)
                    lat = gps_msg.latitude
                    lon = gps_msg.longitude
                line = b''
            else:
                line += bytes(chr(i), 'ascii')


t1 = threading.Thread(target=deck_box_thread)
t2 = threading.Thread(target=gps_thread)
t1.start()
t2.start()
t1.join()
t2.join()


