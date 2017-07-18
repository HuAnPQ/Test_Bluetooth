package com.ponce.hugo.test_bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ToggleButton mTB_bluetooth;
    Button b_escanear;
    ProgressDialog progressDialogEscanear;
    TextView tv_dispositivosEncontrados;

    BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mArrayAdapter = new ArrayList<>();

    int REQUEST_ENABLE_BT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mTB_bluetooth = (ToggleButton) findViewById(R.id.tb_bluetooth);
        mTB_bluetooth.setOnCheckedChangeListener(onCheckedChangeListener);
        mTB_bluetooth.setChecked(mBluetoothAdapter.isEnabled());

        b_escanear = (Button) findViewById(R.id.b_escanear);
        tv_dispositivosEncontrados = (TextView) findViewById(R.id.tv_dispositivosEncontrados);

        //region progressDialogEscanear
        progressDialogEscanear = new ProgressDialog(this);
        progressDialogEscanear.setMessage("Escaneando...");
        progressDialogEscanear.setCancelable(false);
        progressDialogEscanear.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBluetoothAdapter.cancelDiscovery();
                    }
                });
        //endregion

        IntentFilter filtro = new IntentFilter();
        filtro.addAction(BluetoothDevice.ACTION_FOUND);
        filtro.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filtro.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filtro);

        HabilitarControles();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (! EsSoportadoBluetooth()){
            mostrarToast("Dispositivo Bluetooth no soportado.");
            finish();
        }
    }

    //region OnCheckedChangeListener
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                // The toggle is enabled
                if (mBluetoothAdapter.isEnabled()) return;
                Intent activarBluetoothIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(activarBluetoothIntent, REQUEST_ENABLE_BT);
                onActivityResult(REQUEST_ENABLE_BT, RESULT_CANCELED, activarBluetoothIntent);
                //mostrarToast("Activado");

            } else {
                // The toggle is disabled
                if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
                mostrarToast("Desactivado");
            }
            //Esta linea es para el botón no cambie de estado hasta que se responda la pregunta.
            mTB_bluetooth.setChecked(mBluetoothAdapter.isEnabled());
            HabilitarControles();
        }
    };
    //endregion

    public void Escanear(View view){
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            mostrarToast("Activado");
            mTB_bluetooth.setChecked(mBluetoothAdapter.isEnabled());
        }
        HabilitarControles();
    }

    private boolean EsSoportadoBluetooth(){
        return (mBluetoothAdapter != null   );
    }

    private void HabilitarControles(){
        boolean valor = mBluetoothAdapter.isEnabled();
        b_escanear.setEnabled(valor);
    }

    //region Métodos Utilies
    private void mostrarToast(String mensaje){
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
    //endregion


    //region Receptor de difusión(BroadcastReceiver)
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    mArrayAdapter = new ArrayList<>();
                    progressDialogEscanear.show();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    progressDialogEscanear.dismiss();
                    MostrarDispositivosEncontrados();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mostrarToast("Nombre=>"+ dispositivo.getName() + "\nMAC=>" + dispositivo.getAddress());
                    mArrayAdapter.add(dispositivo);
                    break;
                default:
                    break;
            }

        }
    };

    private void MostrarDispositivosEncontrados() {
        String texto = "";
        for (BluetoothDevice dispositivo :
                mArrayAdapter) {
            texto = texto + dispositivo.getName() + "\n";
        }
        tv_dispositivosEncontrados.setText(texto);
    }
    //endregion
}

