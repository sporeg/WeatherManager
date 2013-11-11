package com.sporeg.weathermanager;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "com.sporeg.weathermanager.SearchSuggestionProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public SearchSuggestionProvider() {
		super();
		setupSuggestions(AUTHORITY, MODE);
	}
}
