import sys, codecs
f = codecs.open(sys.argv[1], 'r', encoding='utf-8')
o = codecs.open(sys.argv[2], 'w', encoding='utf-8')
for line in f:
    parts = line.strip().split("\t")
    if len(parts) < 10:
        o.write("\n")
    else:
        o.write(parts[1])
        o.write(" ")
f.close()
o.close()