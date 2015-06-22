package jkmg.com.androidappobjectscommunicants;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by GuillaumeB on 21/06/2015.
 */
public class ParameterActivity extends AppCompatActivity {

    private EditText hostAddr;
    private EditText camPort;
    private EditText cmdPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        // On set la toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        // Recup des elems ui
        hostAddr = (EditText) findViewById(R.id.host_value);
        camPort  = (EditText) findViewById(R.id.cam_port_value);
        cmdPort = (EditText) findViewById(R.id.cmd_port_value);
    }

    public void onClick(View view) {

        Parameter params = new Parameter();

        params.setHostAddr("192.168.100.15");
        params.setCamPort(5008);
        params.setCmdPort(5005);

        Intent intent = new Intent(this, RemoteCamControlActivity.class);
        intent.putExtra("params", params);
        startActivity(intent);

/*
        // Recup input utilisateur
        String tmpHostAddr = hostAddr.getText().toString();
        String tmpCamPort = camPort.getText().toString();
        String tmpCmdPort = cmdPort.getText().toString();

        // Check non empty input
        if (tmpHostAddr == null || tmpHostAddr.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Host empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tmpCamPort == null || tmpCamPort.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Cam port empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tmpCmdPort == null || tmpCmdPort.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Cmd port empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Changement d'Activity
        params.setHostAddr(tmpHostAddr);
        params.setCamPort(Integer.parseInt(tmpCamPort));
        params.setCmdPort(Integer.parseInt(tmpCmdPort));

        Intent intent = new Intent(this, RemoteCamControlActivity.class);
        intent.putExtra("params", params);
        startActivity(intent);*/

    }
}
