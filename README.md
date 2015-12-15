CDT is a Java package for Compositional Distributional Tensor semantics. The following is a tutorial on how to run a word level experiment (Wordsim353). Please cite

A Quantum-Theoretic Approach to Distributional Semantics
William Blacoe, Elham Kashefi, Mirella Lapata
Proceedings of the 2013 Conference of the North American Chapter of the Association for Computational Linguistics: Human Language Technologies (NAACL 2013)

Explanations on running compositional sentence level experiments will follow in the future. For these please cite

Semantic Composition Inspired by Quantum Measurement
William Blacoe
Proceedings of the 8th International Conference on Quantum Interaction (QI 2014)



1) Dependency-parse the corpus and save as text file or gzipped text file. Dividing the corpus into multiple files (kept in the same folder) is recommended because threading is used to process one file per thread. Make sure the files' text is in the following format, where <s> tags sentences and <text> tags documents:

<text id="ukwac:http://www.caa.co.uk/default.aspx?categoryid=122&pagetype=90&pageid=1166">
<s>
1       For     for     IN      IN      _       15      prep    _       _
2       further further JJR     JJR     _       3       amod    _       _
3       information     information     NN      NN      _       1       pobj    _       _
4       on      on      IN      IN      _       3       prep    _       _
5       what    what    WP      WP      _       12      dobj    _       _
6       the     the     DT      DT      _       7       det     _       _
7       obligations     obligation      NNS     NNS     _       12      nsubj   _       _
8       of      of      IN      IN      _       7       prep    _       _
9       the     the     DT      DT      _       10      det     _       _
10      trustee trustee NN      NN      _       8       pobj    _       _
11      are     are     VBP     VBP     _       12      aux     _       _
12      see     see     VB      VB      _       4       pcomp   _       _
13      Group   group   NNP     NNP     _       14      nn      _       _
14      Ownership       ownership       NNP     NNP     _       12      dep     _       _
15      .       .       SENT    SENT    _       0       null    _       _
</s>
...
<s>
...
</s>
</text>


2) Put all files required for this experiment in a project folder:
	- the space file (space.txt found in this package)
	- the dataset file
	- the target words file (listing all words in the dataset)
	- the stoplist file
Set the hyperparameters in the space file. Make all paths relative to the project folder path (see below).

3) Start the experiment by running
experiment.wordlevel.wordsim353.Wordsim353 [path of project folder]
where project folder is the folder that contains all files required for this experiment (all paths contained in the code are relative to this path). The space file "space.txt" should be contained directly in the project folder. The Wordsim experiment's code contains some more hyperparameters (e.g. ) which can be changed. Experiment steps:
	- If the space file does not yet specify the target word vocabulary, this will be updated automatically using the target words file.
	- If the space file does not yet specify context word vocabularies, this will be done automatically by extracting counts for context words from the corpus (and saved in the context counts file specified in "space.txt"), and the space file will be updated with this information. This step can be accelerated if a context words count file already exists. This file can be obtained by running corpus.dep.contextCounter.DepContextCounter [path of corpus folder] [path of output file] [amount of sentences]
The amount of sentences parameter should be set to -1 if all sentences of all files in the corpus folder are to be used. Otherwise the first [amount of sentences] number of sentences from each corpus file will be used.
	- Joint counts are extracted from the corpus (and saved in the folder "jdops")
	- Marginal counts are extracted from the corpus (and saved in the folder "jdops")
	- An association function akin to PMI is applied to the joint and marginal counts to obtain the final lexical representation. In the ldops files
		- the [ldops cardinality] highest entries are tagged with <basematrices>
		- the marginalisation (partial trace) of each mode are tagged with <partialtracediagonals><mode>...</partialtracediagonals>
	- All inner products computed during the experiment are saved in "innerProducts.txt" so that they can be reused.
	- The Spearman correlation coefficient between the datasets target values and the predicted values is computed and printed.


Disclaimer: This version of the CDT code is a huge overhaul of the version used for the paper "A Quantum-Theoretic Approach to Distributional Semantics". Obtaining the same results as in the paper is by no means guaranteed. Among other things, modelling aspects and hyperparameters have been changed in the mean time.
