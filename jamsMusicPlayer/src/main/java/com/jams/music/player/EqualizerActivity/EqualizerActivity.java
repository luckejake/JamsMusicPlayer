/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jams.music.player.EqualizerActivity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.audiofx.PresetReverb;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jams.music.player.R;
import com.jams.music.player.AsyncTasks.AsyncApplyEQToAllSongsTask;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.Dialogs.EQAlbumsListDialog;
import com.jams.music.player.Dialogs.EQArtistsListDialog;
import com.jams.music.player.Dialogs.EQGenresListDialog;
import com.jams.music.player.Helpers.TypefaceHelper;
import com.jams.music.player.Helpers.UIElementsHelper;
import com.jams.music.player.Utils.Common;
import com.jams.music.player.Views.VerticalSeekBar;

public class EqualizerActivity extends FragmentActivity {

	// Equalizer values used in ChangeListeners.
	private final int HZ_50 =      50000;
	private final int HZ_130 =    130000;
	private final int HZ_320 =    320000;
	private final int HZ_800 =    800000;
	private final int KHZ_2 =    2000000;
	private final int KHZ_5 =    5000000;
	private final int KHZ_12_5 = 9000000;

	//Context.
	protected Context mContext;
	private Common mApp;
    private EqualizerActivity mFragment;

    //Equalizer container elements.
    private ScrollView mScrollView;

	// 50Hz equalizer controls.
	private VerticalSeekBar equalizer50HzSeekBar;
	private TextView text50HzGainTextView;
	private TextView text50Hz;

	// 130Hz equalizer controls.
	private VerticalSeekBar equalizer130HzSeekBar;
	private TextView text130HzGainTextView;
	private TextView text130Hz;

	// 320Hz equalizer controls.
	private VerticalSeekBar equalizer320HzSeekBar;
	private TextView text320HzGainTextView;
	private TextView text320Hz;

	// 800 Hz equalizer controls.
	private VerticalSeekBar equalizer800HzSeekBar;
	private TextView text800HzGainTextView;
	private TextView text800Hz;

	// 2 kHz equalizer controls.
	private VerticalSeekBar equalizer2kHzSeekBar;
	private TextView text2kHzGainTextView;
	private TextView text2kHz;

	// 5 kHz equalizer controls.
	private VerticalSeekBar equalizer5kHzSeekBar;
	private TextView text5kHzGainTextView;
	private TextView text5kHz;

	// 12.5 kHz equalizer controls.
	private VerticalSeekBar equalizer12_5kHzSeekBar;
	private TextView text12_5kHzGainTextView;
	private TextView text12_5kHz;

	private VerticalSeekBar[] equalizerSeekBars = new VerticalSeekBar[] { equalizer50HzSeekBar, equalizer130HzSeekBar,
			equalizer320HzSeekBar, equalizer800HzSeekBar, equalizer2kHzSeekBar, equalizer5kHzSeekBar, equalizer12_5kHzSeekBar };

	private TextView[] gainTextViews = new TextView[] { text50HzGainTextView, text130HzGainTextView, text320HzGainTextView,
			text800HzGainTextView, text2kHzGainTextView, text5kHzGainTextView, text12_5kHzGainTextView };

	private TextView[] textViews = new TextView[] { text50Hz, text130Hz, text320Hz, text800Hz, text2kHz, text5kHz, text12_5kHz };

	// Equalizer preset controls.
	private RelativeLayout loadPresetButton;
	private RelativeLayout saveAsPresetButton;
	private RelativeLayout resetAllButton;
	private TextView loadPresetText;
	private TextView savePresetText;
	private TextView resetAllText;

	// Temp variables that hold the equalizer's settings.
	private int fiftyHertzLevel = 16;
	private int oneThirtyHertzLevel = 16;
	private int threeTwentyHertzLevel = 16;
	private int eightHundredHertzLevel = 16;
	private int twoKilohertzLevel = 16;
	private int fiveKilohertzLevel = 16;
	private int twelvePointFiveKilohertzLevel = 16;

	// Temp variables that hold audio fx settings.
	private int virtualizerLevel;
	private int bassBoostLevel;
	private int reverbSetting;

	private EqualizerData _equalizerData;

	//Audio FX elements.
	private SeekBar virtualizerSeekBar;
	private SeekBar bassBoostSeekBar;
	private Spinner reverbSpinner;
	private TextView virtualizerTitle;
	private TextView bassBoostTitle;
	private TextView reverbTitle;

    //Misc flags.
    private boolean mDoneButtonPressed = false;

	public void initTextView( TextView textView, String fontFace, int paintFlags, int textColor ) {
		textView.setTypeface( TypefaceHelper.getTypeface( mContext, fontFace ));
		textView.setPaintFlags( textView.getPaintFlags() | paintFlags );
		textView.setTextColor( textColor );
	}

	public void initTextViews( TextView[] textViews, String fontFace, int paintFlags, int textColor ) {
		for( TextView textView : textViews ) {
			initTextView( textView, fontFace, textView.getPaintFlags() | paintFlags, textColor );
		}
	}

	public void loadControls() {
		//Equalizer container elements.
		mScrollView = (ScrollView) findViewById(R.id.equalizerScrollView);
		mScrollView.setBackgroundColor(UIElementsHelper.getBackgroundColor(mContext));

		int[] arrayDefinitions = new int[] { R.array.controls_50Hz, R.array.controls_130Hz, R.array.controls_320Hz,
				R.array.controls_800Hz, R.array.controls_2kHz, R.array.controls_5kHz, R.array.controls_12_5kHz };

		for( int x = 0; x < arrayDefinitions.length; x++ ) {
			// initEqualizerControl( arrayDefinitions[ x ], x );
			int[] defs = getResources().getIntArray( arrayDefinitions[ x ] );

			equalizerSeekBars[ x ] = (VerticalSeekBar) findViewById( defs[ 0 ] );
			gainTextViews[ x ] = (TextView) findViewById( defs[ 1 ] );
			textViews[ x ] = (TextView) findViewById( defs[ 2 ] );
		}

		//Equalizer preset controls.
		loadPresetButton = (RelativeLayout) findViewById(R.id.loadPresetButton);
		saveAsPresetButton = (RelativeLayout) findViewById(R.id.saveAsPresetButton);
		resetAllButton = (RelativeLayout) findViewById(R.id.resetAllButton);
		loadPresetText = (TextView) findViewById(R.id.load_preset_text);
		savePresetText = (TextView) findViewById(R.id.save_as_preset_text);
		resetAllText = (TextView) findViewById(R.id.reset_all_text);

		//Audio FX elements.
		virtualizerSeekBar = (SeekBar) findViewById(R.id.virtualizer_seekbar);
		bassBoostSeekBar = (SeekBar) findViewById(R.id.bass_boost_seekbar);
		reverbSpinner = (Spinner) findViewById(R.id.reverb_spinner);
		virtualizerTitle = (TextView) findViewById(R.id.virtualizer_title_text);
		bassBoostTitle = (TextView) findViewById(R.id.bass_boost_title_text);
		reverbTitle = (TextView) findViewById(R.id.reverb_title_text);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Context.
        mContext = getApplicationContext();
        mApp = (Common) mContext.getApplicationContext();
        mFragment = this;

        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_equalizer);

		loadControls();

		int paintFlags = Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG;
		int textColor = UIElementsHelper.getSmallTextColor( mContext );
		String fontFace = "RobotoCondensed-Regular";
		int allPaintFlags;

		initTextViews( gainTextViews, fontFace, paintFlags, textColor );
		initTextViews( textViews, fontFace, paintFlags, textColor );

        loadPresetText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Bold"));
        savePresetText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Bold"));
        resetAllText.setTypeface(TypefaceHelper.getTypeface(mContext, "RobotoCondensed-Bold"));

        loadPresetText.setPaintFlags(text50HzGainTextView.getPaintFlags() | paintFlags );
        savePresetText.setPaintFlags(text50HzGainTextView.getPaintFlags() | paintFlags );
        resetAllText.setPaintFlags(text50HzGainTextView.getPaintFlags() | paintFlags );

        //Init reverb presets.
        ArrayList<String> reverbPresets = new ArrayList<String>();

		// Todo: Move to strings.xml
		String[] reverbPresetNames = new String[] { "None", "Large Hall", "Large Room", "Medium Hall", "Medium Room", "Small Room", "Plate" };

		for( String presetName : reverbPresetNames ) {
			reverbPresets.add( presetName );
		}

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reverbPresets);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reverbSpinner.setAdapter(dataAdapter);

        //Set the max values for the seekbars.
        virtualizerSeekBar.setMax(1000);
        bassBoostSeekBar.setMax(1000);

		TextView[] titleTextViews = new TextView[] { virtualizerTitle, bassBoostTitle, reverbTitle };
		textColor = UIElementsHelper.getSmallTextColor( mContext );

		initTextViews( titleTextViews, fontFace, paintFlags, textColor );

        resetAllButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Reset all sliders to 0.
				int defaultProgressSetting = 16;

				for( int x = 0; x < equalizerSeekBars.length; x++ ) {
					equalizerSeekBars[ x ].setProgressAndThumb( defaultProgressSetting );
				}

                virtualizerSeekBar.setProgress(0);
                bassBoostSeekBar.setProgress(0);
                reverbSpinner.setSelection(0, false);

                //Apply the new setings to the service.
                applyCurrentEQSettings();

                //Show a confirmation toast.
                Toast.makeText(mContext, R.string.eq_reset, Toast.LENGTH_SHORT).show();
            }
        });

        loadPresetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                buildLoadPresetDialog().show();

            }

        });

        saveAsPresetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                buildSavePresetDialog().show();

            }

        });

		OnSeekBarChangeListener[] listeners = new OnSeekBarChangeListener[] { equalizer50HzListener, equalizer130HzListener,
				equalizer320HzListener, equalizer800HzListener, equalizer2kHzListener, equalizer5kHzListener, equalizer12_5kHzListener };

		for( int x = 0; x < equalizerSeekBars.length; x++ ) {
			equalizerSeekBars[ x ].setOnSeekBarChangeListener( listeners[ x ] );
		}

        virtualizerSeekBar.setOnSeekBarChangeListener(virtualizerListener);
        bassBoostSeekBar.setOnSeekBarChangeListener(bassBoostListener);
        reverbSpinner.setOnItemSelectedListener(reverbListener);

        //Get the saved equalizer settings and apply them to the UI elements.
        new AsyncInitSlidersTask().execute();

    }

    /**
     * Sets the activity theme based on the user preference.
     */
    private void setTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                setTheme(R.style.AppThemeNoTranslucentNav);
            }
			else {
                setTheme(R.style.AppThemeNoTranslucentNavLight);
            }
        }
		else {
            if (mApp.getCurrentTheme()==Common.DARK_THEME) {
                setTheme(R.style.AppTheme);
            }
			else {
                setTheme(R.style.AppThemeLight);
            }
        }
    }

	private String getDecibelSetting( int seekBarLevel ) {
		int v;

		if( seekBarLevel == 16 ) {
			v = 0;
		}
		else if( seekBarLevel < 16 ) {
			v = -1 * ( seekBarLevel == 0 ? 15 : ( 16 - seekBarLevel ));
		}
		else {
			v = seekBarLevel - 16;
		}

		return Integer.toString( v ) + " dB";
	}

	private short getBandLevel( int seekBarLevel ) {
		short v = 0; // seekBarLevel == 16 is 0

		if( seekBarLevel < 16 ) {
			v = (short)(-1 * ( seekBarLevel == 0 ? 1500 : ((16 - seekBarLevel) * 100)));
		}
		else if( seekBarLevel > 16 ) {
			v = (short)((seekBarLevel - 16) * 100 );
		}

		return v;
	}

	private void setEqualizerBandLevel( short bandLevel, short otherValue ) {
		mApp.getService().getEqualizerHelper().getCurrentEqualizer().setBandLevel( bandLevel, otherValue );
	}

	private short getEqualizerBandLevel( int bandValue ) {
		return mApp.getService().getEqualizerHelper().getCurrentEqualizer().getBand( bandValue );
	}

	private void updateGainListener( int seekBarLevel, int bandValue, TextView textView ) {
		textView.setText( getDecibelSetting( seekBarLevel ));
		setEqualizerBandLevel( getEqualizerBandLevel( bandValue ), (getBandLevel( seekBarLevel )));
	}

	private OnSeekBarChangeListener equalizer50HzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, HZ_50, text50HzGainTextView );
			fiftyHertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};

	private OnSeekBarChangeListener equalizer130HzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, HZ_130, text130HzGainTextView );
			oneThirtyHertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};

	private OnSeekBarChangeListener equalizer320HzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, HZ_320, text320HzGainTextView );
			threeTwentyHertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};

	private OnSeekBarChangeListener equalizer800HzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, HZ_800, text800HzGainTextView );
			eightHundredHertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};

	private OnSeekBarChangeListener equalizer2kHzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, KHZ_2, text2kHzGainTextView );
			twoKilohertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};

	private OnSeekBarChangeListener equalizer5kHzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, KHZ_5, text5kHzGainTextView );
			fiveKilohertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};

	private OnSeekBarChangeListener equalizer12_5kHzListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
			updateGainListener( seekBarLevel, KHZ_12_5, text12_5kHzGainTextView );
			twelvePointFiveKilohertzLevel = seekBarLevel;
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}
	};
	
	/**
	 * Spinner listener for reverb effects.
	 */
	private OnItemSelectedListener reverbListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {

			short[] presets = new short[] { PresetReverb.PRESET_NONE, PresetReverb.PRESET_LARGEHALL, PresetReverb.PRESET_LARGEROOM,
					PresetReverb.PRESET_MEDIUMHALL, PresetReverb.PRESET_MEDIUMROOM, PresetReverb.PRESET_SMALLROOM, PresetReverb.PRESET_PLATE };

			if (mApp.isServiceRunning()) {
				mApp.getService().getEqualizerHelper().getCurrentReverb().setPreset( presets[ index ] );
				reverbSetting = index;
			}
			else
				reverbSetting = 0;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
    };
    
    /**
     * Bass boost listener.
     */
    private OnSeekBarChangeListener bassBoostListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			mApp.getService().getEqualizerHelper().getCurrentBassBoost().setStrength((short) arg1);
			bassBoostLevel = (short) arg1;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
    };
    
    /**
     * Virtualizer listener.
     */
    private OnSeekBarChangeListener virtualizerListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			mApp.getService().getEqualizerHelper().getCurrentVirtualizer().setStrength((short) arg1);
			virtualizerLevel = (short) arg1;
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
    };
    
	/**
	 * Builds the "Save Preset" dialog. Does not call the show() method, so you 
	 * should do this manually when calling this method.
	 * 
	 * @return A fully built AlertDialog reference.
	 */
	private AlertDialog buildSavePresetDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.add_new_equalizer_preset_dialog_layout, null);
        
        final EditText newPresetNameField = (EditText) dialogView.findViewById(R.id.new_preset_name_text_field);
        newPresetNameField.setTypeface(TypefaceHelper.getTypeface(mContext, "Roboto-Light"));
        newPresetNameField.setPaintFlags(newPresetNameField.getPaintFlags() | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        
        //Set the dialog title.
        builder.setTitle(R.string.save_preset);
        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
				
			}
        	
        });
        
        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//Get the preset name from the text field.
				String presetName = newPresetNameField.getText().toString();
				
				//Add the preset and it's values to the DB.
				mApp.getDBAccessHelper().addNewEQPreset(presetName, 
									  				    fiftyHertzLevel, 
									  				    oneThirtyHertzLevel, 
									  				    threeTwentyHertzLevel, 
									  				    eightHundredHertzLevel, 
									  				    twoKilohertzLevel, 
									  				    fiveKilohertzLevel, 
									  				    twelvePointFiveKilohertzLevel, 
									  				    (short) virtualizerSeekBar.getProgress(), 
									  				    (short) bassBoostSeekBar.getProgress(), 
									  				    (short) reverbSpinner.getSelectedItemPosition());
				
				Toast.makeText(mContext, R.string.preset_saved, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
        	
        });

        return builder.create();
        
	}
	
	/**
	 * Builds the "Load Preset" dialog. Does not call the show() method, so this 
	 * should be done manually after calling this method.
	 * 
	 * @return A fully built AlertDialog reference.
	 */
	private AlertDialog buildLoadPresetDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Get a cursor with the list of EQ presets.
        final Cursor cursor = mApp.getDBAccessHelper().getAllEQPresets();
        
        //Set the dialog title.
        builder.setTitle(R.string.load_preset);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cursor.moveToPosition(which);
				
				//Close the dialog.
				dialog.dismiss();
				
				//Pass on the equalizer values to the appropriate fragment.
				fiftyHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_50_HZ));
				oneThirtyHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_130_HZ));
				threeTwentyHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_320_HZ));
				eightHundredHertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_800_HZ));
				twoKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_2000_HZ));
				fiveKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_5000_HZ));
				twelvePointFiveKilohertzLevel = cursor.getInt(cursor.getColumnIndex(DBAccessHelper.EQ_12500_HZ));
				virtualizerLevel = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.VIRTUALIZER));
				bassBoostLevel = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.BASS_BOOST));
				reverbSetting = cursor.getShort(cursor.getColumnIndex(DBAccessHelper.REVERB));

				cursor.moveToPosition( which );

				_equalizerData = new EqualizerData( cursor ); // todo: jake, see if there's a preset name.

				//Save the new equalizer settings to the DB.
				@SuppressWarnings({ "rawtypes" })
				AsyncTask task = new AsyncTask() {

					@Override
					protected Object doInBackground(Object... arg0) {
						setEQValuesForSong(mApp.getService().getCurrentSong().getId());
						return null;
					}
					
					@Override
					public void onPostExecute(Object result) {
						super.onPostExecute(result);
						
						//Reinitialize the UI elements to apply the new equalizer settings.
						new AsyncInitSlidersTask().execute();
					}
				};
				task.execute();

				if (cursor!=null)
					cursor.close();
				
			}
			
		}, DBAccessHelper.PRESET_NAME);

		return builder.create();

	}

	/**
	 * Builds the "Apply To" dialog. Does not call the show() method, so you 
	 * should do this manually when calling this method.
	 *
	 * @return A fully built AlertDialog reference.
	 */
	public AlertDialog buildApplyToDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//Set the dialog title.
		builder.setTitle(R.string.apply_to);
		builder.setCancelable(false);
		builder.setItems(R.array.apply_equalizer_to_array, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (which==0) {
					setEQValuesForSong(mApp.getService().getCurrentSong().getId());
					Toast.makeText(mContext, R.string.eq_applied_to_current_song, Toast.LENGTH_SHORT).show();

					//Finish this activity.
					finish();

				}
				else if (which==1) {
					AsyncApplyEQToAllSongsTask task = new AsyncApplyEQToAllSongsTask(mContext, mFragment);
					task.execute();
					dialog.dismiss();

					//Finish this activity.
					finish();

				}
				else if (which==2) {
					EQArtistsListDialog artistDialog = new EQArtistsListDialog();
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//					artistDialog.show( getSupportFragmentManager().beginTransaction(), getDialogName( artistDialog ) );
//					artistDialog.show( getSupportFragmentManager().beginTransaction(), "eqArtistsListDialog");
					artistDialog.show(ft, "eqArtistsListDialog");

					dialog.dismiss();

				}
				else if (which==3) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					EQAlbumsListDialog albumsDialog = new EQAlbumsListDialog();
					albumsDialog.show(ft, "eqAlbumsListDialog");

					dialog.dismiss();

				}
				else if (which==4) {
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					EQGenresListDialog genresDialog = new EQGenresListDialog();
					genresDialog.show(ft, "eqGenresListDialog");

					dialog.dismiss();

				}

			}

		});

		return builder.create();
	}
    
    /**
     * Saves the EQ settings to the database for the specified song.
     */
    public void setEQValuesForSong(String songId) {
    	
    	//Grab the EQ values for the specified song.
    	int[] currentEqValues = mApp.getDBAccessHelper().getSongEQValues(songId);
		
		//Check if a database entry already exists for this song.
		if (currentEqValues[10]==0) {
			//Add a new DB entry.
			mApp.getDBAccessHelper().addSongEQValues(songId, 
									 				 fiftyHertzLevel, 
									 				 oneThirtyHertzLevel, 
									 				 threeTwentyHertzLevel, 
									 				 eightHundredHertzLevel, 
									 				 twoKilohertzLevel, 
									 				 fiveKilohertzLevel,
									 				 twelvePointFiveKilohertzLevel,
									 				 virtualizerLevel, 
									 				 bassBoostLevel, 
									 				 reverbSetting);
		} else {
			//Update the existing entry.
			mApp.getDBAccessHelper().updateSongEQValues(songId, 
									 			   		fiftyHertzLevel, 
									 			   		oneThirtyHertzLevel, 
									 			   		threeTwentyHertzLevel, 
									 			   		eightHundredHertzLevel, 
									 			   		twoKilohertzLevel, 
									 			   		fiveKilohertzLevel, 
									 			   		twelvePointFiveKilohertzLevel, 
 									 			   		virtualizerLevel,
									 			   		bassBoostLevel, 
									 			   		reverbSetting);
		}
    }

    /**
     * Applies the current EQ settings to the service.
     */
    public void applyCurrentEQSettings() {
    	if (!mApp.isServiceRunning())
    		return;
		
		equalizer50HzListener.onProgressChanged(equalizer50HzSeekBar, equalizer50HzSeekBar.getProgress(), true);
		equalizer130HzListener.onProgressChanged(equalizer130HzSeekBar, equalizer130HzSeekBar.getProgress(), true);
		equalizer320HzListener.onProgressChanged(equalizer320HzSeekBar, equalizer320HzSeekBar.getProgress(), true);
		equalizer800HzListener.onProgressChanged(equalizer800HzSeekBar, equalizer800HzSeekBar.getProgress(), true);
		equalizer2kHzListener.onProgressChanged(equalizer2kHzSeekBar, equalizer2kHzSeekBar.getProgress(), true);
		equalizer5kHzListener.onProgressChanged(equalizer5kHzSeekBar, equalizer5kHzSeekBar.getProgress(), true);
		equalizer12_5kHzListener.onProgressChanged(equalizer12_5kHzSeekBar, equalizer12_5kHzSeekBar.getProgress(), true);
		
		virtualizerListener.onProgressChanged(virtualizerSeekBar, virtualizerSeekBar.getProgress(), true);
		bassBoostListener.onProgressChanged(bassBoostSeekBar, bassBoostSeekBar.getProgress(), true);
		reverbListener.onItemSelected(reverbSpinner, null, reverbSpinner.getSelectedItemPosition(), 0l);
    }
	
	/**
	 * Broadcast receiver that calls the methods that update the sliders with the 
	 * current song's EQ.
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
	    public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(Common.UPDATE_EQ_FRAGMENT)) {
				new AsyncInitSlidersTask().execute();
			}

            if (intent.hasExtra(Common.SERVICE_STOPPING)) {
                finish();
            }
		}
		
	};

    /**
     * Initializes the ActionBar.
     */
    private void showEqualizerActionBar(Menu menu) {

        //Set the Actionbar color.
        getActionBar().setBackgroundDrawable(UIElementsHelper.getGeneralActionBarBackground(mContext));

        //Hide all menu items except the toggle button and "done" icon.
        menu.findItem(R.id.action_equalizer).setVisible(false);
        menu.findItem(R.id.action_pin).setVisible(false);
        menu.findItem(R.id.action_queue_drawer).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_done).setVisible(true);

        /**
         * The Toggle button in the actionbar doesn't work at this point. The setChecked()
         * method doesn't do anything, so there's no way to programmatically set the
         * switch to its correct position when the equalizer fragment is first shown.
         * Users will just have to rely on the "Reset" button in the equalizer fragment
         * to effectively switch off the equalizer.
         */
        menu.findItem(R.id.action_equalizer_toggle).setVisible(false); //Hide the toggle for now.

		//Set the toggle listener.
		ToggleButton equalizerToggle = (ToggleButton) menu.findItem(R.id.action_equalizer_toggle)
									 		  			  .getActionView()
									 		  			  .findViewById(R.id.actionbar_toggle_switch);

		//Set the current state of the toggle.
		boolean toggleSetting = true;
		if (mApp.isEqualizerEnabled())
			toggleSetting = true;
		else
			toggleSetting = false;

        //Set the ActionBar title text color.
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView abTitle = (TextView) findViewById(titleId);
        abTitle.setTextColor(0xFFFFFFFF);

		equalizerToggle.setChecked(toggleSetting);
		equalizerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean state) {
				mApp.setIsEqualizerEnabled(state);

				if (state==true)
					applyCurrentEQSettings();
			}

		});

        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.now_playing, menu);

        showEqualizerActionBar(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_done:
                mDoneButtonPressed = true;
                buildApplyToDialog().show();
                return true;
            default:
                //Return false to allow the activity to handle the item click.
                return false;
        }
    }

    @Override
    public void onPause() {
    	super.onPause();

        //Save the EQ values for the current song.
        if (!mDoneButtonPressed) {
           setEQValuesForSong(mApp.getService().getCurrentSong().getId());
           Toast.makeText(mContext, R.string.eq_applied_to_current_song, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		//Initialize the broadcast manager that will listen for track changes.
    	LocalBroadcastManager.getInstance(mContext)
		 					 .registerReceiver((mReceiver), new IntentFilter(Common.UPDATE_UI_BROADCAST));
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		//Unregister the broadcast receivers.
    	LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
	}
	
    /**
    * Retrieves the saved equalizer settings for the current song 
    * and applies them to the UI elements.
    */
	public class AsyncInitSlidersTask extends AsyncTask<Boolean, Boolean, Boolean> {
		
		int[] eqValues;
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			eqValues = mApp.getDBAccessHelper()
				 	   .getSongEQValues(mApp.getService()
						 			  		.getCurrentSong()
						 			  		.getId());
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			_equalizerData = new EqualizerData( eqValues );

			// when _equalizerData is configured throughout this class, remove the following settings:
			fiftyHertzLevel = eqValues[0];
			oneThirtyHertzLevel = eqValues[1];
			threeTwentyHertzLevel = eqValues[2];
			eightHundredHertzLevel = eqValues[3];
			twoKilohertzLevel = eqValues[4];
			fiveKilohertzLevel = eqValues[5];
			twelvePointFiveKilohertzLevel = eqValues[6];
			virtualizerLevel = eqValues[7];
			bassBoostLevel = eqValues[8];
			reverbSetting = eqValues[9];

			VerticalSeekBar[] seekBars = new VerticalSeekBar[] { equalizer50HzSeekBar, equalizer130HzSeekBar, equalizer320HzSeekBar,
					equalizer800HzSeekBar, equalizer2kHzSeekBar, equalizer5kHzSeekBar, equalizer12_5kHzSeekBar };

			for( int x = 0; x < seekBars.length; x++ ) {
				seekBars[ x ].setProgressAndThumb( eqValues[ x ] );
			}

			virtualizerSeekBar.setProgress( _equalizerData.getVirtualizer() );
			bassBoostSeekBar.setProgress( _equalizerData.getBassBoost() );
			reverbSpinner.setSelection( _equalizerData.getReverb(), false );

			drawEqualizerValue( text50HzGainTextView, _equalizerData.get50Hz() );
			drawEqualizerValue( text130HzGainTextView, _equalizerData.get130Hz() );
			drawEqualizerValue( text320HzGainTextView, _equalizerData.get320Hz() );
			drawEqualizerValue( text800HzGainTextView, _equalizerData.get800Hz() );
			drawEqualizerValue( text2kHzGainTextView, _equalizerData.get2Khz() );
			drawEqualizerValue( text5kHzGainTextView, _equalizerData.get5Khz() );
			drawEqualizerValue( text12_5kHzGainTextView, _equalizerData.get12Khz() );
		}

		private void drawEqualizerValue( TextView textView, int eqLevel ) {
			int value = -1;
			String sign = ""; // default for eqLevel == 16, which is 0 dB.

			if( eqLevel == 16 ) {
				value = 0;
			}
			else if ( eqLevel < 16 ) {
				value = ( eqLevel == 0 ? 15 : 16 - eqLevel );
				sign = "-";
			}
			else if ( eqLevel > 16 ) {
				value = eqLevel - 16;
				sign = "+";
			}

			textView.setText( sign + value + " dB" );
		}
	}

    /**
     * Getter methods.
     */

	public EqualizerData getEqualizerData() {
		return _equalizerData;
	}

	public void setEqualizerData( EqualizerData equalizerData ) {
		_equalizerData = equalizerData;
	}

	public int getFiftyHertzLevel() {
		return fiftyHertzLevel;
	}

	public int getOneThirtyHertzLevel() {
		return oneThirtyHertzLevel;
	}

	public int getThreeTwentyHertzLevel() {
		return threeTwentyHertzLevel;
	}

	public int getEightHundredHertzLevel() {
		return eightHundredHertzLevel;
	}

	public int getTwoKilohertzLevel() {
		return twoKilohertzLevel;
	}

	public int getFiveKilohertzLevel() {
		return fiveKilohertzLevel;
	}

	public int getTwelvePointFiveKilohertzLevel() {
		return twelvePointFiveKilohertzLevel;
	}

	public int getVirtualizerLevel() {
		return virtualizerLevel;
	}

	public int getBassBoostLevel() {
		return bassBoostLevel;
	}
	
	public SeekBar getVirtualizerSeekBar() {
		return virtualizerSeekBar;
	}
	
	public SeekBar getBassBoostSeekBar() {
		return bassBoostSeekBar;
	}
	
	public Spinner getReverbSpinner() {
		return reverbSpinner;
	}

	/**
	 * Setter methods.
	 */
	
	public void setFiftyHertzLevel(int fiftyHertzLevel) {
		this.fiftyHertzLevel = fiftyHertzLevel;
	}

	public void setOneThirtyHertzLevel(int oneThirtyHertzLevel) {
		this.oneThirtyHertzLevel = oneThirtyHertzLevel;
	}

	public void setThreeTwentyHertzLevel(int threeTwentyHertzLevel) {
		this.threeTwentyHertzLevel = threeTwentyHertzLevel;
	}

	public void setEightHundredHertzLevel(int eightHundredHertzLevel) {
		this.eightHundredHertzLevel = eightHundredHertzLevel;
	}

	public void setTwoKilohertzLevel(int twoKilohertzLevel) {
		this.twoKilohertzLevel = twoKilohertzLevel;
	}

	public void setFiveKilohertzLevel(int fiveKilohertzLevel) {
		this.fiveKilohertzLevel = fiveKilohertzLevel;
	}

	public void setTwelvePointFiveKilohertzLevel(int twelvePointFiveKilohertzLevel) {
		this.twelvePointFiveKilohertzLevel = twelvePointFiveKilohertzLevel;
	}

	public void setVirtualizerLevel(int virtualizerLevel) {
		this.virtualizerLevel = virtualizerLevel;
	}

	public void setBassBoostLevel(int bassBoostLevel) {
		this.bassBoostLevel = bassBoostLevel;
	}
}
