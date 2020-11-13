package com.example.asynctastexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private ProgressBar mProgressCopy = null;
    private TextView mTextMessage =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressCopy=findViewById(R.id.progressCopy);
        mTextMessage=findViewById(R.id.textMessage);

        Button buttonCopy =findViewById(R.id.buttonCopy);
        buttonCopy.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                CopyDatabaseAsyncTask task = new CopyDatabaseAsyncTask(MainActivity.this);
                task.execute("unnamed.jpg");
            }
        });


    }

    private class CopyDatabaseAsyncTask extends AsyncTask<String, Long, Boolean>{
        private Context mContext = null;

        public CopyDatabaseAsyncTask(Context context){
            mContext = context;
        }

        /**
         * doInBackground() : 실질적인 비동기 작업이 실행되는 메서드 , 백그라운드 스레드에서 실행되므로
         *                    메서드 내에서 UI를 직접 제어하면 안됨.
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(String... params) {
            AssetManager am = mContext.getResources().getAssets();
            File file = null;
            InputStream is = null;
            FileOutputStream fos = null;
            long fileSize = 0;
            long copySize = 0;
            int len = 0;

            byte[] buf = new byte[1024];

            try{
                fileSize = am.openFd(params[0]).getLength();

                is = am.open(params[0]);

                file = new File(getFilesDir(),params[0]);
                fos = new FileOutputStream(file);

                while((len=is.read(buf))>0){
                    fos.write(buf,0,len);

                    copySize += len;

                    publishProgress(fileSize,copySize);

                    //sleep 100ms
                    Thread.sleep(10);
                }

                Thread.sleep(500);

                fos.close();
                is.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return (fileSize==copySize);
        }

        /**
         *  onPreExecute() : 백그라운드 스레드가 실행되기 전, 메인 스레드에 의해 호출되는 메서드
         */

        @Override
        protected void onPreExecute() {
            mProgressCopy.setMax(100);
            mProgressCopy.setProgress(0);
            super.onPreExecute();
        }

        /**
         * onPostExecute() : 마지막으로 doInBackground() 메서드 실행이 완료되어 리턴 되었을 때 호출되는 메서드
         * @param result
         */
        @Override
        protected void onPostExecute(Boolean result) {
            mTextMessage.setText("Copy Completed.");
            super.onPostExecute(result);
        }

        /**
         * onProgressUpdate() : doInBackground() 메서드에서 publishProgress() 메서드를 호출했을 때,
         *                      메인 UI스레드에서 실행할 메서드
         * @param values
         */
        @Override
        protected void onProgressUpdate(Long... values) {
            long fileSize = values[0];
            long copySize = values[1];
            int percent = (int)((copySize*100)/fileSize);

            mTextMessage.setText(percent+" %");
            mProgressCopy.setProgress(percent);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}