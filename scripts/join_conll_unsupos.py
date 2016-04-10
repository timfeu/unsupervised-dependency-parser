import sys, codecs
c = codecs.open(sys.argv[1], 'r', encoding='utf-8')
u = codecs.open(sys.argv[2], 'r', encoding='utf-8')
vpos = sys.argv[3].split(";")
o = codecs.open(sys.argv[4], 'w', encoding='utf-8')

print("Searching for POS tag(s) "+repr(vpos))

ulines = u.read().splitlines()
lineptr = 0
tokenptr = 0
utokens = ulines[lineptr].strip().split(' ')

for line in c:
    parts = line.strip().split("\t")
    if len(parts) < 10:
        o.write("\n")
        lineptr += 1
        tokenptr = 0
        if lineptr < len(ulines):
            utokens = ulines[lineptr].strip().split(' ')
    else:
        for i in range(0,10):
            if i == 3 or i == 4:
                tokenparts = utokens[tokenptr].rsplit('|',1)
                pos = tokenparts[1].rstrip('*')
                if pos in vpos:
                    o.write('VB')
                else:
                    o.write('U' + pos)
            else:
                o.write(parts[i])
            if i == 9:
                o.write("\n")
            else:
                o.write("\t")
        tokenptr += 1
c.close()
u.close()
o.close()
