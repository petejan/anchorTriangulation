import numpy as np
import utm
import sys
import re

import datetime

from matplotlib.backends.backend_pdf import PdfPages
import matplotlib.pyplot as plt
from matplotlib.patches import Circle

import scipy.optimize as optimize

"""
Created on Thu Mar 26 18:05:51 2015

python program to find anchor position from release soundings

format of input file:

2021-04-24 05:41:23 UTC,46°49.90' S,141°38.76'E,-46.831707,141.645980,Anchor deployed
2021-04-24 07:33:52 UTC,46°51.00' S,141°36.06'E,-46.849947,141.601010,SAZ-23 triangulation
2021-04-24 07:34:26 UTC,46°50.99' S,141°36.06'E,-46.849905,141.600973,
2021-04-24 07:35:05 UTC,46°50.99' S,141°36.06'E,-46.849819,141.600946,RNG: TX = 11.0 RX = 12.0 time = --.--- Sec.
2021-04-24 07:35:18 UTC,46°50.99' S,141°36.06'E,-46.849789,141.600935,CMD: 265023
2021-04-24 07:36:28 UTC,46°50.98' S,141°36.05'E,-46.849733,141.600888,RNG: TX = 11.0 RX = 12.0 time = 08.807 Sec.
2021-04-24 07:36:53 UTC,46°50.98' S,141°36.05'E,-46.849711,141.600882,CMD: 265046
2021-04-24 07:37:59 UTC,46°50.98' S,141°36.05'E,-46.849642,141.600822,CMD: 173633
2021-04-24 07:39:18 UTC,46°50.97' S,141°36.05'E,-46.849503,141.600754,RNG: TX = 11.0 RX = 12.0 time = --.--- Sec.
2021-04-24 07:39:40 UTC,46°50.97' S,141°36.04'E,-46.849470,141.600724,RNG: TX = 11.0 RX = 12.0 time = 08.807 Sec.
2021-04-24 07:41:08 UTC,46°50.96' S,141°36.04'E,-46.849323,141.600588,RNG: TX = 11.0 RX = 12.0 time = 08.806 Sec.


@author: Peter Jansen

2022-05-27 Added text page to pdf output

"""

# error function to optimise 
def dist(x, x0, y0, z0):
    return np.sqrt((x[0] - x0)**2 + (x[1] - y0)**2 + z0 ** 2)

def distz(point, z0):
    return np.sqrt((point[0])**2 + (point[1])**2 + z0 ** 2)

i = 0

utmloc = []  # list of utm locations
t = []  # list of times

list_pre = []  # list of pre-locations

# get file from command line
fn = sys.argv[1]

line_exp = re.compile(r'(\d{4}\-\d{2}\-\d{2} \d{2}:\d{2}:\d{2}) UTC.*,.*,.*,(.*),(.*),(.*)$')
range_exp = re.compile(r'(\d{4}\-\d{2}\-\d{2} \d{2}:\d{2}:\d{2}) UTC.*,.*,.*,(.*),(.*),RNG: TX = [\d\.]* RX = [\d\.]* time = ([\d\.]*) Sec\.$')

# read the event log file

with open(fn , 'rt' , encoding="iso-8859-1") as f:

    for line in f:
        if (line.startswith(';')):
            continue

        matchobj = line_exp.match(line)
        if matchobj:
            if (line.find('Anchor') >= 0) | (i == 0): # get position of anchor drop, or first record
                anchor_drop_lat = float(matchobj.group(2))
                anchor_drop_lon = float(matchobj.group(3))
                anchor_utm = utm.from_latlon(anchor_drop_lat, anchor_drop_lon)
                print("anchor reference", anchor_drop_lat, anchor_drop_lon, line)
                
            match_range = range_exp.match(line)
            if match_range:
                print (match_range.group(2), match_range.group(3), match_range.group(4)) 
                t.append(float(match_range.group(4)))
                utmloc.append(utm.from_latlon(float(match_range.group(2)), float(match_range.group(3)), force_zone_number = anchor_utm[2]))
            else:
                list_pre.append({'ts': matchobj.group(1), 'lat': float(matchobj.group(2)), 'lon': float(matchobj.group(3)), 'txt': matchobj.group(4)})
            i = i + 1

#ship_transducer_depth=6.3 # m
ship_transducer_depth=9 # m
release_height_above_seafloor=39 # m SOFS
#release_height_above_seafloor=56.6 # m EAC
#release_height_above_seafloor=125.1 # m SAZ
#release_height_above_seafloor=46 # m SAZ
soundspeed=1500.0 # local speed m/s

soundx = [u[0] for u in utmloc]
soundy = [u[1] for u in utmloc]

# create a local coordinate centre fr0m the anchor drop position

localx = np.asarray(soundx) - anchor_utm[0]
localy = np.asarray(soundy) - anchor_utm[1]

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
print('anchor utm', anchor_utm)
anchor_solution = (x1 + anchor_utm[0], y1 + anchor_utm[1], anchor_utm[2], anchor_utm[3])
anchor_solution_ll = utm.to_latlon(anchor_solution[0], anchor_solution[1], anchor_solution[2], anchor_solution[3])

error1 = np.zeros(len(obs1[0]))
for i in np.arange(len(obs1[0])):
    error1[i] = np.abs(distz([obs1[0][i]-x1, obs1[1][i]-y1], z1) - z_1[i])
    
stderror1 = error1.mean()

fallback = (np.sqrt(x1**2 + y1**2))

with PdfPages(fn+'.pdf') as pdf:

    #plt.figure(figsize=(11.69, 8.27))
    plt.figure()

    txt = 'sound speed used : ' + str(soundspeed) + ' m/s\n'
    txt += 'ship transducer depth : ' + str(ship_transducer_depth) + ' m\n'
    txt += 'release to anchor : ' + str(release_height_above_seafloor) + ' m\n\n'

    for l in list_pre:
        pre_utm = utm.from_latlon(l['lat'], l['lon'], force_zone_number = anchor_utm[2]);
        pre_dist = np.sqrt((anchor_solution[0] - pre_utm[0])**2 + (anchor_solution[1] - pre_utm[1])**2)
        #print('pre list', l, pre_dist)
        txt += l['ts'] + ' pre : ' + '{:6.2f} m'.format(pre_dist) + ' : ' + l['txt'] + '\n'
    txt += '\n'

    txt += 'file : ' + fn + '\n' + '\n'
    txt += 'std error first pass {:4.2f}'.format(stderror) + '\n'
    txt += 'fit x = {:6.2f}, y = {:6.2f}, z1 = {:6.2f}'.format( x1 , y1,  z1) + '\n'
    txt += 'std error second pass {:4.2f}'.format(stderror1) + '\n'
    txt += 'fall back {:4.1f} (m)'.format(fallback) + '\n'
    txt += 'anchor solution lat {:9.5f} lon {:9.5f}'.format(anchor_solution_ll[0], anchor_solution_ll[1]) + '\n'
    txt += 'release depth {:6.1f} anchor depth {:6.1f}'.format(z1, z1+release_height_above_seafloor+ship_transducer_depth)

    plt.text(-0.1, -0.1, txt, fontsize=8, family='monospace')
    plt.axis('off')
    pdf.savefig()
    plt.close()

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
        if z[i] > z1:
            r = np.sqrt(z[i]**2 - z1**2)
            edgecolor = 'g'
            # colour points out of the tolerence red
            if (error[i] > tolerance):
                edgecolor = 'r'
            c = Circle([localx[i], localy[i]], r, fill=0, edgecolor=edgecolor, linewidth=0.2)
            ax.add_patch(c)
                            
    plt.grid()
    plt.title(fn + '\nanchor {:6.1f}(m) lat,lon {:9.5f} {:9.5f} deg\nmean error 2nd pass {:4.2f} (m)\nred - 2nd pass, blue-star 1st pass, black circle - 1sd'.format(z1+release_height_above_seafloor+ship_transducer_depth, anchor_solution_ll[0], anchor_solution_ll[1], stderror1), fontsize=8)
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
    d['CreationDate'] = datetime.datetime(2022, 5, 27)
    d['ModDate'] = datetime.datetime.today()


# some outputs

print ('file', fn)
print ('std error first pass {:4.2f}'.format(stderror))
print ('fit = ' , x1 , ' y ', y1, ' z1 = ', z1)
print ('std error second pass {:4.2f}'.format(stderror1))
print ('fall back {:4.1f} (m)'.format(fallback))
print ('anchor solution lat {:9.5f} lon {:9.5f}'.format(anchor_solution_ll[0], anchor_solution_ll[1]))
print ('release depth {:6.1f} anchor depth {:6.1f}'.format(z1, z1+release_height_above_seafloor+ship_transducer_depth))
