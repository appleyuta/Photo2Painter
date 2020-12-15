package com.yutakobayashi.artisticconverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomDialogFlagment extends DialogFragment {
    private Activity activity;
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = activity;
    }


    // ダイアログが生成された時に呼ばれるメソッド ※必須
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        return new AlertDialog.Builder(getActivity())
                .setTitle("確認")
                .setMessage("現在表示されている画像を保存しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        //Activity activity = getActivity();
                        ImageView imageView = activity.findViewById(R.id.image_view);
                        Bitmap saveImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        // 保存先のフォルダー
                        File cFolder = activity.getExternalFilesDir(Environment.DIRECTORY_DCIM);

                        String fileDate = new SimpleDateFormat(
                                "yyyMMdd_HHmmss", Locale.US).format(new Date());
                        // ファイル名
                        String fileName = String.format("Transfer_%s.jpg", fileDate);

                        File cameraFile = new File(cFolder, fileName);
                        try {
                            FileOutputStream out = new FileOutputStream(cameraFile);
                            saveImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        registerDatabase(cameraFile);
                        resultSaveImg();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }
    @Override
    public void onPause() {
        super.onPause();

        // onPause でダイアログを閉じる場合
        dismiss();
    }


    private void registerDatabase(File file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = activity.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file.getAbsolutePath());
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }


    //ユーザに画像が正常に保存されたことを知らせる
    private void resultSaveImg() {
        Toast toast = Toast.makeText(activity, "画像を保存しました", Toast.LENGTH_SHORT);
        toast.show();
    }
}