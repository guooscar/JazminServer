/**
 * 
 */
package jazmin.test.core.aop;

/**
 * @author yama
 * 23 Dec, 2014
 */
public class TestService {
	public void methodA(){
		System.out.println("A");
	}
	public int methodB(){
		System.out.println("B");
		return 1;
	}
	public int methodC(){
		throw new IllegalArgumentException("C");
	}
	//
	public static void methodE(){
		
	}
	public void methodF(int a){
		System.out.println("F:"+a);
	}
}
