package haw_hamburg.rpi_404;

import android.graphics.Color;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//======================================================================//
// Projekt: RPi-404         Author: Pieter Haase                        //
//----------------------------------------------------------------------//
// Diese Klasse dient dazu den Videostream in einen Webview einzubinden //
//======================================================================//

public class VideoView {

    private MainActivity mainActivity;
    private WebView webView;
    private WebViewClient webViewClient;
    private String url;

    public VideoView(MainActivity mainActivity, WebView webView){
        this.mainActivity = mainActivity;
        this.webView = webView;

        webViewClient = new WebViewClient();
        webView.setWebViewClient(webViewClient);

        webView.setBackgroundColor(Color.parseColor(Settings.backgroundColor));
        Log.i("VIDSTREAM", "opening URL");

        if(Settings.testMode){
            url = "http://200.36.58.250/mjpg/video.mjpg";       // Im TestMode wird ein Webcam Feed aus dem Internet geöffnet
        }
        else{
            if(Settings.piWiFiMode){
                Log.i("VIDSTREAM", "WIFI");
                url = "http://192.168.0.194:5000/";             // Wenn Pi über WiFi verbunden ist, WiFi-IP-Adresse benutzen
                //url = "http://www.google.de";
            }
            else
                url = "http://192.168.0.193:5000/";             // Sonst LAN-IP-Adresse benutzen
        }

        // Einbinden des Videostreams über HTML
        //String html = "<html><body style=\"margin:0 ; padding:0\"><img src=\"" + url + "\" width=\"100%\" \"/ ></body></html>";
        //webView.loadData(url, "text/html", null);
        webView.loadUrl(url);
        /*
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if(url.equals("http://192.168.0.193:5000/"))
                        url = "http://192.168.0.194:5000/";
                else
                    url = "http://192.168.0.193:8081/";
                String html = "<html><body style=\"margin:0 ; padding:0\"><img src=\"" + url + "\" width=\"100%\" \"/ ></body></html>";
                webView.loadData(html, "text/html", null);
            }
        });*/
    }

    public WebView getView(){
        return webView;
    }
}
