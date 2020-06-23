package com.example.autocompletesoap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String NAMESPACE = "http://tempuri.org/";
    AutoCompleteTextView au_name;
    AutoCompleteTextView au_emp;
    private static ArrayAdapter<String> stud_name = null;
    ArrayList<String> stud_list = new ArrayList<String>();
    String[] str_name;

    String[] s_employeeno;
    private static ArrayAdapter<String>  empno=null;
    ArrayList<String> empnolist = new ArrayList<String>();

    Button camera;
    Button next;

    public static ImageView img_preview = null;
    static final int REQUEST_PICTURE_CAPTURE = 1;
    private String pictureFilePath;
    public static Context ctx;
    Bitmap bitmap;

    Dialog dialogbox;
    TextView dialogmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ctx = this;

        dialogbox = new Dialog(this);

        au_name = findViewById(R.id.nameid);
        au_emp = findViewById(R.id.nameid1);
        next = findViewById(R.id.nextid);

        img_preview = findViewById(R.id.cameraid);
        camera = findViewById(R.id.butid);

        (new getTeam()).execute();

        (new getNumber()).execute("","");

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            camera.setEnabled(false);
        }

        camera.setOnClickListener(captures);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bitmap == null) {

                    Toast.makeText(MainActivity.this, "Please take a photo", Toast.LENGTH_SHORT).show();

                } else {

                    bitmap = getResizedBitmap(bitmap, 250, 250);
                    String[] s = {"name", "company", "contactno", "dept"};

                    (new InsertwithImage()).execute(s);

                }

            }
        });


    }


    private View.OnClickListener captures = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                sendTakePictureIntent();
            }
        }
    };

    private void sendTakePictureIntent() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.AutoCompleteSoap.provider[YOUR_APP_ID]",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    cameraIntent.setClipData(ClipData.newRawUri("", photoURI));
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "AutoCompleteSoap" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                img_preview.setImageURI(Uri.fromFile(imgFile));
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), Uri.fromFile(imgFile));
                    bitmap = RotateBitmap(bitmap, 90);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class getTeam extends AsyncTask<String, String, String> {

        ProgressDialog bar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar = new ProgressDialog(MainActivity.this);
            bar.setCancelable(false);
            bar.setMessage("Please Wait");
            bar.setIndeterminate(true);
            bar.setCanceledOnTouchOutside(false);
            bar.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String methodname = "LoadCompany";
            String URL = "http://ip/folder/file.asmx";
            return WebService.WebServiceCall(null, null, methodname, NAMESPACE, URL);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            bar.dismiss();

            if (result.equals("")) {
                Toast.makeText(MainActivity.this, "Please give valid details", Toast.LENGTH_SHORT).show();
            } else {

                try {

                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobj = jsonarray.getJSONObject(i);
                        stud_list.add(jsonobj.getString("Dept"));

                    }
                    str_name = stud_list.toArray(new String[stud_list.size()]);
                    stud_name = new ArrayAdapter<String>(getApplicationContext(), R.layout.autocomelist, R.id.autocomtext, stud_list);
                    au_name.setThreshold(1);
                    au_name.setAdapter(stud_name);

                } catch (Exception er) {
                    Toast.makeText(MainActivity.this, "Waiting for Network", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public class getNumber extends AsyncTask<String, String, String> {

        ProgressDialog bar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar = new ProgressDialog(MainActivity.this);
            bar.setCancelable(false);
            bar.setMessage("Please Wait");
            bar.setIndeterminate(true);
            bar.setCanceledOnTouchOutside(false);
            bar.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String[] paras = {"name","class"};
            String[] values = {params[0],params[1]};
            String methodname = "Number";
            String URL = "http://myIP /Service/Report.asmx";
            return WebService.WebServiceCall(paras,values,methodname, NAMESPACE, URL);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            bar.dismiss();

            if(result.equals(""))
            {
                Toast.makeText(MainActivity.this, "Please give valid details", Toast.LENGTH_SHORT).show();
            }else {

                try {
                    empnolist.clear();
                    JSONArray jsonarray = new JSONArray(result);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobj = jsonarray.getJSONObject(i);
                        empnolist.add(jsonobj.getString("EmpNo").replace("-"," "));

                    }
                    s_employeeno=empnolist.toArray(new String[empnolist.size()]);
                    empno = new ArrayAdapter<String>(getApplicationContext(),R.layout.autocomelist,R.id.autocomtext,empnolist);
                    au_emp.setThreshold(1);
                    au_emp.setAdapter(empno);
                }
                catch(Exception er)
                {
                    Toast.makeText(MainActivity.this, "Waiting for Network", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    public class InsertwithImage extends AsyncTask<String, String, String> {

        byte[] data;
        ProgressDialog bar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar = new ProgressDialog(MainActivity.this);
            bar.setCancelable(false);
            bar.setMessage("Upload Image");
            bar.setIndeterminate(true);
            bar.setCanceledOnTouchOutside(false);
            bar.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String URL = "http://ip/service/services.asmx";

            String responsetring = "";
            try {
                Bitmap bm = bitmap;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                data = baos.toByteArray();
            } catch (Exception er) {

            }
            try {
                SoapObject request = new SoapObject(NAMESPACE, "methodname");
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                MarshalBase64 marshal = new MarshalBase64();
                marshal.register(envelope);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                request.addProperty("f", data);
                request.addProperty("Name", params[0]);
                request.addProperty("Company", params[1]);
                request.addProperty("ContactNo", params[2]);
                request.addProperty("Dept", params[4]);


                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
                try {
                    androidHttpTransport.call(NAMESPACE + "methodname", envelope);
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }

                SoapPrimitive response;
                try {
                    response = (SoapPrimitive) envelope.getResponse();
                    responsetring = response.toString();
                } catch (SoapFault e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responsetring;
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            bar.dismiss();

            if (result.equals("Already Insided")) {

                Toast.makeText(MainActivity.this, "Your Mobile Number Already Registered...", Toast.LENGTH_SHORT).show();

            } else if (result.equals("Name status not")) {

                Toast.makeText(MainActivity.this, "Employee status not in onroll", Toast.LENGTH_SHORT).show();
                au_name.setText("");
                au_name.setFocusable(true);
                au_name.setError("Enter Meeting Staff");

            } else if (result.equals("Enter Meeting Staff in Valid Format ,  Please Select in List Shown")) {

                Toast.makeText(MainActivity.this, "Please select in list shown", Toast.LENGTH_SHORT).show();
                au_name.setText("");
                au_name.setFocusable(true);
                au_name.setError("Enter Meeting Staff");

            } else {

                ShowAlertDailogbox();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        {
                            String id = result.replace("Welcome...!*", "");

                            Toast.makeText(MainActivity.this, "Registered Successfully....Please Collect", Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(MainActivity.this, MainActivity.class);
                            i.putExtra("name", au_name.getText().toString());
                            i.putExtra("id", id);
                            startActivity(i);
                            finish();
                        }
                    }
                };
                au_name.postDelayed(runnable, 2000);
            }

        }
    }

    private void ShowAlertDailogbox() {

        dialogbox.setContentView(R.layout.dialogbox);
        dialogmessage = findViewById(R.id.textid);
        dialogbox.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogbox.show();
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);


        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
