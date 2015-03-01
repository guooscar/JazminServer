/**
 * 
 */
package jazmin.driver.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;

/**
 * @author yama
 * 21 Jan, 2015
 */
public class SearchDocument {
	Document document;
	Term term;
	public SearchDocument() {
		document=new Document();
	}
	//
	public void putString(String key,String content,boolean store){
		StringField field=new StringField(key, content, store?Field.Store.YES:Field.Store.NO);
		document.add(field);
	}
	public void putText(String key,String content,boolean store){
		document.add(new TextField(key, content, store?Field.Store.YES:Field.Store.NO));
	}
	//
	public void setTerm(String fl,String text){
		term=new Term(fl, text);
	}
	//
	public String get(String key){
		return document.get(key);
	}
}
