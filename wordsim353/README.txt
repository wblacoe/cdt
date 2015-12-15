

CORPUS
Place corpus files (and no other files) in a folder in ./corpora
They can be raw text or g-zipped .
Their content should have the following format:

<text id="ukwac:http://observer.guardian.co.uk/osm/story/0,,1009777,00.html">
<s>
1	Hooligans	hooligan	NNS	NNS	_	0	null	_	_
2	,	,	,	,	_	4	punct	_	_
3	unbridled	unbridled	JJ	JJ	_	4	amod	_	_
4	passion	passion	NN	NN	_	1	null	_	_
5	-	-	:	:	_	4	punct	_	_
6	and	and	CC	CC	_	4	cc	_	_
7	no	no	DT	DT	_	9	det	_	_
8	executive	executive	JJ	JJ	_	9	amod	_	_
9	boxes	box	NNS	NNS	_	4	conj	_	_
10	.	.	SENT	SENT	_	4	dep	_	_
</s>
<s>
1	Not	not	RB	RB	_	3	neg	_	_
2	many	many	JJ	JJ	_	3	amod	_	_
3	footballers	footballer	NNS	NNS	_	4	nsubj	_	_
4	speak	speak	VBP	VBP	_	0	null	_	_
5	out	out	RP	RP	_	4	prt	_	_
6	against	against	IN	IN	_	4	prep	_	_
7	the	the	DT	DT	_	9	det	_	_
8	corporate	corporate	JJ	JJ	_	9	amod	_	_
9	makeover	makeover	NN	NN	_	6	pobj	_	_
10	,	,	,	,	_	9	punct	_	_
11	or	or	CC	CC	_	9	cc	_	_
12	takeover	takeover	NN	NN	_	9	conj	_	_
13	,	,	,	,	_	4	punct	_	_
14	of	of	IN	IN	_	4	prep	_	_
15	English	english	JJ	JJ	_	16	amod	_	_
16	football	football	NN	NN	_	14	pobj	_	_
17	.	.	SENT	SENT	_	4	punct	_	_
</s>
</text>

<text...>
<s>
...
</s>
...
<s>
...
</s>
</text>

DATASET
http://www.cs.technion.ac.il/~gabr/resources/data/wordsim353/wordsim353.zip -> combined.tab

VOCABULARY
cat combined.tab | tail -n +2 | awk -F'\t' '{ print $1"\n"$2; }' | sort | uniq > words

SPACE
The file space.wordsim353 is provided which defines the partitioning of all dependency relations into modes. When the program is run and the vocabulary and context counts files are already prepared, this space file gets overwritten, now containing all context words as dimensions for each mode and all target words. The file space.empty is provided as a backup.
