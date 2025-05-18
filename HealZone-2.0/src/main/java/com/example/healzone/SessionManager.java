package com.example.healzone;

public class SessionManager {
    private static boolean loggedIn = false;
    private static String currentUser;

    public static String isLoggedIn() {

        if(loggedIn){
            return getCurrentUser();
        }else{
            return null;
        }
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void logIn(String user) {
        loggedIn = true;
        currentUser = user;
    }

    public static void logOut() {
        loggedIn = false;
        currentUser = null;
    }
}
