package com.aaa.dflix_live;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    WebView webView;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    private String webUrl = "http://dflix.live/";


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        webView = findViewById( R.id.webD );
        progressBar = findViewById( R.id.progressBarD );
        swipeRefreshLayout = findViewById( R.id.sRefreshD );
        WebSettings webSettings= webView.getSettings();
        webSettings.setJavaScriptEnabled( true );
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.setSoundEffectsEnabled(true);
        webView.setScrollbarFadingEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSaveFormData(true);
      //  webSettings.setLayoutAlgorithm( WebSettings.LayoutAlgorithm.NARROW_COLUMNS );
        webSettings.setRenderPriority( WebSettings.RenderPriority.HIGH );
        webSettings.setEnableSmoothTransition( true );
        webView.getSettings().setLightTouchEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            webSettings.setLayoutAlgorithm( WebSettings.LayoutAlgorithm.NORMAL );
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
           webSettings.setLayoutAlgorithm( WebSettings.LayoutAlgorithm.NARROW_COLUMNS );
//            webSettings.setRenderPriority( WebSettings.RenderPriority.HIGH );
//            webSettings.setEnableSmoothTransition( true );
//            webView.getSettings().setLightTouchEnabled(true);

        }

        webView.setWebChromeClient(new MyChrome());
        webView.setWebViewClient( new Callback() );
        if (savedInstanceState == null) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(webUrl);
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing( true );
                new Handler().postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing( false );
                        webView.loadUrl(webUrl);

                    }
                }, 3000);
            }
        } );
        swipeRefreshLayout.setColorSchemeColors(

                getResources().getColor( R.color.colorPrimaryDark ),
                getResources().getColor( R.color.colorPrimary ),
                getResources().getColor( R.color.colorAccent )
        );
        webView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (webView.getScrollY() == 0) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });


        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if(checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

                        Log.d("permission","permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions,1);
                    }else{

                        DownloadManager.Request request = new DownloadManager.Request( Uri.parse( url ) );
                        request.setMimeType( mimeType );
                        String cookies = CookieManager.getInstance().getCookie( url );
                        request.addRequestHeader( "cookie", cookies );
                        request.addRequestHeader( "User-Agent", userAgent );
                        request.setDescription( "Downloading file...." );
                        request.setTitle( URLUtil.guessFileName( url, contentDisposition, mimeType ) );
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED );
                        request.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName( url, contentDisposition, mimeType ) );
                        DownloadManager dm = (DownloadManager) getSystemService( DOWNLOAD_SERVICE );
                        dm.enqueue( request );
                        Toast.makeText( getApplicationContext(), "Downloading File", Toast.LENGTH_SHORT ).show();
                    }
                }
                else {

                    DownloadManager.Request request = new DownloadManager.Request( Uri.parse( url ) );
                    request.setMimeType( mimeType );
                    String cookies = CookieManager.getInstance().getCookie( url );
                    request.addRequestHeader( "cookie", cookies );
                    request.addRequestHeader( "User-Agent", userAgent );
                    request.setDescription( "Downloading file...." );
                    request.setTitle( URLUtil.guessFileName( url, contentDisposition, mimeType ) );
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED );
                    request.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName( url, contentDisposition, mimeType ) );
                    DownloadManager dm = (DownloadManager) getSystemService( DOWNLOAD_SERVICE );
                    dm.enqueue( request );
                    Toast.makeText( getApplicationContext(), "Downloading File", Toast.LENGTH_SHORT ).show();
                }


            }
        });



    }
    private class Callback extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted( view, url, favicon );
            progressBar.setVisibility( View.VISIBLE );

        }
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            webUrl=url;
//            return super.shouldOverrideUrlLoading(view, url);
//        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished( view, url );
            webUrl=url;
            progressBar.setVisibility( View.GONE );
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
          //  Toast.makeText( MainActivity.this, "Something went wrong. Swipe to refresh.", Toast.LENGTH_SHORT ).show();
        }
    }
    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {}

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    protected void exitBackKey(){
//        AlertDialog alertDialog = new AlertDialog.Builder(this)
//                .setMessage( "Are you sure you want to Exit?" )
//                .setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        System.exit( 0 );
//                    }
//                } )
//                .setNegativeButton( "No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                } ).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // finish();
                System.exit( 0 );
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
        }
        else {
            exitBackKey();
          //  super.onBackPressed();
        }

        }

}