from itertools import izip
import sys, codecs
orig = codecs.open(sys.argv[1], 'r', encoding='utf-8')
unsupos = codecs.open(sys.argv[2], 'r', encoding='utf-8')

tp = 0.0
fp = 0.0
fn = 0.0

lineno = 0

for orig_line, unsupos_line in izip(orig, unsupos):
    lineno += 1
    orig_parts = orig_line.strip().split("\t")
    unsupos_parts = unsupos_line.strip().split("\t")
    if len(orig_parts) >= 10:
        assert(len(orig_parts) == len(unsupos_parts)), "Line length not same at line %d, was %d at original file and %d at unsupos conll file" % (lineno, len(orig_parts), len(unsupos_parts),)
        orig_is_verb = orig_parts[3].startswith("V")
        unsupos_is_verb = unsupos_parts[3].startswith("V")
        if orig_is_verb:
            if unsupos_is_verb:
                tp += 1
            else:
                fn += 1
        else:
            if unsupos_is_verb:
                fp += 1

print("Recall: %f (%f/%f)" % (tp/(tp+fn),tp,tp+fn,))
print("Precision: %f (%f/%f)" % (tp/(tp+fp),tp,tp+fp,))
