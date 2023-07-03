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

import static ir.bistcard.nfcconnector.Utils.byteArrayToHexString;

public class MainActivity extends AppCompatActivity {
  Button btnRead;
  Button btnWrite;
  Button btnChangeKeyDefault;
  Button btnChangeKey;
  EditText edtSector;
  EditText edtBlock;
  TextView txtLog;

  Switch switchKeyB;
  Switch switchDefaultKey;
  private static final byte[] KEY_A = {(byte) 0x1C, (byte) 0x1C, (byte) 0x1C, (byte) 0x1C, (byte) 0x1C, (byte) 0x1C};
  private static final byte[] DEFAULT_ACCESS_BITS = {(byte) 0xff, (byte) 0x07, (byte) 0x80, (byte) 0x69};
  private static final byte[] ACCESS_BITS = {(byte) 0x7E, (byte) 0x17, (byte) 0x88, (byte) 0x69};
  private static final byte[] KEY_B = {(byte) 0xfc, (byte) 0xfc, (byte) 0xfc, (byte) 0xfc, (byte) 0xfc, (byte) 0xfc};


  private static final byte[] SAMPLE_BLOCK = {(byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d, (byte) 0x1f, (byte) 0x1d};
  NfcAdapter nfcAdapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    btnRead = findViewById(R.id.btnRead);
    btnChangeKeyDefault = findViewById(R.id.btnChangeKeyDefault);
    btnChangeKey = findViewById(R.id.btnChangeKey);
    btnWrite = findViewById(R.id.btnWrite);
    edtSector = findViewById(R.id.edtSector);
    edtBlock = findViewById(R.id.edtBlock);
    txtLog = findViewById(R.id.txtLog);
    switchKeyB = findViewById(R.id.switchKeyB);
    switchDefaultKey = findViewById(R.id.switchDefaultKey);
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

      if (edtSector == null || edtSector.getText().toString().isEmpty() || edtBlock == null || edtBlock.getText().toString().isEmpty() || edtBlock.getText().toString().equals("3")) {
        txtLog.append("Error : Enter Sector or block " + "\n");
        return;
      }
      int randomRequestCode = new Random().nextInt(1000 - 10) + 10;

      int sector = Integer.parseInt(edtSector.getText().toString());
      int block = Integer.parseInt(edtBlock.getText().toString());
      txtLog.append("Write : Sector" + sector + "block : " + block + "\n");
      Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("ID", "WRITE");
      intent.putExtra("SECTOR", sector);
      intent.putExtra("BLOCK", block);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, randomRequestCode, intent, 0);
      String[][] techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);

    });

    btnChangeKey.setOnClickListener(view -> {
      if (edtSector == null || edtSector.getText().toString().isEmpty()) {
        txtLog.append("Error : Enter Sector" + "\n");
        return;
      }
      int randomRequestCode = new Random().nextInt(1000 - 10) + 10;

      int sector = Integer.parseInt(edtSector.getText().toString());
      txtLog.append("ChangeKey  Sector : " + sector + "\n");
      Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("ID", "CHANGE_KEY");
      intent.putExtra("SECTOR", sector);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, randomRequestCode, intent, 0);
      String[][] techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);
    });
    btnChangeKeyDefault.setOnClickListener(view -> {
      if (edtSector == null || edtSector.getText().toString().isEmpty()) {
        txtLog.append("Error : Enter Sector " + "\n");
        return;
      }
      int randomRequestCode = new Random().nextInt(1000 - 10) + 10;

      int sector = Integer.parseInt(edtSector.getText().toString());
      txtLog.append(" Change Key to Default Sector : " + sector + "\n");
      Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("ID", "CHANGE_KEY_DEFAULT");
      intent.putExtra("SECTOR", sector);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, randomRequestCode, intent, 0);
      String[][] techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techListsArray);
    });
  }

  private void writeData(MifareClassic mifareClassic, int sector, int block) throws IOException {
    int blockIndex = sector * 4 + block;
    mifareClassic.connect();
    boolean isAuthenticated = authenticateSector(mifareClassic, sector);
    txtLog.append("isAuthenticated  : " + isAuthenticated + "  Sector : " + sector + "\n\n");
    if (!isAuthenticated) return;
    mifareClassic.writeBlock(blockIndex, SAMPLE_BLOCK);
    txtLog.append("Success" + "\n");

    mifareClassic.close();

  }

  /*
   change keys and access bit
    6 byte key a + 4 byte access bit (3 byte + 1 byte user data) = 6 byte key b

     calculate access bit : http://calc.gmss.ru/Mifare1k/ or https://slebe.dev/mifarecalc/
   */
  private void changeKey(MifareClassic mifareClassic, int sector) throws IOException {
    int blockIndex = sector * 4 + 3;
    mifareClassic.connect();
    boolean isAuthenticated = authenticateSector(mifareClassic, sector);
    txtLog.append("isAuthenticated  : " + isAuthenticated + "  Sector : " + sector + "\n\n");
    if (!isAuthenticated) return;

    byte[] sector_trailer = new byte[16];

    System.arraycopy(KEY_A, 0, sector_trailer, 0, KEY_A.length);
    System.arraycopy(ACCESS_BITS, 0, sector_trailer, KEY_A.length, ACCESS_BITS.length);
    System.arraycopy(KEY_B, 0, sector_trailer, ACCESS_BITS.length + KEY_A.length, KEY_B.length);

    txtLog.append("sector trailer preview : \n" + byteArrayToHexString(sector_trailer) + "\n");


    mifareClassic.writeBlock(blockIndex, sector_trailer);
    txtLog.append("Success" + "\n");

    mifareClassic.close();

  }

  /*
  6 byte key a + 4 byte access bit = 6 byte key b
  00 00 00 00 00 00 + ff 07 80 ff + 00 00 00 00 00 00

   */
  private void changeKeyToDefault(MifareClassic mifareClassic, int sector) throws IOException {
    int blockIndex = sector * 4 + 3;
    mifareClassic.connect();
    boolean isAuthenticated = authenticateSector(mifareClassic, sector);
    txtLog.append("isAuthenticated  : " + isAuthenticated + "  Sector : " + sector + "\n\n");
    if (!isAuthenticated) return;

    byte[] sector_trailer = new byte[16];

    System.arraycopy(MifareClassic.KEY_DEFAULT, 0, sector_trailer, 0, MifareClassic.KEY_DEFAULT.length);
    System.arraycopy(DEFAULT_ACCESS_BITS, 0, sector_trailer, MifareClassic.KEY_DEFAULT.length, DEFAULT_ACCESS_BITS.length);
    System.arraycopy(MifareClassic.KEY_DEFAULT, 0, sector_trailer, DEFAULT_ACCESS_BITS.length + MifareClassic.KEY_DEFAULT.length, MifareClassic.KEY_DEFAULT.length);

    txtLog.append("sector trailer preview : \n" + byteArrayToHexString(sector_trailer) + "\n");


    mifareClassic.writeBlock(blockIndex, sector_trailer);
    txtLog.append("Success" + "\n");

    mifareClassic.close();

  }


  private void readData(MifareClassic mifareClassic, int sector) throws IOException {
    int block1number = sector * 4;
    int block2number = sector * 4 + 1;
    int block3number = sector * 4 + 2;
    int blockTrailerNo = sector * 4 + 3;
    mifareClassic.connect();
    boolean isAuthenticated = authenticateSector(mifareClassic, sector);
    txtLog.append("isAuthenticated  : " + isAuthenticated + "  Sector : " + sector + "\n\n");
    if (!isAuthenticated) {
      return;
    }
    String block0 = byteArrayToHexString(mifareClassic.readBlock(block1number));
    txtLog.append("block 0 : \n" + block0 + "\n");
    String block1 = byteArrayToHexString(mifareClassic.readBlock(block2number));
    txtLog.append("block 1 : \n" + block1 + "\n");

    String block2 = byteArrayToHexString(mifareClassic.readBlock(block3number));
    txtLog.append("block 2 : \n" + block2 + "\n");
    String block3 = byteArrayToHexString(mifareClassic.readBlock(blockTrailerNo));
    txtLog.append("block Trailer : \n" + block3 + "\n\n");
    mifareClassic.close();
  }

  //handle authentication to card
  private boolean authenticateSector(MifareClassic mifareClassic, int sector) throws IOException {


    if (switchDefaultKey.isChecked()) {
      byte[] defaultKey = MifareClassic.KEY_DEFAULT;
      txtLog.append("defaultKey : \n" +  byteArrayToHexString(defaultKey) + "\n");

      if (switchKeyB.isChecked()) {
        return mifareClassic.authenticateSectorWithKeyB(sector, defaultKey);
      } else {
        return mifareClassic.authenticateSectorWithKeyA(sector, defaultKey);
      }


    } else {
      if (switchKeyB.isChecked()) {
        return mifareClassic.authenticateSectorWithKeyB(sector, KEY_B);
      } else {
        return mifareClassic.authenticateSectorWithKeyA(sector, KEY_A);
      }
    }

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
          int blockIndex = intent.getExtras().getInt("BLOCK");
          writeData(mifareClassic, sector, blockIndex);
          break;
        case "CHANGE_KEY":
          changeKey(mifareClassic, sector );
          break;
        case "CHANGE_KEY_DEFAULT":
          changeKeyToDefault(mifareClassic, sector );
          break;
      }
    } catch (IOException e) {
      txtLog.append("IOException : " + e.getMessage() + "\n");
      e.printStackTrace();
    }


//    try {
//      mifareClassic.connect();
////      byte[] defaultKey = MifareClassic.KEY_DEFAULT;
//      byte[] defaultKey =KEY_A;
//      boolean isAuthenticated = mifareClassic.authenticateSectorWithKeyA(2, defaultKey);
//      txtLog.append("isAuthenticated WithKeyB : " + isAuthenticated + "\n");
//      String block0 = byteArrayToHexString(mifareClassic.readBlock(8));
//      txtLog.append("block0: \n" + block0 + "\n");
//      String block1 = byteArrayToHexString(mifareClassic.readBlock(9));
//      txtLog.append("block1: \n" + block1 + "\n");
//
//      String block2 = byteArrayToHexString(mifareClassic.readBlock(10));
//      txtLog.append("block2: \n" + block2 + "\n");
//
//      String block3 = byteArrayToHexString(mifareClassic.readBlock(11));
//      txtLog.append("block3: \n" + block3 + "\n");
//
//      //change sector 1 keys
//      byte[] sector1_trailer =new byte[16];
//
//      // change keys without change access bit keys
////      System.arraycopy(KEY_A, 0, sector1_trailer, 0, KEY_A.length);
////      System.arraycopy(DEFAULT_ACCESS_BITS, 0, sector1_trailer, KEY_A.length, DEFAULT_ACCESS_BITS.length);
////      System.arraycopy(KEY_B, 0, sector1_trailer, DEFAULT_ACCESS_BITS.length+KEY_A.length, KEY_B.length);
//
//
//
//      // change keys and access bit
//
//      System.arraycopy(KEY_A, 0, sector1_trailer, 0, KEY_A.length);
//      System.arraycopy(ACCESS_BITS, 0, sector1_trailer, KEY_A.length, ACCESS_BITS.length);
//      System.arraycopy(KEY_B, 0, sector1_trailer, ACCESS_BITS.length+KEY_A.length, KEY_B.length);
//
//
//      txtLog.append("trailer preview: \n" + byteArrayToHexString(sector1_trailer) + "\n");
//
//     // mifareClassic.writeBlock(11, sector1_trailer);
//      mifareClassic.writeBlock(8, sector1_trailer);
//      String block = byteArrayToHexString(mifareClassic.readBlock(11));
//      txtLog.append("trailer: \n" + block + "\n");
//      mifareClassic.close();
//    } catch (IOException e) {
//      txtLog.append("IOException: " + e.getMessage() + "\n");
//
////      throw new RuntimeException(e);
//    }
//
//
//  }
  }
}