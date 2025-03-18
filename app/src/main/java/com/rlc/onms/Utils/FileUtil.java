package com.rlc.onms.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {



    // Drive dosya indirme. KMZ listesi
    public static void updateFilesFromDrive(Context context, Drive googleDriveService, String parentFolderId) throws IOException {
        //InputStream jsonKeyStream = context.getResources().openRawResource(R.raw.service_account_key);
        FileList result = googleDriveService.files().list()
                .setSpaces("drive")
                .setQ("'" + parentFolderId + "' in parents")
                .setFields("files(id, name)")
                .execute();

        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files != null) {
            for (com.google.api.services.drive.model.File file : files) {
                File localFile = new File(context.getFilesDir(), "kmz_files/" + file.getName());
                try (OutputStream outputStream = new FileOutputStream(localFile)) {
                    googleDriveService.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
                }
            }
        }
    }

    // KMZ leri listeleme
    public static List<String> getInternalKmzFiles(Context context) {
        File kmzDir = new File(context.getFilesDir(), "kmz_files");
        if (!kmzDir.exists()) {
            kmzDir.mkdirs();
        }

        File[] files = kmzDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".kmz"));
        List<String> kmzFiles = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                kmzFiles.add(file.getName());
            }
        }
        return kmzFiles;
    }

    // kmz > kml
    public static String extractKmlFromKmz(Context context, Uri kmzUri) throws IOException {


        InputStream inputStream = context.getContentResolver().openInputStream(kmzUri);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;

        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.getName().endsWith(".kml")) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                zipInputStream.closeEntry();
                return outputStream.toString("UTF-8");
            }
        }
        return null;
    }

         public static Intent getFileChooserIntent() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            return intent;
        }
}
