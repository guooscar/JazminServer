package jazmin.deploy.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author icecooly
 *
 */
public class DateUtil {

	public static Date getNextDay(int num) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, num);
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTime();
	}

	//
	public static boolean isToday(Date date) {
		Calendar calDateA = Calendar.getInstance();
		calDateA.setTime(date);
		Calendar calDateB = Calendar.getInstance();
		calDateB.setTime(new Date());
		return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
				&& calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
				&& calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
	}
}
