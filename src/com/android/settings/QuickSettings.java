package com.android.settings;

import android.os.Handler;
import android.os.Bundle;
import android.content.Context;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.provider.Settings;
import android.os.UserHandle;
import android.net.Uri;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.tequila.support.preferences.SystemSettingListPreference;
import com.android.internal.util.tequila.ThemeUtils;


@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    
    public static final String TAG = "QuickSettings";

    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";
    
    private Handler mHandler;
    private ThemeUtils mThemeUtils;
    private SystemSettingListPreference mQsStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mThemeUtils = new ThemeUtils(getActivity());
        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mCustomSettingsObserver.observe();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQsStyle) {
            mCustomSettingsObserver.observe();
            return true;
        }
        
        return false;
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
        }
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(), Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);
        switch (qsPanelStyle) {
            case 0:
              setQsStyle("com.android.systemui");
              break;
            case 1:
              setQsStyle("com.android.system.qs.twotoneaccent");
              break;
            default:
              break;
        }
    }

    public void setQsStyle(String overlayName) {
        mThemeUtils.setOverlayEnabled("android.theme.customization.qs_panel", overlayName, "com.android.systemui");
    }

    @Override
    public int getMetricsCategory() {
        return 0;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_settings);
}