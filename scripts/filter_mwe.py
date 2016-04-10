import sys 
f = open(sys.argv[1], 'r')
o = open(sys.argv[2], 'w')
s = float(sys.argv[3])
for line in f:
    parts = line.split("\t")
    if int(parts[0]) > 1 and float(parts[2]) >= s:
        o.write(parts[0])
        o.write("\t")
        o.write(parts[1])
        o.write("\t")
        o.write(parts[2])
        o.write("\n")
f.close()
o.close()