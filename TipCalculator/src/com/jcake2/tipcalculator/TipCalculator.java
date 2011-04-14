package com.jcake2.tipcalculator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class TipCalculator extends Activity {
	private static final String TIPCALCOPTIONS = "TIPCALCOPTIONS";
	private static double MIN_TIP = 0.1;
	private static double MAX_TIP = 0.25;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadMinAndMax();
		setContentView(R.layout.options);
		setContentView(R.layout.tip); // FIXME forcing this to be loaded?
		setContentView(R.layout.main);
	}
	
	private void loadMinAndMax() {
		FileInputStream fis = null;
		try {
			fis = openFileInput(TIPCALCOPTIONS);
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			String restoreStr = new String(buffer);
			if(restoreStr.contains(":")){
				String[] minAndMax = restoreStr.split(":");
				MIN_TIP = Double.parseDouble(minAndMax[0]);
				MAX_TIP = Double.parseDouble(minAndMax[1]);
			}
			closeFile(fis);
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			closeFile(fis);
		} 
	}

	private void closeFile(FileInputStream fis) {
		try {
			fis.close();
		} catch (IOException ioe) {
			throw new RuntimeException("Could not close files", ioe);
		}
	}
	
	@Override
	public void onDestroy() {
		saveMinAndMax();
		super.onDestroy();
	}
	
	private void saveMinAndMax() {
		FileOutputStream fos = null;

		try {
			fos = openFileOutput(TIPCALCOPTIONS, Context.MODE_PRIVATE);
					fos.write((MIN_TIP + ":" + MAX_TIP).getBytes());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				throw new RuntimeException("Could not close file", e);
			}
		}
	}
	
	public void help(View view){
		setContentView(R.layout.help);
	}
	
	public void options(View view){
		setContentView(R.layout.options);
		//TODO do all this setup in the xml ahead of time?
		HorizontalSlider minSlider = (HorizontalSlider) findViewById(R.id.min_slider);
		HorizontalSlider maxSlider = (HorizontalSlider) findViewById(R.id.max_slider);
		minSlider.setLinkedSlider(maxSlider);
		TextView minValue = (TextView) findViewById(R.id.min_text);
		TextView maxValue = (TextView) findViewById(R.id.max_text);
		minSlider.setPercentTracker(minValue);
		maxSlider.setPercentTracker(maxValue);
		minSlider.updateProgress(PercentHelper.getProgress(MIN_TIP,minSlider.getMax()));
		maxSlider.updateProgress(PercentHelper.getProgress(MAX_TIP,maxSlider.getMax()));
	}
	
	public void optionsDone(View view){
		HorizontalSlider minSlider = (HorizontalSlider) findViewById(R.id.min_slider);
		HorizontalSlider maxSlider = (HorizontalSlider) findViewById(R.id.max_slider);
		MIN_TIP = PercentHelper.getPercent(minSlider.getProgress(), minSlider.getMax()) / 100;
		MAX_TIP = PercentHelper.getPercent(maxSlider.getProgress(), maxSlider.getMax()) / 100;
		setContentView(R.layout.main);
		saveMinAndMax();
	}

	public void calculate(View view) {
		EditText total = (EditText) findViewById(R.id.total);
		String text = total.getText().toString();

		RadioGroup generous = (RadioGroup) findViewById(R.id.generous);
		boolean isGenerous = getValueOfThisRadioButton(generous);

		RadioGroup niceWaiter = (RadioGroup) findViewById(R.id.niceWaiter);
		boolean wasNice = getValueOfThisRadioButton(niceWaiter);
		
		RadioGroup refills = (RadioGroup) findViewById(R.id.drinkRefills);
		boolean gotRefills = getValueOfThisRadioButton(refills);
		
		RadioGroup correctOrder = (RadioGroup) findViewById(R.id.correctOrder);
		boolean gotCorrectOrder = getValueOfThisRadioButton(correctOrder);
		
		String result = calculateTip(text, isGenerous, wasNice, gotRefills, gotCorrectOrder);
		showTipView(result);
	}

	private boolean getValueOfThisRadioButton(RadioGroup niceWaiter) {
		int niceWaiterValue = niceWaiter.getCheckedRadioButtonId();
		boolean wasNice = true;
		if (niceWaiterValue == R.id.no) {
			wasNice = false;
		}
		return wasNice;
	}

	private void showTipView(String tipValue) {
		setContentView(R.layout.tip);
		TextView tip = (TextView) findViewById(R.id.tip);
		tip.setText(tipValue);
	}

	private String calculateTip(String totalString, boolean isGenerous,
			boolean wasNice, boolean gotRefills, boolean gotCorrectOrder) {
		try {
			double total = parseForTotal(totalString);
			double percent = calculateTipPercent(isGenerous, wasNice, gotRefills, gotCorrectOrder);
			double tip = total * percent;
			String tipString = formatAsMoney(tip);
			return tipString;
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR:  please enter total as $ddd.dd";
		}
	}

	private double calculateTipPercent(boolean isGenerous, boolean wasNice, boolean gotRefills, boolean gotCorrectOrder) {
		double tipRange = MAX_TIP - MIN_TIP;
		double percent = MIN_TIP + tipRange * 3 / 10;
		if (isGenerous) {
			percent += 3 * tipRange / 10;
		}
		if (wasNice) {
			percent += 2 * tipRange / 10;
			if(isGenerous){
				percent += 2 * tipRange / 10;
			}
		}
		else{
			percent -= 1 * tipRange / 10;
		}
		if (gotRefills){
			percent += 1 * tipRange / 10;
		}
		if (!gotCorrectOrder){
			percent -= 3 * tipRange / 10;
		}
		percent = Math.min(percent, MAX_TIP);
		percent = Math.max(percent, MIN_TIP);
		return percent;
	}

	private double parseForTotal(String totalString) {
		if (totalString.startsWith("$")) {
			totalString = totalString.replace("$", "");
		}
		double total = Double.parseDouble(totalString);
		return total;
	}

	private String formatAsMoney(double tip) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		Formatter result = formatter.format("$%(,.2f", tip);
		return result.toString();
	}

	public void goToMain(View view) {
		setContentView(R.layout.main);
	}
}