package com.PKG.rs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private PDFView pdfView;
    private TextView urlTextView;
    private ProgressBar progressBar;
    private boolean isPdfDownloaded = false;

    TextView idtxt, Nametxt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_main);

        pdfView = findViewById(R.id.pdfView);
        urlTextView = findViewById(R.id.urlTextView);
        progressBar = findViewById(R.id.progressBar);

        idtxt = findViewById(R.id.id1);
        Nametxt = findViewById(R.id.name1);

        Intent intent = getIntent();
        String questionPaperName = intent.getStringExtra("QUESTION_PAPER_NAME");
        String id = intent.getStringExtra("ID");
        String Name = intent.getStringExtra("NAME");

        idtxt.setText(id);
        Nametxt.setText(Name);

        fetchPdfUrls(questionPaperName);
    }

    private void fetchPdfUrls(String questionPaperName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://akgsacademy.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        User user = new User();
        user.setId(questionPaperName);

        apiService.postData(user).enqueue(new Callback<List<Map<String, String>>>() {
            @Override
            public void onResponse(Call<List<Map<String, String>>> call, Response<List<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleResponse(response.body());
                } else {
                    showToast("Failed to get response data");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, String>>> call, Throwable t) {
                showToast("Network error");
            }
        });
    }

    private void handleResponse(List<Map<String, String>> responseData) {
        StringBuilder combinedUrls = new StringBuilder();
        for (Map<String, String> data : responseData) {
            String pdf = data.get("pdf");
            String path = data.get("path");
            String url = path + "/" + pdf;
            combinedUrls.append(url).append("\n\n");
        }
        urlTextView.setText(combinedUrls.toString());
        loadPdfFromUrl(combinedUrls.toString().trim().split("\n\n")[0]);
    }

    private void loadPdfFromUrl(String pdfUrl) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                downloadAndDisplayPdf(pdfUrl);
            }
        } else {
            downloadAndDisplayPdf(pdfUrl);
        }
    }

    private void downloadAndDisplayPdf(String pdfUrl) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                String contentType = connection.getContentType();

                if (!"application/pdf".equals(contentType)) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        showToast("The file is not a valid PDF");
                    });
                    return;
                }

                int fileLength = connection.getContentLength();
                File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "downloaded.pdf");

                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(pdfFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    long total = 0;
                    while ((length = inputStream.read(buffer)) > 0) {
                        total += length;
                        if (fileLength > 0) {
                            int progress = (int) (total * 100 / fileLength);
                            runOnUiThread(() -> progressBar.setProgress(progress));
                        }
                        outputStream.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (pdfFile.exists()) {
                        isPdfDownloaded = true; // PDF downloaded successfully
                        pdfView.fromFile(pdfFile)
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        showToast("PDF loaded with " + nbPages + " pages.");
                                    }
                                })
                                .load();
                    } else {
                        showToast("Failed to load PDF file");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to download PDF");
                });
            }
        }).start();
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String combinedUrls = urlTextView.getText().toString();
                loadPdfFromUrl(combinedUrls.split("\n\n")[0]);
            } else {
                showToast("Permission denied");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isPdfDownloaded) {
            super.onBackPressed();
        } else {
            showToast("Please wait until the PDF is downloaded");
        }
    }

    public void goback(View view) {
        onBackPressed();
    }
}
