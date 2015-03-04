package com.jams.music.player.EqualizerActivity;

import android.database.Cursor;

import com.jams.music.player.DBHelpers.DBAccessHelper;

/**
 * Created by jake on 2/23/15.
 *
 * Container class for equalizer values.
 */
public class EqualizerData {
	private String _name;

	// Temp variables that hold the equalizer's settings.
	private int _50Hz = 16;
	private int _130Hz = 16;
	private int _320Hz = 16;
	private int _800Hz = 16;
	private int _2Khz = 16;
	private int _5Khz = 16;
	private int _12Pt5Khz = 16;

	// Temp variables that hold audio fx settings.
	private int _virtualizer;
	private int _bassBoost;
	private int _reverb;

	public EqualizerData( String name, int... args ) {
		int[] values = new int[] { _50Hz, _130Hz, _320Hz, _800Hz, _2Khz, _5Khz, _12Pt5Khz, _virtualizer, _bassBoost, _reverb };
		int x = 0;

		for( int arg : args ) {
			values[ x++ ] = arg;
		}

		_name = name;
	}

	public EqualizerData( int[] args ) {
		int[] values = new int[] { _50Hz, _130Hz, _320Hz, _800Hz, _2Khz, _5Khz, _12Pt5Khz, _virtualizer, _bassBoost, _reverb };
		int x = 0;

		for( int arg : args ) {
			values[ x++ ] = arg;
		}
	}

	/*
	 * Initialize the class from the cursor.  The cursor position has been set prior to entry.
	 */
	public EqualizerData( Cursor cursor ) {
		int[] values = new int[] { _50Hz, _130Hz, _320Hz, _800Hz, _2Khz, _5Khz, _12Pt5Khz, _virtualizer, _bassBoost, _reverb };
		String[] keys = new String[] { DBAccessHelper.EQ_50_HZ, DBAccessHelper.EQ_130_HZ, DBAccessHelper.EQ_320_HZ, DBAccessHelper.EQ_800_HZ,
									   DBAccessHelper.EQ_2000_HZ, DBAccessHelper.EQ_5000_HZ, DBAccessHelper.EQ_12500_HZ, DBAccessHelper.VIRTUALIZER,
									   DBAccessHelper.BASS_BOOST, DBAccessHelper.REVERB };

		for( int x = 0; x < values.length; x++ ) {
			values[ x ] = cursor.getInt( cursor.getColumnIndex( keys[ x ] ));
		}
		// Verify code in for loop sets the member variables correctly (I should trust my coding skills...) then delete
		// the
//
//		_50Hz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_50_HZ));
//		_130Hz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_130_HZ));
//		_320Hz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_320_HZ));
//		_800Hz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_800_HZ));
//		_2Khz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_2000_HZ));
//		_5Khz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_5000_HZ));
//		_12Pt5Khz = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_12500_HZ));
//		_virtualizer = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.VIRTUALIZER));
//		_bassBoost = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.BASS_BOOST));
//		_reverb = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.REVERB));
	}

	/*
	 * Get the name associated with this data.  Used for presets.
	 */
	public String getName() {
		return _name;
	}

	public void setName( String name ) {
		_name = name;
	}

	public int get50Hz() {
		return _50Hz;
	}

	public int get130Hz() {
		return _130Hz;
	}

	public int get320Hz() {
		return _320Hz;
	}

	public int get800Hz() {
		return _800Hz;
	}

	public int get2Khz() {
		return _2Khz;
	}

	public int get5Khz() {
		return _5Khz;
	}

	public int get12Khz() {
		return _12Pt5Khz;
	}

	public int getVirtualizer() {
		return _virtualizer;
	}

	public int getBassBoost() {
		return _bassBoost;
	}

	public int getReverb() {
		return _reverb;
	}

	public void set50Hz( int i50Hz) {
		_50Hz = i50Hz;
	}

	public void set130Hz( int i130Hz) {
		_130Hz = i130Hz;
	}

	public void set320Hz( int i320Hz) {
		_320Hz = i320Hz;
	}

	public void set800Hz( int i800Hz ) {
		_800Hz = i800Hz;
	}

	public void set2Khz( int i2Khz ) {
		_2Khz = i2Khz;
	}

	public void set5Khz( int i5Khz ) {
		_5Khz = i5Khz;
	}

	public void set12Khz( int i12Khz ) {
		_12Pt5Khz = i12Khz;
	}

	public void setVirtualizer( int virtualizer ) {
		_virtualizer = virtualizer;
	}

	public void setBassBoost( int bassBoost ) {
		_bassBoost = bassBoost;
	}

	public void setReverb( int reverb ) {
		_reverb = reverb;
	}
}
