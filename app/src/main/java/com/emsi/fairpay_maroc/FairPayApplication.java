package com.emsi.fairpay_maroc;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.emsi.fairpay_maroc.utils.LanguageHelper;

public class FairPayApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        // Apply the saved language before attaching the context
        String languageCode = LanguageHelper.getLanguage(base);
        super.attachBaseContext(LanguageHelper.updateLocale(base, languageCode));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Apply the saved language when configuration changes
        String languageCode = LanguageHelper.getLanguage(this);
        LanguageHelper.updateLocale(this, languageCode);
    }
}
