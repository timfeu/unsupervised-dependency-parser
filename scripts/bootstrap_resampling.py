import sys
from random import randint

a_file = open(sys.argv[1], 'r')
b_file = open(sys.argv[2], 'r')

a_scores = list()
b_scores = list()

def read_scores(f, l):
	for line in f:
		parts = line.split()
		l.append(float(parts[3]))

read_scores(a_file, a_scores)
read_scores(b_file, b_scores)
assert len(a_scores) == len(b_scores), "Got %d scores A but %d scores B" % (len(a_scores),len(b_scores),)

b_won = 0

for i in range(0,1000):
	a_score_sum = 0.0
	b_score_sum = 0.0
	for j in range(0,len(a_scores)):
		random_index = randint(0,len(a_scores)-1)
		a_score_sum += a_scores[random_index]
		b_score_sum += b_scores[random_index]
	if b_score_sum > a_score_sum:
		#print("b_score_sum %f while a_score_sum %f" % (b_score_sum/len(a_scores), a_score_sum/len(a_scores),))
		b_won += 1

print("In %d experiments, b won %d times (%f percent)" % (1000, b_won, (float(b_won)/1000)*100,))
