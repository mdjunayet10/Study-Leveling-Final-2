//app
package org.app;

import firebase.FirebaseConfig;
import ui.LoginScreen;

public class App {
    public static void main(String[] args) {
// Initialize Firebase first
        FirebaseConfig.initialize();

        // Then open the app UI
        new LoginScreen();
    }
}