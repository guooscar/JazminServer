/**
 * 
 */
package jazmin.deploy.workflow.definition;

/**
 * @author yama
 *
 */
public class Transtion {
	public String name;
	public String to;
	public Transtion(){
		
	}
	public Transtion(String name,String to){
		this.name=name;
		this.to=to;
	}
}
