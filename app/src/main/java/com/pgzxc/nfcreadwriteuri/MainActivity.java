package com.pgzxc.nfcreadwriteuri;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pgzxc.nfcreadwriteuri.activity.ShowNFCTagContentActivity;
import com.pgzxc.nfcreadwriteuri.activity.UriListActivity;
import com.pgzxc.nfcreadwriteuri.parse.UriRecord;

public class MainActivity extends AppCompatActivity {
    private TextView mSelectUri;
    private String mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSelectUri = (TextView) findViewById(R.id.textview_uri);

    }

    public void onClick_SelectUri(View view) {
        Intent intent = new Intent(this, UriListActivity.class);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            mUri = data.getStringExtra("uri");
            mSelectUri.setText(mUri);
        }
    }

    public void onNewIntent(Intent intent) {
        if (mUri == null) {
            Intent myIntent = new Intent(this, ShowNFCTagContentActivity.class);
            myIntent.putExtras(intent);
            myIntent.setAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            startActivity(myIntent);
        } else  // write nfc
        {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{createUriRecord(mUri)});
            if (writeTag(ndefMessage, tag)) {
                mUri = null;
                mSelectUri.setText("");
            }
        }
    }

    public NdefRecord createUriRecord(String uriStr) {
        byte prefix = 0;
        for (Byte b : UriRecord.URI_PREFIX_MAP.keySet()) {
            String prefixStr = UriRecord.URI_PREFIX_MAP.get(b).toLowerCase();
            if ("".equals(prefixStr))
                continue;
            if (uriStr.toLowerCase().startsWith(prefixStr)) {
                prefix = b;
                uriStr = uriStr.substring(prefixStr.length());
                break;
            }

        }
        byte[] data = new byte[1 + uriStr.length()];
        data[0] = prefix;
        System.arraycopy(uriStr.getBytes(), 0, data, 1, uriStr.length());

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], data);
        return record;
    }

    boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
                return true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }
}
