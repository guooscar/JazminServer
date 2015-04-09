/**
 * 
 */
package jazmin.driver.lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jazmin.core.Driver;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.misc.InfoBuilder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;

/**
 * @author yama 21 Jan, 2015
 */
public class LuceneSearchDriver extends Driver{
	private static Logger logger=LoggerFactory.get(LuceneSearchDriver.class);
	//
	private IndexReader reader;
	private Analyzer analyzer;
	private String indexPath;
	//
	public void indexPath(String path){
		if(inited()){
			throw new IllegalArgumentException("set before inited");
		}
		this.indexPath=path;
	}
	//
	@Override
	public void init() throws Exception {
		analyzer=new ComplexAnalyzer();
	}
	//
	@Override
	public String info() {
		InfoBuilder ib=new InfoBuilder();
		ib.format("%-30s : %-30s\n");
		ib.print("indexPath",indexPath);
		return ib.toString();
	}
	// --------------------------------------------------------------------------
	// search and index
	//
	public void indexDocuments(List<SearchDocument>docs) throws SearchException {
		try{
			Directory dir = FSDirectory.open(new File(indexPath));
			// :Post-Release-Update-Version.LUCENE_XY:
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST,analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriter writer = new IndexWriter(dir, iwc);
			for(SearchDocument doc:docs){
				if(logger.isDebugEnabled()){
					logger.debug("index:"+doc.term);
				}
				writer.updateDocument(doc.term,doc.document);
			}
			writer.commit();
			writer.close();
		}catch(Exception e){
			throw new SearchException(e);
		}finally{
			resetReader();
		}
	}
	//
	public void deleteDocument(String key,String id) throws SearchException {
		try{
			Directory dir = FSDirectory.open(new File(indexPath));
			// :Post-Release-Update-Version.LUCENE_XY:
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST,analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriter writer = new IndexWriter(dir, iwc);
			writer.deleteDocuments(new Term(key,id));
			writer.commit();
			writer.close();
		}catch(Exception e){
			throw new SearchException(e);
		}finally{
			resetReader();
		}
	}
	//
	public void indexDocument(SearchDocument doc) throws SearchException {
		List<SearchDocument>docs=new ArrayList<SearchDocument>();
		docs.add(doc);
		indexDocuments(docs);
	}
	//
	private void resetReader(){
		
	}
	//
	private void initReader()throws Exception{
		if(reader!=null){
			reader.close();
		}
		reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
	}
	//
	public List<SearchDocument> searchDocuments(String key[],String query,int number) 
			throws SearchException{
		try{
			initReader();
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(number, true);
			MultiFieldQueryParser mqp=new MultiFieldQueryParser(key, analyzer);
			Query q = mqp.parse(query);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			List<SearchDocument>result=new ArrayList<SearchDocument>();
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				SearchDocument dd=new SearchDocument();
				dd.document=d;
				result.add(dd);
			}
			return result;
		}catch(Exception e){
			throw new SearchException(e);
		}
	}
	//
	public SearchDocument getDocument(int docId) 
			throws SearchException{
		try {
			initReader();
			SearchDocument sd=new SearchDocument();
			sd.document=reader.document(docId);
			if(sd.document==null){
				return null;
			}
			return sd;
		} catch (Exception e) {
			throw new SearchException(e);
		}
		
	}
	//
	public int getNumDocs() throws SearchException{
		try {
			initReader();
			return reader.numDocs();
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
	//
	public List<SearchDocument> queryMoreLikeThis(String likeFields[], int docId,
			int count) throws SearchException {
		try {
			initReader();
			List<SearchDocument> result = new ArrayList<SearchDocument>();
			IndexSearcher searcher = new IndexSearcher(reader);
			MoreLikeThis mlt = new MoreLikeThis(reader); 
			mlt.setAnalyzer(analyzer);
			mlt.setFieldNames(likeFields);
			mlt.setMinTermFreq(1);
			mlt.setMinDocFreq(1);
			Document doc = reader.document(docId);
			Query query = mlt.like(docId);
			TopDocs similarDocs = searcher.search(query, count);
			for (int i = 0; i < similarDocs.scoreDocs.length; i++) {
				if (similarDocs.scoreDocs[i].doc != docId) {
					doc = reader.document(similarDocs.scoreDocs[i].doc);
					SearchDocument sd = new SearchDocument();
					sd.document = doc;
					result.add(sd);
				}
			}
			return result;
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}
	
}
