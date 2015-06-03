/**  
* @Title: Utils.java
* @Package com.fived.welink.dataproc
* @Description: TODO
* @author CJF
* @date 2014-6-2 обнГ2:36:20
* @version V1.0  
*/
package com.fived.welink.dataproc;

public class Utils {  
    private static long lastClickTime;  
    public static boolean isFastDoubleClick(long art) {  
        long time = System.currentTimeMillis();  
        long timeD = time - lastClickTime;  
        if ( 0 < timeD && timeD < art) {     
            return true;     
        }     
        lastClickTime = time;     
        return false;     
    }  
}  
