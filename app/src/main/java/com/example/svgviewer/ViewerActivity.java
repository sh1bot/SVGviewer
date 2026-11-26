package com.example.svgviewer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public final class ViewerActivity extends Activity {
    private static final String PAGE_URL = "https://svg-viewer.invalid/";
    private static final String IMAGE_URL = PAGE_URL + "image.svg";
    private static final String PAGE = """
            <!doctype html>
            <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=8">
            <meta http-equiv="Content-Security-Policy"
                  content="default-src 'none'; img-src 'self'; style-src 'unsafe-inline'">
            <title>SVG Viewer</title>
            <style>
                body { margin: 0; }
                img { display: block; width: 100%; height: 100vh; object-fit: contain; }
            </style>
            <img src="image.svg" alt="Unable to display this SVG">
            """;

    private WebView viewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        if (!Intent.ACTION_VIEW.equals(getIntent().getAction())
                || uri == null || !"content".equals(uri.getScheme())) {
            TextView message = new TextView(this);
            message.setGravity(Gravity.CENTER);
            message.setText("Open an SVG from your file manager.");
            setContentView(message);
            return;
        }

        viewer = new WebView(this);
        WebSettings settings = viewer.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        viewer.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (request.isForMainFrame() && PAGE_URL.equals(url)) {
                    return new WebResourceResponse("text/html", "UTF-8",
                            new ByteArrayInputStream(PAGE.getBytes(StandardCharsets.UTF_8)));
                }

                // Render the document as an image so SVG scripts and external resources stay disabled.
                if (!request.isForMainFrame() && IMAGE_URL.equals(url)) {
                    try {
                        InputStream svg = getContentResolver().openInputStream(uri);
                        if (svg != null) {
                            return new WebResourceResponse("image/svg+xml", null, svg);
                        }
                    } catch (IOException | SecurityException ignored) {
                        // The provider may have removed the file or revoked access.
                    }
                    runOnUiThread(() -> {
                        if (!isDestroyed()) {
                            Toast.makeText(ViewerActivity.this, "Unable to read the SVG", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
                        Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
        });
        setContentView(viewer);
        viewer.loadUrl(PAGE_URL);
    }

    @Override
    protected void onDestroy() {
        if (viewer != null) {
            ((ViewGroup) viewer.getParent()).removeView(viewer);
            viewer.destroy();
        }
        super.onDestroy();
    }
}
