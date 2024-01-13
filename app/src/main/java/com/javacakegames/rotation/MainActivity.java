package com.javacakegames.rotation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

  // todo: detect root before detecting if getting/setting rotation fails

  private Context context;
  private TextView textBefore;
  private boolean couldNotFetchShown;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textBefore = findViewById(R.id.textBefore);

    TextView tv = findViewById(R.id.textAfter);
    tv.setText(Html.fromHtml(getString(R.string.textAfter), Html.FROM_HTML_MODE_COMPACT));

    context = getApplicationContext();

    refresh(null, null);

  }

  private int refresh(BufferedReader br, DataOutputStream os) {
    String status = "";
    try {
      if (br == null & os == null) {
        Process process = Runtime.getRuntime().exec("su");
        br = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
        os = new DataOutputStream(process.getOutputStream());
      }
      os.writeBytes("settings get secure show_rotation_suggestions\n");
      os.writeBytes("exit\n");
      os.flush();
      status = br.readLine();
      br.close();
    } catch (Exception ignored) {
      if (!couldNotFetchShown) {
        alert(getString(R.string.couldNotFetchTitle),
              getString(R.string.couldNotFetchMessage));
      }
      couldNotFetchShown = true;
    }

    if (status == null) status = "e";
    final int statusCode;
    switch(status) {
      case "0":
        status = "Disabled";
        statusCode = 0;
        break;
      case "1":
        status = "Enabled";
        statusCode = 1;
        break;
      default:
        status = "Unknown";
        statusCode = -1;
    }

    displayStatus(status);

    return statusCode;

  }

  public void disable(View view) {
    toggle(0);
  }

  public void enable(View view) {
    toggle(1);
  }

  public void displayStatus(String status) {
    textBefore.setText(Html.fromHtml(getString(R.string.textBefore)
      .replace("Unknown", status), Html.FROM_HTML_MODE_LEGACY));
  }

  private void toggle(int value) {

    displayStatus("Thinking");

    new Thread(() -> {
      try {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(p.getOutputStream());
        BufferedReader br = new BufferedReader(
          new InputStreamReader(p.getInputStream()));
        os.writeBytes("settings put secure show_rotation_suggestions " + value + "\n");
        refresh(br, os);
        //os.writeBytes("exit\n");
        //os.flush();
        //p.waitFor();
      } catch (Exception ignored) {
        refresh(null, null);
      }

    }).start();

  }

  private void alert(String title, String message) {
    runOnUiThread(() -> {
      if (!isFinishing()){
        new AlertDialog.Builder(MainActivity.this)
          .setTitle(title)
          .setMessage(message)
          .setCancelable(true)
          .setPositiveButton("\uD83D\uDE41", (dialog, which) -> {

          }).show();
      }
    });
  }

  @Override // Fix for Android 10 memory leak
  public void onBackPressed() {
    Toast.makeText(context, getString(R.string.goodbye), Toast.LENGTH_SHORT).show();
    finishAfterTransition();
  }
  
}