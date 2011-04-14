package com.jcake2.tipcalculator;

public class PercentHelper {

	public static double getPercent(int progress, int max){
		return Math.min(50, 50 * progress * 1.0 / max);
	}
	
	public static int getProgress(double fraction, int max){
		return (int) (fraction * max * 2);
	}
}
