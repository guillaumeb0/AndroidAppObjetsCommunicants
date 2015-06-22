package jkmg.com.androidappobjectscommunicants;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class RemoteCamControlActivity extends AppCompatActivity {

    private Socket camSocket;
    private DatagramSocket cmdSocket;
    private DataInputStream input;

    /*private static final String serverAddr = "192.168.0.13";
    private static final int camPort = 5008;*/

    private String serverAddr;
    private int camPort;
    private int cmdPort;

    private int LENGTH_SIZE = 5;

    private TextView status;
    private TextView lengthValue;
    private TextView messageValue;
    private ImageView picture;
    private Button buttonLeft;
    private Button buttonUp;
    private Button buttonRight;
    private Button buttonDown;

    private String res;
    private Bitmap bm;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_cam_control);

        // Recup infos intent
        Intent intent = getIntent();
        Parameter params = (Parameter) intent.getSerializableExtra("params");
        serverAddr = params.getHostAddr();
        camPort = params.getCamPort();
        cmdPort = params.getCmdPort();


        // Recup des elements graphique
        status = (TextView) findViewById(R.id.status_value);
        lengthValue = (TextView) findViewById(R.id.length_value);
        //messageValue = (TextView) findViewById(R.id.message_value);
        picture = (ImageView)findViewById(R.id.picture);
        buttonLeft = (Button) findViewById(R.id.button_left);
        buttonUp = (Button) findViewById(R.id.button_up);
        buttonRight = (Button) findViewById(R.id.button_right);
        buttonDown = (Button) findViewById(R.id.button_down);


        // Debut du code utile
        new Thread(new Runnable() {
            @Override
            public void run() {
                initCmdSocket();
                connectToCamServer();
                // Boucle récupérant et affichant les images capturées par la caméra depuis le serveur
                while (!Thread.currentThread().isInterrupted()) {
                    // Récupération d'une image du serveur
                    try {
                        bm = readImageFromNetwork();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    // Mise à jour de l'IHM avec la dernière image reçue
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            picture.setImageBitmap(RemoteCamControlActivity.this.bm);
                        }
                    });
                }
            }
        }).start();
    }

    // Lis une entrée réseau et la transforme de jpg en bitmap
    private Bitmap readImageFromNetwork() {
        byte[] data = readDataFromNetwork();
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, Integer.parseInt(lengthValue.getText().toString()));
        return bm;
    }

    public void onClick(View view) {

        Button button = (Button) view;


        switch (button.getId()) {
            case R.id.button_left: Toast.makeText(getApplicationContext(), "left button", Toast.LENGTH_SHORT).show();
                sendCmdToCmdServer("left");
                break;
            case R.id.button_up: Toast.makeText(getApplicationContext(), "up button", Toast.LENGTH_SHORT).show();
                sendCmdToCmdServer("up");
                break;
            case R.id.button_right: Toast.makeText(getApplicationContext(), "right button", Toast.LENGTH_SHORT).show();
                sendCmdToCmdServer("right");
                break;
            case R.id.button_down: Toast.makeText(getApplicationContext(), "down button", Toast.LENGTH_SHORT).show();
                sendCmdToCmdServer("down");
                break;
        }
    }

    private void sendCmdToCmdServer(String value) {

        JSONObject msg = new JSONObject();

        try {
            msg.put("type", 0);
            msg.put("content", value);
        } catch (JSONException e) {
            printException(e);
        }
        new Thread(new CmdSender(msg.toString())).start();


    }

    class CmdSender implements Runnable{

        private String value;

        public CmdSender(String value) {
            this.value = value;
        }

        @Override
        public void run() {
            byte[] msg = value.getBytes();
            try {
                InetAddress host = InetAddress.getByName(serverAddr);
                DatagramPacket packet = new DatagramPacket(msg, msg.length, host, cmdPort);
                cmdSocket.send(packet);
            } catch (UnknownHostException e) {
                printException(e);
            } catch (IOException e) {
                printException(e);
            }
        }
    }


    private byte[] readDataFromNetwork() {
        byte[] data = new byte[LENGTH_SIZE];
        try {
            // Récupération de la longueur du prochain message
            input.readFully(data);
            int length = Integer.parseInt(new String(data));
            // Affichage de la taille du prochain message dans l'IHM
            runOnUiThread(new TextViewUpdater(lengthValue, String.valueOf(length)));
            // Récupération du prochain message
            data = new byte[length];
            input.readFully(data, 0, length);
        } catch (IOException e) {
            printException(e);
            return null;
        }
        return data;
    }

    // Méthode utilitaire pour "Toast" une exception
    private void printException(Exception e) {
        runOnUiThread(new ExceptionToaster(e));
    }

    // Classe utilitaire pour maj la valeur d'une TextView
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

    // Classe utilitaire pour "Toast" une exception
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

    private void initCmdSocket() {
        try {
            cmdSocket = new DatagramSocket();
        } catch (SocketException e) {
            printException(e);
        }
    }

    // Methode de connexion au serveur & de créatoin d'un inputstream
    private void connectToCamServer() {
        try {
            camSocket = new Socket(serverAddr, camPort);
            input = new DataInputStream(camSocket.getInputStream());
            runOnUiThread(new TextViewUpdater(status, "Connexion établie"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

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
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
