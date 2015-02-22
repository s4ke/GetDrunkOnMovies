package de.fsmpi.drunkserver.db;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;

/**
 * @author Martin Braun
 */
public class CompleteWordAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		// we use a tokenizer that doesn't remove dots,
		// hyphens or whatever as this program is used for language research and
		// we don't want to filter things out that could be found otherwise
		final Tokenizer src = new WhitespaceTokenizer(reader);
		TokenStream tok = new TrimFilter(src);
		tok = new LowerCaseFilter(tok);
		// we don't want empty tokens
		tok = new LengthFilter(tok, 1, Integer.MAX_VALUE);
		// we shouldn't lowercase here or use stopwordfilters, as this is for
		// the analysis of texts with all its parts
		return new TokenStreamComponents(src, tok);
	}

}
