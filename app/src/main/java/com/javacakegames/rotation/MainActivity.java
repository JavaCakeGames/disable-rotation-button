package com.javacakegames.rotation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MainActivity extends Activity {

  Context context;
  private TextView textBefore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textBefore = findViewById(R.id.textBefore);
    textBefore.setText(Html.fromHtml(getString(R.string.textBefore), Html.FROM_HTML_MODE_LEGACY));

    TextView tv = findViewById(R.id.textAfter);
    tv.setText(Html.fromHtml(getString(R.string.textAfter), Html.FROM_HTML_MODE_LEGACY));

    context = getApplicationContext();

    refresh(null);

  }

  public void refresh(View view) {
    String status = "";
    try {
      Process process = Runtime.getRuntime().exec("su");
      BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(process.getInputStream()));
      DataOutputStream os = new DataOutputStream(process.getOutputStream());
      os.writeBytes("settings get secure show_rotation_suggestions\n");
      os.writeBytes("exit\n");
      os.flush();
      status = bufferedReader.readLine();
      bufferedReader.close();
    } catch (Exception ignored) {}

    if (status == null) status = "e";
    switch(status) {
      case "0":
        status = "Disabled";
        break;
      case "1":
        status = "Enabled";
        break;
      default:
        status = "Unknown";
    }

    textBefore.setText(Html.fromHtml(getString(R.string.textBefore).replace("Unknown", status), Html.FROM_HTML_MODE_LEGACY));

  }

  public void disable(View view) {
    toggle(0);
  }

  public void enable(View view) {
    toggle(1);
  }

  private void toggle(int value) {

    textBefore.setText(Html.fromHtml(getString(R.string.textBefore).replace("Unknown", "Thinking"), Html.FROM_HTML_MODE_LEGACY));

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Process p = Runtime.getRuntime().exec("su");
          DataOutputStream os = new DataOutputStream(p.getOutputStream());
          os.writeBytes("settings put secure show_rotation_suggestions " + value + "\n");
          os.writeBytes("exit\n");
          os.flush();
          p.waitFor();
        } catch (Exception ignored) {}

        refresh(null);
      }
    }).start();

  }



}