Unsupervised Dependency Parser
==============================

The software implements a language-independent unsupervised parser creating unlabeled de-
pendencies as described in Søgaard (2012). This report consists of two parts. The first part
shows how the software can be compiled and used to create dependency trees. The second part
describes our experiments with various modifications of the parser.

**Warning:** I wasn't able to reproduce the results from the paper, so there may be something inherently wrong with the software.

How to build
------------

To compile the parser, you require Apache Maven 2.0 1. Change to the `junsupervised` directory
and run `mvn package` to create a non-standalone JAR in the target directory. You can also
change to `junsupervised/parser` and run `mvn assembly:assembly` to create a fat JAR file
containing all dependencies. Note that distributing this fat JAR may violate the license terms of dependent libraries.

How to run
----------

The parser has no standalone version. The easiest way to run the parser is inside an [uimaFIT](https://uima.apache.org/uimafit.html) pipeline. Add the following dependency to your `pom.xml`:

	<dependency>
	  <groupId>org.apache.uima</groupId>
	  <artifactId>uimafit-core</artifactId>
	  <version>2.1.0</version>
	</dependency>

In its most basic setup, the parser requires segmented and tokenized text annotated as [DKPro](https://dkpro.github.io/) types. Having POS tags increases accuracy. Here is an example reading raw English text, applying the OpenNLP segmenter and POS tagger, and writing out the annotated text in CoNLL-2007 format:

	import de.tudarmstadt.informatik.lt.sogaardparser.JUnsupervisedParser;
	import de.tudarmstadt.informatik.lt.sogaardparser.UniversalPOSTagConverter;
	import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2006Writer;
	import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
	import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
	import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
	import org.apache.uima.UIMAException;
	import org.apache.uima.fit.pipeline.SimplePipeline;

	import java.io.IOException;

	import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
	import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

	public class Main {
	    public static void main(String[] args) throws UIMAException, IOException {
		SimplePipeline.runPipeline(
		        createReaderDescription(TextReader.class,
		                TextReader.PARAM_SOURCE_LOCATION, "YOURTEXT.txt",
		                TextReader.PARAM_LANGUAGE, "en"),
		        createEngineDescription(OpenNlpSegmenter.class),
		        createEngineDescription(OpenNlpPosTagger.class),
		        createEngineDescription(UniversalPOSTagConverter.class),
		        createEngineDescription(JUnsupervisedParser.class,
		                JUnsupervisedParser.PARAM_FUNCTION_WORDS_FILE, "A_LIST_OF_KEYWORDS.txt",
		                JUnsupervisedParser.PARAM_USE_UNIVERSAL_RULES, false,
		                JUnsupervisedParser.PARAM_USE_POS_VERB, true),
		        createEngineDescription(Conll2006Writer.class,
		                Conll2006Writer.PARAM_TARGET_LOCATION, "OUTPUT_FOLDER")
		);
	    }
	}

**Note:** The German CoNLL-06 data does not adhere to the data standard, as some tokens miss the POS tag in column 5. Although they still have the coarse grained POS tag in column 4, these tokens will be dropped by the DKPro Conll2007Reader. You can fix the data by applying the `fixpos` Python script which basically just copies column 4 to column 5.

References
----------

* Søgaard, Anders (2012). “Unsupervised dependency parsing without training”. In: Natural Lan-
guage Engineering 18.02, pp. 187–203.
