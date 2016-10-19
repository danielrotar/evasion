
import socket
import random
import sys
import time
import random

host = "localhost"
port = int(sys.argv[1])

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
sock.connect((host, port))

sock.sendall("name: random_player_" + str(port) + "\n")

hunter = False
stream = ""
while True:
    while True:
        stream = stream + sock.recv(4096)
        lines = stream.split("\n")
        if len(lines) > 1:
            line = lines[-2]
            stream = lines[-1]
            break
        else:
            continue
    #print "received: " + line
    val = .1 + random.uniform(0,1)
    #print "will sleep for: " + str(val)
    start = time.time()
    time.sleep(val)
    now = time.time()
    print "actually slept for: " + str(now-start)
    if line == "done":
        break
    elif line == "hunter":
        hunter = True
    elif line == "prey":
        hunter = False
    else:
        data = line.split(" ")
        if hunter:
            x = random.randint(0,50)
            wall = "0"
            if x == 0:
                wall = "1"
            elif x == 1:
                wall = "2"
            if random.randint(0,20) == 0:
                wall = "0 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20"
            tosend = data[1] + " " + data[2] + " " + wall
            #print "sending: " + tosend
            sock.sendall(tosend + "\n")
        else:
            x = random.randint(-1,1)
            y = random.randint(-1,1)
            tosend = data[1] + " " + data[2] + " " + str(x) + " " + str(y)
            #print "sending: " + tosend
            sock.sendall(tosend + "\n")