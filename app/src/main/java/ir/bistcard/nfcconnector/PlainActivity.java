package ir.bistcard.nfcconnector;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;


/*
  Created by hotmi on 7/24/2023.
 */
public class PlainActivity extends AppCompatActivity {

  Button btnRead, btnWrite;
  EditText edtSector, edtText;
  TextView txtLog;
  NfcAdapter nfcAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_plain);
    btnRead = findViewById(R.id.btnRead);
    btnWrite = findViewById(R.id.btnWrite);
    edtSector = findViewById(R.id.edtSector);
    edtText = findViewById(R.id.edtText);
    txtLog = findViewById(R.id.txtLog);

    txtLog.setOnLongClickListener(view -> {
      txtLog.setText("");
      return true;
    });

  }


  @Override
  protected void onResume() {
    super.onResume();
    nfcAdapter = NfcAdapter.getDefaultAdapter(this);


    btnRead.setOnClickListener(view -> {
      if (edtSector == null || edtSector.getText().toString().isEmpty()) {
        txtLog.append("Error : Enter Sector" + "\n");
        return;
      }
      int randomRequestCode = new Random().nextInt(1000 - 10) + 10;

      int sector = Integer.parseInt(edtSector.getText().toString());
      txtLog.append("readData : Sector" + sector + "\n");
      Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("ID", "READ");
      intent.putExtra("SECTOR", sector);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, randomRequestCode, intent, 0);
      String[][] techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);


    });

    btnWrite.setOnClickListener(view -> {

      if (edtSector == null || edtSector.getText().toString().isEmpty()||edtText == null || edtText.getText().toString().isEmpty()) {
        txtLog.append("Error : Enter Sector or block " + "\n");
        return;
      }
      int randomRequestCode = new Random().nextInt(1000 - 10) + 10;

      int sector = Integer.parseInt(edtSector.getText().toString());
      txtLog.append("Write : Sector" + sector + "\n");
      Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("ID", "WRITE");
      intent.putExtra("SECTOR", sector);
      intent.putExtra("DATA", edtText.getText().toString());
      PendingIntent pendingIntent = PendingIntent.getActivity(this, randomRequestCode, intent, 0);
      String[][] techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);

    });

  }

  private void writeData(MifareClassic mifareClassic, int sector, String data) throws IOException {
    byte[] dataByte = new byte[48];
    byte[] inputByte = Utils.convertAsciiToBytes(data);
    System.arraycopy(inputByte, 0, dataByte, 0, inputByte.length);

    int blockIndex0 = sector * 4 + 0;
    int blockIndex1 = sector * 4 + 1;
    int blockIndex2 = sector * 4 + 2;

    byte[] block0 = new byte[16];
    System.arraycopy(dataByte, 0, block0, 0, 16);
    byte[] block1 = new byte[16];
    System.arraycopy(dataByte, 16, block1, 0, 16);
    byte[] block2 = new byte[16];
    System.arraycopy(dataByte, 32, block2, 0, 16);

    mifareClassic.connect();
    boolean isAuthenticated = mifareClassic.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT);
    txtLog.append("isAuthenticated  : " + isAuthenticated + "  Sector : " + sector + "\n\n");
    if (!isAuthenticated) return;
    mifareClassic.writeBlock(blockIndex0, block0);
    mifareClassic.writeBlock(blockIndex1, block1);
    mifareClassic.writeBlock(blockIndex2, block2);
    txtLog.append("Success" + "\n");

    mifareClassic.close();

  }

  private void readData(MifareClassic mifareClassic, int sector) throws IOException {
    int block1number = sector * 4;
    int block2number = sector * 4 + 1;
    int block3number = sector * 4 + 2;
    int blockTrailerNo = sector * 4 + 3;
    mifareClassic.connect();
    boolean isAuthenticated = mifareClassic.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT);
    txtLog.append("isAuthenticated  : " + isAuthenticated + "  Sector : " + sector + "\n\n");
    if (!isAuthenticated) {
      return;
    }

    byte[] block0 = mifareClassic.readBlock(block1number);
    byte[] block1 = mifareClassic.readBlock(block2number);
    byte[] block2 = mifareClassic.readBlock(block3number);


    byte[] result = new byte[block0.length + block1.length + block2.length];
    System.arraycopy(block0, 0, result, 0, block0.length);
    System.arraycopy(block1, 0, result, block0.length, block1.length);
    System.arraycopy(block2, 0, result, block0.length + block1.length, block2.length);

    txtLog.append("result : \n" + Utils.convertBytesToAscii(result) + "\n\n");
    mifareClassic.close();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    MifareClassic mifareClassic = MifareClassic.get(tag);
    txtLog.append("Tag detected" + "\n");
    int sector = intent.getExtras().getInt("SECTOR");
    try {
      switch (intent.getExtras().getString("ID")) {
        case "READ":
          readData(mifareClassic, sector);
          break;
        case "WRITE":
          String data = intent.getExtras().getString("DATA");
          writeData(mifareClassic, sector, data);
          break;

      }
    } catch (IOException e) {
      txtLog.append("IOException : " + e.getMessage() + "\n");
      e.printStackTrace();
    }


  }


}
