import sys
f = open(sys.argv[1], 'r')
o = open(sys.argv[2], 'w')

for line in f:
    parts = line.strip().split()
    if len(parts) == 10:
        parts[4] = parts[3]
        o.write("\t".join(parts) + "\n")
    else:
        o.write(line)

o.close()
f.close()