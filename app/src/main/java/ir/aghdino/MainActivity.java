package ir.aghdino;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Erfan Akhavan Rad
 */

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    Context context = this;
    DrawerLayout drawer_menu;
    RecyclerView rv_drawer_menu;
    ImageView imv_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        if (!isConnected(this)) {
            showCustomDialogue();
            webView.destroy();
        }

        webView.getSettings().getJavaScriptEnabled();
        webView.setWebViewClient(new CustomWebViewClient());
//        webView.loadUrl("https://Peyvandeyaran.ir/aghdino");
//        webView.loadUrl("https://google.com");
        webView.loadUrl("https://peyvandeyaran.ir/aqdino");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        initView();
        onClick();


    }

    private void showCustomDialogue() {
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.nett);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(image)
                .setMessage("                 شما به اینترنت متصل نیستید")

                .setCancelable(false)
                .setPositiveButton("تنظیمات", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("خروج", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                })
                .create().show();
    }

    private boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConnection = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiConnection != null && wifiConnection.isConnected() || mobileConnection != null && mobileConnection.isConnected())
            return true;
        return false;
    }


    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Toast.makeText(MainActivity.this, "در حال بارگزاری...", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            Toast.makeText(MainActivity.this, "Finish", Toast.LENGTH_SHORT).show();
        }


    }

    private void onClick() {
        imv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_menu.openDrawer(Gravity.RIGHT);
            }
        });
    }

    private void initView() {
        drawer_menu = findViewById(R.id.drawer_menu);
        rv_drawer_menu = findViewById(R.id.rv_drawer_menu);
        imv_menu = findViewById(R.id.imv_menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupDrawer();
    }

    private void setupDrawer() {
        TextView author;
        author = findViewById(R.id.author);
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mailto = "mailto:erfanakhavanrad@yahoo.com" +
                        "?cc=" +
                        "&subject=" + Uri.encode("Work Collaboration") +
                        "&body=" + Uri.encode("");
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "Error to open email app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rv_drawer_menu.setLayoutManager(new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        rv_drawer_menu.setAdapter(new NavDrawerRVAdapter(context).setOnItemClickListener(new NavDrawerRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, NavDrawerRVAdapter.DrawerItem item) {
                drawer_menu.closeDrawer(Gravity.RIGHT);
                if (item.getOutput().equals("")) {
                    Toast.makeText(context, "در حال توسعه", Toast.LENGTH_LONG).show();
                    return;
                }
                switch (item.getType()) {

                    case NavDrawerRVAdapter.ACTIVITY_TYPE:

                        drawer_menu.closeDrawer(Gravity.RIGHT);

                        try {
                            Class<?> classType = Class.forName(item.getOutput());
                            startActivity(new Intent(context, classType));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;

                    case NavDrawerRVAdapter.SHARE_TYPE:
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_SUBJECT, "موضوع");
                        share.putExtra(Intent.EXTRA_TEXT, item.getOutput());
                        context.startActivity(Intent.createChooser(share, "عنوان"));
                        break;

                    case NavDrawerRVAdapter.CALL_TYPE:
                        Intent call = new Intent(Intent.ACTION_DIAL);
                        call.setData(Uri.parse("tel:" + item.getOutput()));
                        context.startActivity(call);
                        break;

                    case NavDrawerRVAdapter.OPEN_SOCIAL_MEDIA:
                        socialCheck(item.getOutput());
                        break;

                    case NavDrawerRVAdapter.CLOSE_APP:
//                            finish();
                        System.exit(0);
                        break;

                }
            }
        }));
    }

    public void socialCheck(String output) {
        if (output.contains("instagram")) {
            Uri uri = Uri.parse(output);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/xxx")));
            }


        } else if (output.contains("pin")) {
            String url = output;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        } else if (output.contains("wa")) {
            String url = output;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else Toast.makeText(context, "unknown", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (drawer_menu.isDrawerOpen(Gravity.RIGHT)) {
            drawer_menu.closeDrawer(Gravity.RIGHT);
        } else {
            finish();
        }
    }
}