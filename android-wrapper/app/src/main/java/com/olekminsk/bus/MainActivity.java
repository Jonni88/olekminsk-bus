package com.olekminsk.bus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private static final String WEB_URL = "https://jonni88.github.io/olekminsk-bus/";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Открываем в Trusted Web Activity / Custom Tabs
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(false);
        builder.setUrlBarHidingEnabled(true);
        builder.setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK);
        
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.launchUrl(this, Uri.parse(WEB_URL));
        
        // Закрываем MainActivity после запуска
        finish();
    }
}
