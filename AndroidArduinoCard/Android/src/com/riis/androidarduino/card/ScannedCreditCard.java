package com.riis.androidarduino.card;

import java.util.Calendar;
import java.util.Date;


public class ScannedCreditCard {
	//This info is from <en.wikipedia.org/wiki/Magnetic_stripe_card#Financial_cards>
	private static final char T1_START_SENTINEL = '%';
	private static final char T1_END_SENTINEL = '?';
	private static final char T1_FIELD_SEPERATOR = '^';
	
	private static final char T2_START_SENTINEL = ';';
	private static final char T2_END_SENTINEL = '?';
	private static final char T2_FIELD_SEPERATOR = '=';
	
	private static final char NAME_SEPERATOR = '/';
	
	private String track1;
	private String track2;
	private String track3;
	
	private char formatCode;
	private String cardNumber;
	private String cardType;
	private String firstName;
	private String lastName;
	private String expDate;
	private String serviceCode;
	private String discretionaryData;
	
	
	private Date scanDate;
	
	public boolean matches(ScannedCreditCard otherScan) {
		return cardNumber.equals(otherScan.cardNumber);
	}

	public String getTrack1() {
		return track1;
	}

	public void setTrack1(String track1) {
		this.track1 = track1;
	}

	public String getTrack2() {
		return track2;
	}

	public void setTrack2(String track2) {
		this.track2 = track2;
	}

	public String getTrack3() {
		return track3;
	}

	public void setTrack3(String track3) {
		this.track3 = track3;
	}

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}

	public void parseData(String message, int encryptionKey) {
		seperateTracks(message);
		
		try {
			parseFirstTrack();
		} catch(Exception e) {
			
		}
		try {
			parseSecondTrack();
		} catch(Exception e) {
			
		}
		try {
			parseThirdTrack();
		} catch(Exception e) {
		
		}
		

    	setScanDate(Calendar.getInstance().getTime());
		
	}
	
	private void seperateTracks(String message) {
		StringBuilder trackSplitREGEX = new StringBuilder();
		trackSplitREGEX.append("[");
		trackSplitREGEX.append(T1_START_SENTINEL);
		trackSplitREGEX.append(T1_END_SENTINEL);
		trackSplitREGEX.append(T2_START_SENTINEL);
		trackSplitREGEX.append(T2_END_SENTINEL);
		trackSplitREGEX.append("]+");
		String[] tracks = message.split(trackSplitREGEX.toString());
    	if(tracks.length >= 1)
    		setTrack1(tracks[0]);
    	else
    		setTrack1("");
    	if(tracks.length >= 2)
    		setTrack2(tracks[1]);
    	else
    		setTrack2("");
    	if(tracks.length >= 3)
    		setTrack3(tracks[2]);
    	else
    		setTrack3("");
	}

	private void parseFirstTrack() {
		StringBuilder trackSplitREGEX = new StringBuilder();
		trackSplitREGEX.append("[\\");
		trackSplitREGEX.append(T1_FIELD_SEPERATOR);
		trackSplitREGEX.append("]+");
		String[] fields = track1.split(trackSplitREGEX.toString());
		formatCode = fields[0].charAt(0);
		cardNumber = fields[0].substring(1);
		getCardTypeFromNumber();
		
		String[] nameInfo = fields[1].split("" + NAME_SEPERATOR);
		lastName = nameInfo[0];
		firstName = nameInfo[1];
		
		expDate = fields[2].substring(0, 4);
		serviceCode = fields[2].substring(4, 7);
		discretionaryData = fields[2].substring(7);
	}
	
	private void getCardTypeFromNumber() {
		if(cardNumber.charAt(0) == '4')
			cardType = "Visa";
		else if(Integer.valueOf(cardNumber.substring(0, 2)) >= 51 &&
				Integer.valueOf(cardNumber.substring(0, 2)) <= 55)
			cardType = "Master Card";
		else if(Integer.valueOf(cardNumber.substring(0, 2)) == 34 ||
				Integer.valueOf(cardNumber.substring(0, 2)) == 37)
			cardType = "American Express";
		else if(cardNumber.substring(0, 4).equalsIgnoreCase("6011"))
			cardType = "Discover";
		else
			cardType = "Unknown";
	}
	
	private void parseSecondTrack() {
		StringBuilder trackSplitREGEX = new StringBuilder();
		trackSplitREGEX.append("[\\");
		trackSplitREGEX.append(T2_FIELD_SEPERATOR);
		trackSplitREGEX.append("]+");
		String[] fields = track2.split(trackSplitREGEX.toString());
		cardNumber = fields[0];
		getCardTypeFromNumber();
		
		expDate = fields[1].substring(0, 4);
		serviceCode = fields[1].substring(4, 7);
		discretionaryData = fields[1].substring(7);
	}
	
	private void parseThirdTrack() {
		//Currently unused by financial card standard.
	}
	
	public String generateCardInfoString() {
		StringBuilder cardInfo = new StringBuilder();
		cardInfo.append("Name: " + firstName + " " + lastName + "\n");
		cardInfo.append("Card Type: " + cardType + "\n");
		cardInfo.append("Card Number: " + cardNumber + "\n");
		cardInfo.append("Exp. Date: " + expDate.substring(2) + "/" + expDate.substring(0, 2) + "\n");
		cardInfo.append("Service Code: " + serviceCode + "\n");
		return cardInfo.toString();
	}
}
