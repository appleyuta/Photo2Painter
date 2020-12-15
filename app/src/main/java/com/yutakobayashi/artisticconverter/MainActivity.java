package com.yutakobayashi.artisticconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.text.SimpleDateFormat;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;
    private final static int RESULT_GALLERY = 1003;
    //private final static int REQUEST_GALLERY = 1003;

    private ImageView imageView;
    private Uri cameraUri;
    private File cameraFile;
    private int input_width;
    private int input_height;
    private String transferName;
    private String transferModelName;
    private Spinner Modelselector;
    private ProgressDialog mDialog = null;
    private Context mContext = this;
    private Transfer transferexec = null;
    private Bitmap nowBitmap;//画風変換を行う対象となる画像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("debug", "onCreate()");
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);

        final Button cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Android 6, API 23以上でパーミッシンの確認
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                } else {
                    cameraIntent();
                }
            }
        });

        final Button galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGallery();
            }
        });

        Modelselector = findViewById(R.id.spinner);
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this);
        Modelselector.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                transferName = item;
                Log.v("name", transferName);
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Button transferButton = findViewById(R.id.transfer);
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //変換を実行するためのクラスのインスタンスを生成
                    if (imageView.getDrawable() != null) {
                        transferexec = new Transfer();
                        //実行して結果を得る
                        transferexec.execute("param");
                    }
                } catch (Exception e) {
                    Log.v("exception", "Transfer Exception");
                }
            }
        });

        final Button SaveButton = findViewById(R.id.save_button);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageView.getDrawable() != null) {
                    final CustomDialogFlagment dialog = new CustomDialogFlagment();
                    dialog.show(getSupportFragmentManager(), "sample");
                }
            }
        });
    }




    private class Transfer extends AsyncTask<String, Integer, Bitmap> {

        //private Bitmap img = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mContext);
            mDialog.setTitle("変換中");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMax(3);
            mDialog.setProgress(0);
            mDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                //進捗度合いを表す変数count
                int count = 0;
                //InputStream is = getContentResolver().openInputStream(cameraUri);
                //InputStream is = new FileInputStream(cameraFile);
                //Bitmap nowBitmap = BitmapFactory.decodeStream(is);
                //インクリメントしてプログレスバー更新
                count++;
                publishProgress(count);
                int wid = nowBitmap.getWidth();
                int hei = nowBitmap.getHeight();
                Log.v("w", String.valueOf(wid));
                Log.v("h", String.valueOf(hei));
                //縦横比により入力サイズを決定
                double h_per_w = (double)hei / wid;
                double a,b,c,d;
                a = Math.abs(h_per_w - 4.0/3);
                b = Math.abs(h_per_w - 3.0/4);
                c = Math.abs(h_per_w - 16.0/9);
                d = Math.abs(h_per_w - 9.0/16);
                if(a < b){
                    if(a < c){
                        if(a < d) {
                            //a
                            input_width = 420;
                            input_height = 560;
                        }
                        else{
                            //d
                            input_width = 640;
                            input_height = 360;
                        }
                    }
                    else if(c < d){
                        //c
                        input_width = 360;
                        input_height = 640;
                    }
                    else{
                        //d
                        input_width = 640;
                        input_height = 360;
                    }
                }
                else if(b < c){
                    if(b < d){
                        //b
                        input_width = 560;
                        input_height = 420;
                    }
                    else{
                        //d
                        input_width = 640;
                        input_height = 360;
                    }
                }
                else if(c < d){
                    //c
                    input_width = 360;
                    input_height = 640;
                }
                else{
                    //d
                    input_width = 640;
                    input_height = 360;
                }

                /*
                if ((double) hei / wid == 4.0 / 3) {
                    input_width = 420;
                    input_height = 560;
                } else if ((double) hei / wid == 3.0 / 4) {
                    input_width = 560;
                    input_height = 420;
                } else if ((double) hei / wid == 16.0 / 9) {
                    input_width = 360;
                    input_height = 640;
                } else if ((double) hei / wid == 9.0 / 16) {
                    input_width = 640;
                    input_height = 360;
                } else {
                    //4:3の標準サイズで変換
                    input_width = 420;
                    input_height = 560;
                }*/

                //is.close();
                //imgから画風変換ネットワーク入力サイズに変換
                Bitmap resize_img = Bitmap.createScaledBitmap(nowBitmap, input_width, input_height, false);
                //imgビットマップを破棄してメモリを解放
                //img.recycle();
                int[] intValues = new int[input_width * input_height];
                float[] floatValues = new float[input_width * input_height * 3];
                //resize_imgからピクセルごとのデータをintValuesに格納
                resize_img.getPixels(intValues, 0, resize_img.getWidth(), 0, 0, resize_img.getWidth(), resize_img.getHeight());
                //resize_imgビットマップを破棄してメモリを解放
                resize_img.recycle();
                //intValuesをfloatValues(RGB値)に変換
                for (int i = 0; i < intValues.length; ++i) {
                    final int val = intValues[i];
                    floatValues[i * 3] = ((val >> 16) & 0xFF);
                    floatValues[i * 3 + 1] = ((val >> 8) & 0xFF);
                    floatValues[i * 3 + 2] = (val & 0xFF);
                }

                float[] outputs = new float[input_width * input_height * 3];

                String[] outputNames = {"output0"};

                //変換に使用するモデル名を決定する
                //使用するモデル名はtransferModelNameに代入される
                selectModel(transferName, input_width, input_height);

                try {
                    //モデルを読み込む
                    TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(getResources().getAssets(), transferModelName);
                    Log.v("tensorflow", "pbファイル読み込み");
                    //プログレスバーを更新
                    count++;
                    publishProgress(count);

                    //モデルに入力するデータを指定する
                    inferenceInterface.feed("input_1", floatValues, 1, input_height, input_width, 3);

                    inferenceInterface.run(outputNames);
                    //モデルを計算して結果を得る
                    inferenceInterface.fetch(outputNames[0], outputs);
                    Log.v("output", "計算完了");
                    String out = String.valueOf(outputs[outputs.length - 1]);
                    Log.v("計算値", out);
                    //プログレスバーを更新
                    count++;
                    publishProgress(count);

                } catch (NullPointerException e) {
                    Log.v("exception", "NullPointer error");
                    Bitmap dummy = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    return dummy;
                }

                //outputs(RGB値)をintValuesに変換
                for (int i = 0; i < intValues.length; ++i) {
                    intValues[i] =
                            0xFF000000
                                    | (((int) (outputs[i * 3])) << 16)
                                    | (((int) (outputs[i * 3 + 1])) << 8)
                                    | ((int) (outputs[i * 3 + 2]));
                }
                //出力用のビットマップを生成
                Bitmap out_bitmap = Bitmap.createBitmap(input_width, input_height, Bitmap.Config.ARGB_8888);
                //計算された値をセットする
                out_bitmap.setPixels(intValues, 0, out_bitmap.getWidth(), 0, 0, out_bitmap.getWidth(), out_bitmap.getHeight());
                //変換結果画像を返す
                return out_bitmap;
            }
            /*catch(IOException e)
            {
                Log.v("exception","IOException");
            }*/ catch (Exception e) {

            }
            //失敗した場合ダミーを返す
            Bitmap dummy = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            return dummy;
        }

        @Override
        protected void onPostExecute(Bitmap out_bitmap) {
            if (out_bitmap.getWidth() != 1) {
                //実行結果をImageViewにセット
                imageView.setImageBitmap(out_bitmap);
            }
            //プログレスバーを終了
            mDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //プログレスバーを更新
            mDialog.setProgress(values[0]);
        }
    }


    //使用する変換ネットワークの名前を更新する
    private void selectModel(String name, int w, int h) {
        transferModelName = name + "_" + String.valueOf(w) + "_" + String.valueOf(h) + ".pb";
    }


    private void startGallery() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, RESULT_GALLERY);
    }

    /*
     * 以下カメラ部分のコード
     * */

    private void cameraIntent() {
        // 保存先のフォルダー
        File cFolder = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        String fileDate = new SimpleDateFormat(
                "yyyMMdd_HHmmss", Locale.US).format(new Date());
        // ファイル名
        String fileName = String.format("CameraIntent_%s.jpg", fileDate);

        cameraFile = new File(cFolder, fileName);

        Log.d("debug", cameraFile.getPath());
        cameraUri = FileProvider.getUriForFile(
                MainActivity.this,
                getApplicationContext().getPackageName() + ".fileprovider",
                cameraFile);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, RESULT_CAMERA);

        Log.d("debug", "startActivityForResult()");
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RESULT_CAMERA) {

            if (cameraUri != null) {
                imageView.setImageURI(cameraUri);
                registerDatabase(cameraFile);
                if (imageView.getDrawable() != null) {
                    nowBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                }
            } else {
                Log.d("debug", "cameraUri == null");
            }
        }
        if (requestCode == RESULT_GALLERY) {
            try {
                cameraUri = intent.getData();
                InputStream is = getContentResolver().openInputStream(cameraUri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                //imageView.setImageBitmap(bmp);
                imageView.setImageURI(cameraUri);
                nowBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                Log.d("debug", cameraUri.getHost());
            } catch (Exception e) {

            }
        }
    }

    // アンドロイドのデータベースへ登録する
    private void registerDatabase(File file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file.getAbsolutePath());
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    // Runtime Permission check
    private void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            cameraIntent();
        }
        // 拒否していた場合
        else {
            requestPermission();
        }
    }

    // 許可を求める
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this,
                    "許可されないとアプリが実行できません",
                    Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.d("debug", "onRequestPermissionsResult()");

        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}