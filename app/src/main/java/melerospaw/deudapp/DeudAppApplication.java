package melerospaw.deudapp;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import melerospaw.deudapp.preferences.SharedPreferencesManager;

public class DeudAppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);

        // TODO: 02/12/2018 Delete in following versions
        // Fix for 1.2.1 bug on Currency key change
        new SharedPreferencesManager(this).applyFix();
    }
}
