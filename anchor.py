# -*- coding: utf-8 -*-
"""
Created on Thu Mar 26 18:05:51 2015

@author: pete
"""

import numpy as np
import utm

import datetime
from matplotlib.backends.backend_pdf import PdfPages
import matplotlib.pyplot as plt

import matplotlib.pyplot as plt
import scipy.optimize as optimize
import csv
import sys
from matplotlib.patches import Circle

def d(x, y, z):
    return np.sqrt(x**2 + y**2 + z**2)
    
def dist(x, x0, y0, z0):
    return np.sqrt((x[0] - x0)**2 + (x[1] - y0)**2 + z0 ** 2)

def distz(x, z0):
    return np.sqrt((x[0])**2 + (x[1])**2 + z0 ** 2)

#obs = 2
#x = np.zeros([obs, 3])
#y = np.zeros(obs)
 
#guess = [0, 0, 4000]
#x[0] = [-10, -10, 4020]
#y[0] = 4000
#x[1] = [10, 10, 4020]
#y[1] = 4000

#(x0, y0, z0), pcov = optimize.curve_fit(dist, x, y, guess)

i = 0
utmloc =[]
t = []

#fn = '2015-03-25-EventLog-Pulse-11-triangulation.txt'
#fn = '2015-03-23-EventLog-SOFS-5-triangulation.txt'
#fn = '2015-03-27-EventLog-SAZ47-17-triangulation.txt'
fn = sys.argv[1]

# read the event log file

with open(fn , 'rb') as f:
    reader = csv.reader(f)
    for row in reader:
        if (row[0].startswith(';')):
            continue
        txt = row[5]
        if ((txt.find('Anchor') >= 0)) | ( i == 0): # get position of anchor drop, or first record
            anchor_drop_lat = np.float64(row[3])
            anchor_drop_lon = np.float64(row[4])
            anchor_utm = utm.from_latlon(anchor_drop_lat, anchor_drop_lon);
        ore = row[5].split(' ')
        if (len(ore) >= 9):
            if (ore[9] != '--.---'):
                if (float(ore[9]) > 0.4):
                    print row[3], row[4], ore[9] 
                    t.append(float(ore[9]))
                    utmloc.append(utm.from_latlon(float(row[3]), float(row[4]), force_zone_number = anchor_utm[2]))
                    i = i + 1

ship_transducer_depth=6.3 # m
#release_height_above_seafloor=33.5 # m
#release_height_above_seafloor=56.6 # m EAC
release_height_above_seafloor=125.1 # m SAZ
soundspeed=1500.0 # local speed m/s

soundx = [u[0] for u in utmloc]
soundy = [u[1] for u in utmloc]

# create a local coordinate centre fr0m the anchor drop position

localx = soundx - np.float64(anchor_utm[0])
localy = soundy - np.float64(anchor_utm[1])

# convert ping travel time to distance
z = np.multiply(t, soundspeed / 2)

# seem like a good place to start, the anchor drop location
guess = [0, 0, 4600]

# find least squares estimate of release location
obs = [localx, localy]
(x0, y0, z0), pcov = optimize.curve_fit(dist, obs, z, guess)

# calculate error in estimate
error = np.zeros(len(obs[1]))
for i in np.arange(len(obs[1])):
    error[i] = np.abs(distz([localx[i]-x0, localy[i]-y0], z0) - z[i])
    
stderror = error.mean()
tolerance = stderror * 1

# remove points with large error
obs1 = [localx[error < tolerance], localy[error < tolerance]]
z_1 = z[error < tolerance]
    
(x1, y1, z1), pcov = optimize.curve_fit(dist, obs1, z_1, [x0, y0, z0])
ll = utm.to_latlon(x1 + anchor_utm[0], y1 + anchor_utm[1], anchor_utm[2], anchor_utm[3])

error1 = np.zeros(len(obs1[0]))
for i in np.arange(len(obs1[0])):
    error1[i] = np.abs(distz([obs1[0][i]-x1, obs1[1][i]-y1], z1) - z_1[i])
    
stderror1 = error1.mean()

with PdfPages(fn+'.pdf') as pdf:
        
    # now some plotting
    fig, ax = plt.subplots()
    
    plt.plot(x0, y0, 'b*')
    plt.plot(x1, y1, 'ro')
    c = Circle([x0, y0], stderror, fill=0, edgecolor='black', linewidth=0.5)
    ax.add_patch(c)
    c = Circle([x1, y1], stderror1, fill=0, edgecolor='black', linewidth=1)
    ax.add_patch(c)

    # plot the raw data
    plt.plot(localx, localy, 'b')
    
    # create circles on the plot based on the radius
    for i in np.arange(len(localx)):
        r = np.sqrt(z[i]**2 - z1**2)
        edgecolor = 'g'
        # colour points out of the tolerence red
        if (error[i] > tolerance):
            edgecolor = 'r'
        c = Circle([localx[i], localy[i]], r, fill=0, edgecolor=edgecolor, linewidth=0.2)
        ax.add_patch(c)
    
    plt.grid()
    plt.title(fn + '\nanchor {:6.1f}(m) lat,lon {:9.5f} {:9.5f} deg\nmean error 2nd pass {:4.2f} (m)\nred - 2nd pass, blue-star 1st pass, black circle - 1sd'.format(z1+release_height_above_seafloor+ship_transducer_depth, ll[0], ll[1], stderror1), fontsize=8)
    plt.xlabel('x dist from anchor drop (m)')
    plt.ylabel('y dist from anchor drop (m)')
    pdf.savefig()
    plt.title('')
    plt.xlim([x1 - 100, x1+100])
    plt.ylim([y1 - 100, y1+100])
    pdf.savefig()
    flg, ax1 = plt.subplots()
    ax1.plot(error, 'g', label='error')
    plt.ylabel('error (m)')
    plt.ylim([0, 500])
    ax2 = ax1.twinx()
    ax2.plot(t,'b', label='t')
    plt.ylim([0, 12])
    plt.ylabel('t (sec)')
    plt.grid()
    plt.xlabel('sample no')
    legend = ax1.legend('error',loc='upper right')
    legend = ax2.legend('t',loc='lower right')
    pdf.savefig()

    d = pdf.infodict()
    d['Title'] = 'Anchor Triangulation'
    d['Author'] = 'Peter Jansen'
    d['Subject'] = 'Anchor Triangulation'
    d['CreationDate'] = datetime.datetime(2015, 4, 2)
    d['ModDate'] = datetime.datetime.today()


# some outputs

fallback = (np.sqrt(x1**2 + y1**2))

print 'file ', fn
print 'std error first pass {:4.2f}'.format(stderror)
print 'fit = ' , x1 , ' y ', y1, ' z1 = ', z1
print 'std error second pass {:4.2f}'.format(stderror1)
print 'fall back {:4.1f} (m)'.format(fallback)
print "anchor solution lat {:9.5f} lon {:9.5f}".format(ll[0], ll[1])
print 'release depth {:6.1f} anchor depth {:6.1f}'.format(z1, z1+release_height_above_seafloor+ship_transducer_depth)
