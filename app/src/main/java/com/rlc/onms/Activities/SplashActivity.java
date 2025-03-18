package com.rlc.onms.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.rlc.onms.Configs.BuildConfig;
import com.rlc.onms.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    //https://drive.google.com/file/d/1gCuUckNc-RHDQKyU9j8CSkE9OABK2OYZ/view?usp=drive_link
    private static final String FOLDER_ID = "1G45fP3qEfP97DNVZeQqgqUNAQJVQj38j"; // Drive klasör kimliği

    private static final int REQUEST_INSTALL_PACKAGES = 1234;
    private String version;

    private String pendingFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        version = BuildConfig.VERSION_NAME; // Mevcut uygulama sürümünü al
        checkDriveFileInBackground(); // Güncellemeleri kontrol et
    }

    private void checkDriveFileInBackground() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(getMainLooper());

        Toast.makeText(this, "Güncellemeler kontrol ediliyor...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                // Drive'daki dosya adını al
                String fileName = getFileNameFromDrive();

                handler.post(() -> {
                    if (fileName != null) {
                        System.out.println("Bulunan dosya adı: " + fileName);

                        if (!fileName.equals(getAppFileName())) {
                            System.out.println("Güncelleme gerekli!");
                            showUpdateDialog(fileName);
                        } else {
                            System.out.println("Uygulamanız güncel.");
                            moveToMainActivity(); // Güncelse MainActivity'ye geçiş
                        }
                    } else {
                        System.out.println("Klasörde dosya bulunamadı.");
                        moveToMainActivity(); // Dosya bulunamadığında MainActivity'ye geçiş
                    }
                });
            } catch (IOException e) {
                handler.post(() -> {
                    System.out.println("Hata oluştu: " + e.getMessage());
                    moveToMainActivity(); // Hata durumunda MainActivity'ye geçiş
                });
            }
        });
    }

    private String getFileNameFromDrive() throws IOException {
        // Hizmet hesabı JSON anahtar dosyasını yükleyin
        InputStream jsonKeyStream = getResources().openRawResource(R.raw.service_account_key);
        GoogleCredentials credentials = GoogleCredentials.fromStream(jsonKeyStream)
                .createScoped(Collections.singleton(DriveScopes.DRIVE));

        Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("ONMS")
                .build();

        // Klasördeki dosyayı sorgula
        FileList result = service.files().list()
                .setQ("'" + SplashActivity.FOLDER_ID + "' in parents") // Belirtilen klasörü hedefler
                .setFields("files(name)") // Sadece dosya adını alır
                .execute();

        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getName(); // İlk dosya adını döndür
        } else {
            return null; // Klasör boşsa null döndür
        }
    }

    private String getAppFileName() {
        return getString(R.string.app_name) + "_" + version + ".apk"; // Örn: "ONMS_1.0.apk"
    }
    private void installApk(File apkFile) {
        Uri apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", apkFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // URI'ye okuma izni ver
        startActivity(intent);
    }

    private void showUpdateDialog(String fileName) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Uygulama Güncellemesi")
                .setMessage("Yeni bir güncelleme mevcut. İndirmek ister misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> downloadAndUpdate(fileName))
                .setNegativeButton("Hayır", (dialog, which) -> moveToMainActivity())
                .setCancelable(false)
                .show();
    }

    private void downloadAndUpdate(String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean canInstall = getPackageManager().canRequestPackageInstalls();
            if (!canInstall) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_INSTALL_PACKAGES);
                Toast.makeText(this, "Lütfen yükleme izni verin.", Toast.LENGTH_SHORT).show();
                pendingFileName = fileName; // İzin sonrası işlem için dosya adını sakla
                return; // İzin alınmadan devam etmeyin
            }
        }
        downloadAndInstallApk(fileName);
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // SplashActivity'yi kapat
    }

    private void downloadAndInstallApk(String fileName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Google Drive API ile dosya indiriliyor

                InputStream jsonKeyStream = getResources().openRawResource(R.raw.service_account_key);
                GoogleCredentials credentials = GoogleCredentials.fromStream(jsonKeyStream)
                        .createScoped(Collections.singleton(DriveScopes.DRIVE));
                Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpCredentialsAdapter(credentials))
                        .setApplicationName("ONMS")
                        .build();

                String fileId = getFileIdFromDrive(fileName);
                File apkFile = new File(getExternalFilesDir(null), "update.apk");

                OutputStream outputStream = new FileOutputStream(apkFile);
                service.files().get(fileId).executeMediaAndDownloadTo(outputStream); // FileId kullanılıyor
                outputStream.close();

                // APK dosyasını yükleme sürecini başlat
                handler.post(() -> installApk(apkFile));

            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Güncelleme indirilemedi.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INSTALL_PACKAGES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean canInstall = getPackageManager().canRequestPackageInstalls();
                if (canInstall && pendingFileName != null) {
                    downloadAndInstallApk(pendingFileName); // İzin sonrası işlemi başlat
                } else {
                    Toast.makeText(this, "Yükleme izni verilmedi.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private String getFileIdFromDrive(String fileName) throws IOException {

        InputStream jsonKeyStream = getResources().openRawResource(R.raw.service_account_key);
        GoogleCredentials credentials = GoogleCredentials.fromStream(jsonKeyStream)
                .createScoped(Collections.singleton(DriveScopes.DRIVE));
        Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("ONMS")
                .build();

        FileList result = service.files().list()
                .setQ("'" + SplashActivity.FOLDER_ID + "' in parents and name='" + fileName + "'")
                .setFields("files(id, name)") // Sadece dosya kimliği ve adı alınır
                .execute();

        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId(); // İlk dosyanın kimliğini döndür
        } else {
            return null; // Dosya bulunamadı
        }
    }


}
