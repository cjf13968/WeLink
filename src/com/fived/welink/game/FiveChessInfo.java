/**  
 * @Title: FiveChessInfo.java
 * @Package com.fived.welink.game
 * @Description: TODO
 * @author CJF
 * @date 2014-6-1 下午2:39:03
 * @version V1.0  
 */
package com.fived.welink.game;

/**
 * @ClassName: FiveChessInfo
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author A18ccms a18ccms_gmail_com
 * @date 2014-6-1 下午2:39:03
 * 
 */
public class FiveChessInfo extends GameInfo {
	public static final String ROLE_BLACK = "black";
	public static final String ROLE_White = "white";

	public int r;
	public int c;

	public FiveChessInfo(int r, int c) {
		this.r = r;
		this.c = c;
	}
}
