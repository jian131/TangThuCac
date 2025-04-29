package com.jian.tangthucac;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase persistence before any other Firebase usage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
