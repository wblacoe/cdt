<h1>CDT</h1>
is a Java package for <b>Compositional Distributional Tensor semantics</b>. The following is a tutorial on how to run a word level experiment (Wordsim353). Please cite

A Quantum-Theoretic Approach to Distributional Semantics
William Blacoe, Elham Kashefi, Mirella Lapata
Proceedings of the 2013 Conference of the North American Chapter of the Association for Computational Linguistics: Human Language Technologies (NAACL 2013)

Explanations on running compositional sentence level experiments will follow in the future. For these please cite

Semantic Composition Inspired by Quantum Measurement
William Blacoe
Proceedings of the 8th International Conference on Quantum Interaction (QI 2014)


<ol>
<li>Dependency-parse the corpus and save as text file or gzipped text file. Dividing the corpus into multiple files (kept in the same folder) is recommended because threading is used to process one file per thread. Make sure the files' text is in the following format, where &lt;s&gt; tags sentences and &lt;text&gt; tags documents:</li>

&lt;text id="ukwac:http://www.caa.co.uk/default.aspx?categoryid=122&pagetype=90&pageid=1166"&gt;
&lt;s&gt;
<table>
<tr><td>1</td><td>For</td><td>for</td><td>IN</td><td>IN</td><td>_</td><td>15</td><td>prep</td><td>_</td><td>_</td></tr>
<tr><td>2</td><td>further</td><td>further</td><td>JJR</td><td>JJR</td><td>_</td><td>3</td><td>amod</td><td>_</td><td>_</td></tr>
</table>
&lt;/s&gt;
...
&lt;s&gt;
...
&lt;/s&gt;
&lt;/text&gt;


<li>Put all files required for this experiment in a project folder:
	<ol>
	<li>the space file (space.txt found in this package)</li>
	<li>the dataset file</li>
	<li>the target words file (listing all words in the dataset)</li>
	<li>the stoplist file</li>
	</ol>
Set the hyperparameters in the space file. Make all paths relative to the project folder path (see below).
</li>


<li>Start the experiment by running
experiment.wordlevel.wordsim353.Wordsim353 [path of project folder]
where project folder is the folder that contains all files required for this experiment (all paths contained in the code are relative to this path). The space file "space.txt" should be contained directly in the project folder. The Wordsim experiment's code contains some more hyperparameters (e.g. ) which can be changed. Experiment steps:
	<ol>
	<li>If the space file does not yet specify the target word vocabulary, this will be updated automatically using the target words file.</li>
	<li>If the space file does not yet specify context word vocabularies, this will be done automatically by extracting counts for context words from the corpus (and saved in the context counts file specified in "space.txt"), and the space file will be updated with this information. This step can be accelerated if a context words count file already exists. This file can be obtained by running corpus.dep.contextCounter.DepContextCounter [path of corpus folder] [path of output file] [amount of sentences]
The amount of sentences parameter should be set to -1 if all sentences of all files in the corpus folder are to be used. Otherwise the first [amount of sentences] number of sentences from each corpus file will be used.</li>
	<li>Joint counts are extracted from the corpus (and saved in the folder "jdops")</li>
	<li>Marginal counts are extracted from the corpus (and saved in the folder "jdops")</li>
	<li>An association function akin to PMI is applied to the joint and marginal counts to obtain the final lexical representation. In the ldops files
		<ol>
		<li>the [ldops cardinality] highest entries are tagged with &lt;basematrices&gt;</li>
		<li>the marginalisation (partial trace) of each mode are tagged with &lt;partialtracediagonals&gt;&lt;mode&gt;...&lt;/partialtracediagonals&gt;</li>
		</ol>
	</li>
	<li>All inner products computed during the experiment are saved in "innerProducts.txt" so that they can be reused.</li>
	<li>The Spearman correlation coefficient between the datasets target values and the predicted values is computed and printed.</li>
	</ol>
</li>
</ol>

Disclaimer: This version of the CDT code is a huge overhaul of the version used for the paper "A Quantum-Theoretic Approach to Distributional Semantics". Obtaining the same results as in the paper is by no means guaranteed. Among other things, modelling aspects and hyperparameters have been changed in the mean time.
