package jazmin.util;

import java.util.Random;
/**
 * 
 * @author yama
 * 27 Dec, 2014
 */
public class RandomUtil {
	
	private static final Random random = new Random();
	//
	public static int randomInt(int minValue, int maxValue){
		if(maxValue<minValue){
			return 0;
		}else if(maxValue==minValue){
			return minValue;
		}
		int randomNumber=minValue+random.nextInt(maxValue-minValue+1);
		return randomNumber;
	}
	//
	public static int randomInt(int maxNumber){
		return random.nextInt(maxNumber);
	}
	//
	public static int randomTotalValue(){
		return randomInt(10000);
	}
	//
	public static boolean isHitPercentOf10000(int percent){
		return randomTotalValue()<percent;
	}
	//
	public static int randomIntArray(int[] weight){
		int totalWeight=0;
		for(int e:weight){
			totalWeight+=e;
		}
		int rnd=RandomUtil.randomInt(totalWeight);
		int tmp=0;
		for(int i=0;i<weight.length;i++){
			tmp+=weight[i];
			if(rnd<tmp){
				return i;
			}
		}
		return 0;
	}
}