package com.waslabank.wasslabank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.waslabank.wasslabank.networkUtils.Connector;
import com.waslabank.wasslabank.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import butterknife.BindView;
import butterknife.ButterKnife;



public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.profile_image)
    ImageView mProfileImageView;
    @BindView(R.id.f_name)
    EditText mFirstNameEditText;
    @BindView(R.id.l_name)
    EditText mLastNameEditText;
    @BindView(R.id.password)
    EditText mPasswordEditText;
    @BindView(R.id.confirm_password)
    EditText mPasswordConfirmEditText;
    @BindView(R.id.mobile)
    EditText mMobileEditText;
    @BindView(R.id.email)
    EditText mEmailEditText;
    @BindView(R.id.sign_up_button)
    Button mSignUpButton;

    String mImagePath = null;

    ProgressDialog mProgressDialog;
    File mSelectedFile;

    private final String TAG = SignUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);



        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });


        mSignUpButton.setOnClickListener(view -> {
            if (mImagePath == null){
                Helper.showSnackBarMessage(getString(R.string.upload_your_image),SignUpActivity.this);
            } else if (mFirstNameEditText.getText().toString().isEmpty()){
                Helper.showSnackBarMessage(getString(R.string.enter_first_name),SignUpActivity.this);
            } else if (mLastNameEditText.getText().toString().isEmpty()){
                Helper.showSnackBarMessage(getString(R.string.enter_last_name),SignUpActivity.this);
            } else if (mPasswordEditText.getText().toString().isEmpty()){
                Helper.showSnackBarMessage(getString(R.string.enter_password),SignUpActivity.this);
            } else if (mPasswordConfirmEditText.getText().toString().isEmpty()){
                Helper.showSnackBarMessage(getString(R.string.enter_password_confirm),SignUpActivity.this);
            } else if (!mPasswordConfirmEditText.getText().toString().equals(mPasswordEditText.getText().toString())){
                Helper.showSnackBarMessage(getString(R.string.password_match),SignUpActivity.this);
            } else if (mMobileEditText.getText().toString().isEmpty()){
                Helper.showSnackBarMessage(getString(R.string.mobile),SignUpActivity.this);
            } else if (mEmailEditText.getText().toString().isEmpty()){
                Helper.showSnackBarMessage(getString(R.string.enter_email),SignUpActivity.this);
            } else {
                Intent intent = new Intent(SignUpActivity.this,PhoneVerificationActivity.class);
                intent.putExtra("firstName",mFirstNameEditText.getText().toString());
                intent.putExtra("lastName",mLastNameEditText.getText().toString());
                intent.putExtra("password",mPasswordEditText.getText().toString());
                intent.putExtra("email",mEmailEditText.getText().toString());
                intent.putExtra("mobile","+2" + mMobileEditText.getText().toString());
                intent.putExtra("image",mImagePath);
                startActivity(intent);
            }
        });
    }

    private void pickImage() {
        ImagePicker.create(this)
                .folderMode(true) // folder mode (false by default)
                .toolbarFolderTitle(getString(R.string.image_folder)) // folder selection title
                .toolbarImageTitle(getString(R.string.select_image)) // image selection title
                .limit(1) //  Max images can be selected
                .single() //single mode
                .showCamera(true) // show camera or not (true by default)
                .start(); // start image picker activity with Request code
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image img = ImagePicker.getFirstImageOrNull(data);
            try {
                Bitmap bitmapImage = getBitmap(img.getPath());
                mProfileImageView.setImageBitmap(bitmapImage);
                mSelectedFile = bitmapToFile(img.getName(), checkImage(img.getPath(), getBitmap(img.getPath())));
                UploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void UploadImage() {
        mProgressDialog = Helper.showProgressDialog(this,
                getString(R.string.loading),
                false);

        mProgressDialog.show();
        Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setTrustManagers(new TrustManager[] {new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {}

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }});

        Ion.with(this)
                .load("https://www.cta3.com/waslabank/api/upload_image_api")
                .setMultipartFile("parameters[0]", "image", mSelectedFile)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Helper.writeToLog(result);
                        if (Connector.checkImages(result)) {
                            mProgressDialog.dismiss();
                            ArrayList<String> imagePaths = Connector.getImages(result);
                            mImagePath = imagePaths.get(0);
                        } else {
                            Log.d("TTTTT", "onCompleted: exception "+e.getMessage());
                            Helper.showSnackBarMessage(getString(R.string.error), SignUpActivity.this);
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }

    private Bitmap getBitmap(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return getResizedBitmap(bitmap, 600);
    }

    private File bitmapToFile(String name, Bitmap bmap) throws IOException {
        if (SignUpActivity.this.getExternalCacheDir() != null) {
            File f = new File(SignUpActivity.this.getExternalCacheDir().getAbsolutePath() + "/" + name);
            boolean crated = f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return f;
        }
        return null;
    }

    private Bitmap checkImage(String path, Bitmap bitmap) throws IOException {

        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap = null;


        return bitmap;
    }

    private Bitmap getBitmapFromPath(String path, int size) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return getResizedBitmap(bitmap, size);
    }


    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public void openFirstName(View view) {
        mFirstNameEditText.setFocusableInTouchMode(true);
        mFirstNameEditText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mFirstNameEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void openLastName(View view) {
        mLastNameEditText.setFocusableInTouchMode(true);
        mLastNameEditText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mLastNameEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void openPassword(View view) {
        mPasswordEditText.setFocusableInTouchMode(true);
        mPasswordEditText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mPasswordEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void openPasswordConfirm(View view) {
        mPasswordConfirmEditText.setFocusableInTouchMode(true);
        mPasswordConfirmEditText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mPasswordConfirmEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void openPhoneNumber(View view) {
        mMobileEditText.setFocusableInTouchMode(true);
        mMobileEditText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mMobileEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void openEmail(View view) {
        mEmailEditText.setFocusableInTouchMode(true);
        mEmailEditText.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) SignUpActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(mEmailEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
