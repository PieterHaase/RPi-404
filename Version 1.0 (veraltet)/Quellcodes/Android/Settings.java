package haw_hamburg.rpi_404;

//=================================================================================================================//
// Projekt: RPi-404         Author: Pieter Haase                                                                   //
//-----------------------------------------------------------------------------------------------------------------//
// In dieser abstrakten Klasse werden Einstellungen wie Schriftgrößen, Linienstärken, und Farbschemata gespeichert //
//=================================================================================================================//

public abstract class Settings {

    public static boolean testMode = false;
    public static int frameMargin = 30;

    public static int lineWidthFine = 2;
    public static int lineWidthBold = 12;
    public static int textSizeSmall = 12;
    public static int textSizeBig = 14;

    public static boolean piWiFiMode = true;        // Gibt an ob die App den Videostream von der WiFi- oder LAN-IP des Raspberry Pi beziehen soll


    // Verschiedene Farbschemata, die erstellt wurden; können durch Ein-/Auskommentieren gewechselt werden
/*
    // THEME 1 - Sci Fi -----------------------------
    public static String backgroundColor = "#324452";
    public static String mainColor = "#CDE5E5";
    public static String accent1Color = "#3A6575";
    public static String accent2Color = "#AD3000";
    public static String accent3Color = "#E79353";
    // ----------------------------------------------
*/
/*
    // THEME 2 - Watch Dog --------------------------
    public static String backgroundColor = "#090911";
    public static String mainColor = "#CCCCCC";
    public static String accent1Color = "#14A098";
    public static String accent2Color = "#FFFFFF";
    public static String accent3Color = "#CB2D6F";
    // ----------------------------------------------
*/
/*
    // THEME 3 - Neon -------------------------------
    public static String backgroundColor = "#0B0C10";
    public static String mainColor = "#C5C6C7";
    public static String accent1Color = "#1F2833";
    public static String accent2Color = "#66FCF1";
    public static String accent3Color = "#45A29E";
    // ----------------------------------------------
*/
/*
    // THEME 4 - Marine -----------------------------       (nicht ganz so gut Ablesbar)
    public static String backgroundColor = "#5AB9EA";
    public static String mainColor = "#FFFFFF";
    public static String accent1Color = "#84CEEB";
    public static String accent2Color = "#8860D0";
    public static String accent3Color = "#5680E9";
    // ----------------------------------------------
*/
/*
    // THEME 5 - The Matrix -------------------------
    public static String backgroundColor = "#000000";
    public static String mainColor = "#6B6E70";
    public static String accent1Color = "#61892F";
    public static String accent2Color = "#FFFFFF";
    public static String accent3Color = "#FFFFFF";
    // ----------------------------------------------
*/
/*
    // THEME 6 - Fresh ------------------------------       (nicht ganz so gut Ablesbar)
    public static String backgroundColor = "#FFFFFF";
    public static String mainColor = "#00ADC8";
    public static String accent1Color = "#8CBD02";
    public static String accent2Color = "#41C6CD";
    public static String accent3Color = "#ADE838";
    // ----------------------------------------------
*/
/*
    // THEME 7 - Deep Blue --------------------------
    public static String backgroundColor = "#173F49";
    public static String mainColor = "#7D7D7B";
    public static String accent1Color = "#4FACBB";
    public static String accent2Color = "#327482";
    public static String accent3Color = "#FFFFFF";
    // ----------------------------------------------
*/
/*
    // THEME 8 - Starship ---------------------------
    public static String backgroundColor = "#101215";
    public static String mainColor = "#D1E8E2";
    public static String accent1Color = "#116466";
    public static String accent2Color = "#FFFFFF";
    public static String accent3Color = "#FFCB9A";
    // ----------------------------------------------
*/
/*
    // THEME 9 - Horizon ----------------------------
    public static String backgroundColor = "#01405F";
    public static String mainColor = "#FFFFFF";
    public static String accent1Color = "#54D1EF";
    public static String accent2Color = "#FED20D";
    public static String accent3Color = "#C27740";
    // ----------------------------------------------
*/
/*
    // THEME 10 - Navy ------------------------------
    public static String backgroundColor = "#333945";
    public static String mainColor = "#F6F7F9";
    public static String accent1Color = "#3F6184";
    public static String accent2Color = "#778898";
    public static String accent3Color = "#C27740";
    // ----------------------------------------------
*/

    // THEME 11 - Techno ----------------------------
    public static String backgroundColor = "#333945";
    public static String mainColor = "#F6F7F9";
    public static String accent1Color = "#B2D145";
    public static String accent2Color = "#5AB9EA";
    public static String accent3Color = "#CB2D6F";
    // ----------------------------------------------

}
