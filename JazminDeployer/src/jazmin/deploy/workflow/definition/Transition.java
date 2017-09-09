/**
 * 
 */
package jazmin.deploy.workflow.definition;

/**
 * @author yama
 *
 */
public class Transition {
	public String name;
	public String to;
	public Transition(){
		
	}
	public Transition(String name,String to){
		this.name=name;
		this.to=to;
	}
}
