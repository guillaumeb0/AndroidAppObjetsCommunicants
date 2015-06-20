package jkmg.com.androidappobjectscommunicants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class MainActivity extends AppCompatActivity {

    private ServerSocket serverSocket;
    private Socket socket;
    DataInputStream input;

    private static final String SERVER_ADDR = "192.168.0.13";
    private static final int SERVER_PORT = 5008;

    private int LENGTH_SIZE = 5;

    private TextView status;
    private TextView lengthValue;
    private TextView messageValue;
    private ImageView picture;
    private EditText editText;
    private Button button;
    private String res;
    private Bitmap bm;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recup des elements graphique
        editText = (EditText) findViewById(R.id.EditText01);
        button = (Button) findViewById(R.id.myButton);
        status = (TextView) findViewById(R.id.status_value);
        lengthValue = (TextView) findViewById(R.id.length_value);
        messageValue = (TextView) findViewById(R.id.message_value);
        picture = (ImageView)findViewById(R.id.picture);


        new Thread(new Runnable() {
            @Override
            public void run() {
                startServer();
                while (!Thread.currentThread().isInterrupted()) {
                    /*res = new String(readDataFromNetwork());
                    runOnUiThread(new TextViewUpdater(messageValue, res));*/
                    try {
                        bm = readImageFromNetwork();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            picture.setImageBitmap(MainActivity.this.bm);
                        }
                    });
                }
            }
        }).start();
    }

    private Bitmap readImageFromNetwork() {
        byte[] data = readDataFromNetwork();
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, Integer.parseInt(lengthValue.getText().toString()));
        return bm;
    }

    public void onClick(View view) {
        /*String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            String content = "hello world";
            File file;
            FileOutputStream outputStream;
            try {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyCache");

                outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
                Toast.makeText(getApplicationContext(), file.getPath(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }


    private byte[] readDataFromNetwork() {
        byte[] data = new byte[LENGTH_SIZE];
        try {
            input.readFully(data);
            int length = Integer.parseInt(new String(data));
            runOnUiThread(new TextViewUpdater(lengthValue, String.valueOf(length)));
            data = new byte[length];
            input.readFully(data, 0, length);
        } catch (IOException e) {
            printException(e);
            return null;
        }
        return data;
    }

    private void printException(Exception e) {
        runOnUiThread(new ExceptionToaster(e));
    }

    class TextViewUpdater implements Runnable {
        private TextView view;
        private String value;

        public TextViewUpdater(TextView view, String value) {
            this.view = view;
            this.value = value;
        }

        @Override
        public void run() {
            view.setText(value);
        }
    }


    class ExceptionToaster implements Runnable {
        private Exception e;

        public ExceptionToaster(Exception e) {
            this.e = e;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            socket = serverSocket.accept();
            input = new DataInputStream(socket.getInputStream());
            runOnUiThread(new TextViewUpdater(status, "Connexion établie"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
