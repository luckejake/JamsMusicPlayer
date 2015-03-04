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
package com.jams.music.player.AsyncTasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.jams.music.player.R;
import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.NowPlayingActivity.NowPlayingActivity;
import com.jams.music.player.Services.AudioPlaybackService;
import com.jams.music.player.Utils.Common;

public class AsyncAddToQueueTask extends AsyncTask<Boolean, Integer, Boolean> {

	private Context mContext;
	private Common mApp;

	private String mArtistName;
	private String mAlbumName;
	private String mSongTitle;
	private String mGenreName;
	private String mPlaylistId;
	private String mPlaylistName;
	private String mAlbumArtistName;

	private Fragment mFragment;
	private Cursor mCursor;
	private String mEnqueueType;
	private int originalPlaybackIndecesSize = 0;
	private boolean mPlayNext = false;
	private String mPlayingNext = "";

	private static final String _AND = " AND ";
	private static final String _OR = " OR ";

	private static final String _TRUE = " 'TRUE' ";
	private static final String _FALSE = " 'FALSE' ";

	private static final String _ASC = " ASC ";
	private static final String _DESC = " DESC ";

	private enum SortOrder {ASCENDING, DESCENDING}


	public AsyncAddToQueueTask(Context context,
							   Fragment fragment,
							   String enqueueType,
							   String artistName,
							   String albumName,
							   String songTitle,
							   String genreName,
							   String playlistId,
							   String playlistName,
							   String albumArtistName) {

		mContext = context;
		mApp = (Common) mContext;

		mArtistName = artistName;
		mAlbumName = albumName;
		mSongTitle = songTitle;
		mGenreName = genreName;
		mPlaylistId = playlistId;
		mPlaylistName = playlistName;
		mAlbumArtistName = albumArtistName;

		mFragment = fragment;
		mEnqueueType = enqueueType;

		if (mApp.getService().getPlaybackIndecesList() != null) {
			originalPlaybackIndecesSize = mApp.getService().getPlaybackIndecesList().size();
		}
	}

	protected void onPreExecute() {
		super.onPreExecute();
	}

	private String escapeQuote(String value) {

		if (value != null && value.contains("'")) {
			value = value.replace("'", "''");
		}

		return value;
	}

	@Override
	protected Boolean doInBackground(Boolean... params) {

		//Specifies if the user is trying to add song(s) to play next.
		if (params.length > 0) {
			mPlayNext = params[0];
		}

		String[] songData = new String[]{mArtistName, mAlbumName, mSongTitle, mGenreName, mAlbumArtistName};

		for (String song : songData) {
			song = escapeQuote(song); // todo: paranoid check. Make sure the member variables get set.
		}

		//Fetch the cursor based on the type of set of songs that are being enqueued.
		assignCursor();

		//Check if the service is currently active.
		if (mApp.isServiceRunning()) {
			if (mPlayNext) {
				/* Loop through the mCursor of the songs that will be enqueued and add the 
				 * loop's counter value to the size of the current mCursor. This will add 
				 * the additional mCursor indeces of the new, merged mCursor to playbackIndecesList. 
				 * The new indeces must be placed after the current song's index.
				 */
				int playNextIndex = 0;

				if (mApp.isServiceRunning()) {
					playNextIndex = mApp.getService().getCurrentSongIndex() + 1;
				}

				for (int i = 0; i < mCursor.getCount(); i++) {
					mApp.getService().getPlaybackIndecesList().add(playNextIndex + i,
							mApp.getService().getCursor().getCount() + i);
				}

			} else {
				/* Loop through the mCursor of the songs that will be enqueued and add the 
				 * loop's counter value to the size of the current mCursor. This will add 
				 * the additional mCursor indeces of the new, merged mCursor to playbackIndecesList.
				 */
				for (int i = 0; i < mCursor.getCount(); i++) {
					mApp.getService().getPlaybackIndecesList().add(mApp.getService().getCursor().getCount() + i);
				}
			}

			mApp.getService().enqueueCursor(mCursor, mPlayNext);
		} else {
			//The service doesn't seem to be running. We'll explicitly stop it, just in case, and then launch NowPlayingActivity.class.
			Intent serviceIntent = new Intent(mContext, AudioPlaybackService.class);
			mContext.stopService(serviceIntent);

			publishProgress(new Integer[]{0});
		}

		publishProgress(new Integer[]{1});

		return true;
	}

	private String selectAlbumName() {
		return DBAccessHelper.SONG_ALBUM + "= '" + mAlbumName + "' ";
	}

	private String selectSongTitle() {
		return DBAccessHelper.SONG_TITLE + " = '" + mSongTitle + "' ";
	}

	private String selectBlacklist(boolean status) {
		return DBAccessHelper.BLACKLIST_STATUS + " = " + (status ? _TRUE : _FALSE);
	}

	private String selectSongArtist() {
		return DBAccessHelper.SONG_ARTIST + " = '" + mArtistName + "' ";
	}

	private String selectAlbumArtistName() {
		return DBAccessHelper.SONG_ALBUM_ARTIST + " = " + " '" + mAlbumArtistName + "' ";
	}

	private String selectSongSource() {
		return DBAccessHelper.SONG_SOURCE + " <> 'GOOGLE_PLAY_MUSIC'";
	}

	public boolean isGooglePlayMusicEnabled() {
		return mApp.isGooglePlayMusicEnabled();
	}

	private String dbSongAlbum() {
		return DBAccessHelper.SONG_ALBUM;
	}

	private String dbSongTitle() {
		return DBAccessHelper.SONG_ALBUM;
	}

	private String dbString(SortOrder sortOrder) {
		return (sortOrder == SortOrder.ASCENDING ? _ASC : _DESC);
	}

	private String bySongAlbum(SortOrder sortOrder) {
		return dbSongAlbum() + dbString(sortOrder);
	}

	private String dbTrackNumber(String trackId, SortOrder sortOrder) {
		return DBAccessHelper.SONG_TRACK_NUMBER + "*" + trackId + dbString(sortOrder);
	}

	private void updateMusicLibraryTableCursor(DBAccessHelper dbHelper, String selection, String orderBy) {
		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE, null,
				selection, null, null, null, orderBy);
	}

	private String orderBySongTitle(SortOrder sortOrder) {
		return dbSongTitle() + dbString(sortOrder);
	}

	private String maybeGet(Conjunction conjunction, String defaultResult) {
		String result = null;

		switch (conjunction) {
			case AND:
				result = _AND;
				break;
			case OR:
				result = _OR;
				break;
		}

		return (result == null ? defaultResult : result);
	}

	/*
	 * Return a string that contains a mess...it's forming or growing.  Have to clean it up somewhere.
	 */
	private String maybeGetSongSource( String prefix ) {
		return (!isGooglePlayMusicEnabled() ? new String( prefix + selectSongSource() ) : "");
	}

	public enum Conjunction {AND, BUT, OR}


	private String constructSelectExpression( String[] conditions ) {
		String result = null;

		for (int x = 0; x < conditions.length; x++) {
			result = result + conditions[x];
		}

		return result;
	}

	private String selectSongExpression( boolean selectBlacklist ) {
		return constructSelectExpression( new String[] { selectSongArtist(), _AND, selectAlbumName(), _AND, selectSongTitle(),
												  		_AND, selectBlacklist( selectBlacklist ), maybeGetSongSource( _AND ) } );
	}

	private void prepSongMusicLibraryCursor(DBAccessHelper dbHelper) {
		// Select: song artist, album name, song title, blacklist (false), and maybe get song source, too:
		updateMusicLibraryTableCursor(dbHelper, selectSongExpression(false), orderBySongTitle(SortOrder.ASCENDING));

		mPlayingNext = mSongTitle;
	}

	private String selectSongArtistExpression(boolean selectBlacklist) {

		return null;
	}

	/*
	 * Select SONG_ARTIST and BLACKLIST.  Add in SONG_SOURCE if Google Play Music is not enabled.
	 */
	private void prepSongArtistMusicLibraryCursor(DBAccessHelper dbHelper) {
		String selection = selectSongArtist() + _AND + selectBlacklist(false);

		if (!isGooglePlayMusicEnabled()) {
			selection = selection + _AND + selectSongSource();
		}

		updateMusicLibraryTableCursor(dbHelper, selection, orderBySongTitle(SortOrder.ASCENDING));

		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE,
				null, selection, null, null, null,
				bySongAlbum(SortOrder.ASCENDING) + ", " + dbTrackNumber("1", SortOrder.ASCENDING));

		mPlayingNext = mArtistName;
	}

	private String selectAlbumMusicExpression( boolean selectBlacklist ) {
		return constructSelectExpression( new String[] { selectSongArtist(), _AND, selectAlbumName(),
														_AND, selectBlacklist( selectBlacklist ), maybeGetSongSource( _AND ) } );
	}

	private void prepAlbumMusicLibraryCursor(DBAccessHelper dbHelper) {
		mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE,
						null, selectAlbumMusicExpression( false ), null, null, null,
						DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");

		mPlayingNext = mAlbumName;
	}

	private String selectAlbumByAlbumArtistExpression( boolean selectBlacklist ) {
		return constructSelectExpression( new String[] { selectAlbumArtistName(), _AND, selectAlbumName(), _AND,
														 selectBlacklist( selectBlacklist ), _AND, maybeGetSongSource( _AND ) });
	}

	private void prepAlbumByAlbumArtist( DBAccessHelper dbHelper ) {
		mCursor = dbHelper.getReadableDatabase().query( DBAccessHelper.MUSIC_LIBRARY_TABLE, null, selectAlbumByAlbumArtistExpression( false ),
													    null, null, null, DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");

		mPlayingNext = mAlbumName;
	}

	//Retrieves and assigns the cursor based on the set of song(s) that are being enqueued.
	private void assignCursor() {
		DBAccessHelper dbHelper = new DBAccessHelper(mContext);

		if (mEnqueueType.equals("SONG")) {
			prepSongMusicLibraryCursor(dbHelper);
		}
		else if (mEnqueueType.equals("ARTIST")) {
			prepSongArtistMusicLibraryCursor(dbHelper);
		}
		else if (mEnqueueType.equals("ALBUM")) {
			prepAlbumMusicLibraryCursor(dbHelper);
		}
		else if (mEnqueueType.equals("ALBUM_BY_ALBUM_ARTIST")) {
			prepAlbumByAlbumArtist( dbHelper );
		} else if (mEnqueueType.equals("ALBUM_ARTIST")) {
			String selection = null;
			if (mApp.isGooglePlayMusicEnabled()) {
				selection = DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mAlbumArtistName + "'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			} else {
				selection = DBAccessHelper.SONG_ALBUM_ARTIST + "=" + "'" + mAlbumArtistName + "'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
						+ DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
			}

			mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE,
					null,
					selection,
					null,
					null,
					null,
					DBAccessHelper.SONG_ALBUM + " ASC, " + DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");

			mPlayingNext = mAlbumArtistName;
		} else if (mEnqueueType.equals("TOP_25_PLAYED")) {

			String selection = null;
			if (mApp.isGooglePlayMusicEnabled() == false) {
				selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			} else {
				selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			}

			mCursor = dbHelper.getTop25PlayedTracks(selection);
			mPlayingNext = mContext.getResources().getString(R.string.the_top_25_played_tracks);
		} else if (mEnqueueType.equals("RECENTLY_ADDED")) {
			String selection = null;
			if (mApp.isGooglePlayMusicEnabled() == false) {
				selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			} else {
				selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			}

			mCursor = dbHelper.getRecentlyAddedSongs(selection);
			mPlayingNext = mContext.getResources().getString(R.string.the_most_recently_added_songs);
		} else if (mEnqueueType.equals("TOP_RATED")) {
			String selection = null;
			if (mApp.isGooglePlayMusicEnabled() == false) {
				selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			} else {
				selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			}

			mCursor = dbHelper.getTopRatedSongs(selection);
			mPlayingNext = mContext.getResources().getString(R.string.the_top_rated_songs);
		} else if (mEnqueueType.equals("RECENTLY_PLAYED")) {
			String selection = null;
			if (mApp.isGooglePlayMusicEnabled() == false) {
				selection = DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			} else {
				selection = DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			}

			mCursor = dbHelper.getRecentlyPlayedSongs(selection);
			mPlayingNext = mContext.getResources().getString(R.string.the_most_recently_played_songs);
		} else if (mEnqueueType.equals("PLAYLIST")) {
           /* String selection = " AND " + DBAccessHelper.MUSIC_LIBRARY_PLAYLISTS_NAME + "." 
            				 + DBAccessHelper.PLAYLIST_ID + "=" + "'" + mPlaylistId + "'";
            
            if (mApp.isGooglePlayMusicEnabled()) {
            	mCursor = dbHelper.getAllSongsInPlaylistSearchable(selection);
            } else {
            	mCursor = dbHelper.getLocalSongsInPlaylistSearchable(selection);
            }
    		
            mPlayingNext = mPlaylistName;*/
		} else if (mEnqueueType.equals("GENRE")) {

			String selection = null;
			if (mApp.isGooglePlayMusicEnabled()) {
				selection = DBAccessHelper.SONG_GENRE + "=" + "'" + mGenreName + "'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'";
			} else {
				selection = DBAccessHelper.SONG_GENRE + "=" + "'" + mGenreName + "'" + " AND "
						+ DBAccessHelper.BLACKLIST_STATUS + "=" + "'FALSE'" + " AND "
						+ DBAccessHelper.SONG_SOURCE + "<>" + "'GOOGLE_PLAY_MUSIC'";
			}

			mCursor = dbHelper.getReadableDatabase().query(DBAccessHelper.MUSIC_LIBRARY_TABLE,
					null,
					selection,
					null,
					null,
					null,
					DBAccessHelper.SONG_ALBUM + " ASC, " +
							DBAccessHelper.SONG_TRACK_NUMBER + "*1 ASC");

			mPlayingNext = mGenreName;
		}


	}

	public Intent foo(Intent intent, String... kvPairs) {
		for (int x = 0; x < kvPairs.length; x += 2) {
			intent.putExtra(kvPairs[x], kvPairs[x + 1]);
		}

		return intent;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		int value = values[0];

		switch (value) {
			case 0:
				Intent intent = new Intent(mContext, NowPlayingActivity.class);

				//Get the parameters for the first song.
				if (mCursor.getCount() > 0) {
					mCursor.moveToFirst();

					int kvPairId = -1;
					Object[] intentExtras = null;

					String[] queueTypes = mContext.getResources().getStringArray(R.array.queue_types);

					int[] queueTypeIds = new int[]{R.array.progress_update_artist, R.array.progress_update_album_artist,
							R.array.progress_update_album, R.array.progress_update_playlist, R.array.progress_update_genre,
							R.array.progress_update_song, R.array.progress_update_album_by_album_artist};

					// Set the resource id of the array containing the key-value pairs to be stored in the extra of the intent:
					for (int x = 0; x < queueTypes.length; x++) {
						if (mEnqueueType.equals(queueTypes[x])) {
							kvPairId = queueTypeIds[x];
							intentExtras = mContext.getResources().getStringArray(kvPairId);

							// check if the extras requires extra data:
							if (queueTypes[x].equals("PLAYLIST")) {
								intentExtras[3] = mPlaylistName;
							} else if (queueTypes[x].equals("SONG")) {
								intentExtras[3] = true;
							}

							break;
						}
					}

					// todo: jake, move the strings into xml.

					String[] cursorColumnValues = new String[]{DBAccessHelper.SONG_DURATION, DBAccessHelper.SONG_TITLE, DBAccessHelper.SONG_ARTIST,
							DBAccessHelper.SONG_ALBUM, DBAccessHelper.SONG_ALBUM_ARTIST, DBAccessHelper.SONG_FILE_PATH,
							DBAccessHelper.SONG_GENRE};

					String[] cursorColumnKeys = new String[]{"SELECTED_SONG_DURATION", "SELECTED_SONG_TITLE", "SELECTED_SONG_ARTIST",
							"SELECTED_SONG_ALBUM", "SELECTED_SONG_ALBUM_ARTIST", "SELECTED_SONG_DATA_URI",
							"SELECTED_SONG_GENRE"};

					for (int x = 0; x < cursorColumnValues.length; x++) {
						intent.putExtra(cursorColumnKeys[x], mCursor.getString(mCursor.getColumnIndex(cursorColumnValues[x])));
					}

					intent.putExtra("NUMBER_SONGS", mCursor.getCount());

					intent.putExtra("SONG_SELECTED_INDEX", 0);
					intent.putExtra("NEW_PLAYLIST", true);
					intent.putExtra("CALLED_FROM_FOOTER", false);
					intent.putExtra(Common.CURRENT_LIBRARY, mApp.getCurrentLibrary());

				} else {
					Toast.makeText(mContext, R.string.error_occurred, Toast.LENGTH_LONG).show();
					break;
				}

				mFragment.getActivity().startActivity(intent);
				mFragment.getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				break;
			case 1:
				int numberOfSongs = mCursor.getCount();
				String toastMessage = "";

				if (numberOfSongs == 1) {
					if (mPlayNext) {
						toastMessage = mPlayingNext + " " + mContext.getResources().getString(R.string.will_be_played_next);
					} else {
						toastMessage = numberOfSongs + " " + mContext.getResources().getString(R.string.song_enqueued_toast);
					}

				} else {
					if (mPlayNext) {
						toastMessage = mPlayingNext + " " + mContext.getResources().getString(R.string.will_be_played_next);
					} else {
						toastMessage = numberOfSongs + " " + mContext.getResources().getString(R.string.songs_enqueued_toast);
					}

				}

				Toast.makeText(mContext, toastMessage, Toast.LENGTH_SHORT).show();

				break;
		}

	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		//Send out a broadcast that loads the new queue across the app.
		Intent intent = new Intent("com.jams.music.player.NEW_SONG_UPDATE_UI");
		intent.putExtra("MESSAGE", "com.jams.music.player.NEW_SONG_UPDATE_UI");
		intent.putExtra("INIT_QUEUE_DRAWER_ADAPTER", true);

		//Start preparing the next song if the current song is the last track.
		if (mApp.getService().getCurrentSongIndex() == (originalPlaybackIndecesSize - 1)) {

			//Check if the service is running.
			if (mApp.isServiceRunning()) {
				mApp.getService().prepareAlternateMediaPlayer();

			}

		}

	}

}
