package androidtr.es.hack.email;

/*
 * Created by Cristian Do Carmo
 * Copyright (c) 2019 . All rights reserved.
 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
import static android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = ((Activity) this).getWindow();
        View decorView = window.getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.setStatusBarColor(getColor(R.color.fondo));

        }


        if (android.os.Build.VERSION.SDK_INT >= 26) {
            window.setNavigationBarColor(getColor(R.color.fondo));
            decorView.setSystemUiVisibility(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                    SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        RelativeLayout abrirArchivo = findViewById(R.id.abrir_archivo);
        RelativeLayout abrirAndroidTR = findViewById(R.id.abrir_androidtr);
        RelativeLayout abrirDevelopmentColors = findViewById(R.id.abrir_development_colors);
        RelativeLayout noHayCorreo = findViewById(R.id.no_hay_correo);
        TextView texto = (findViewById(R.id.email));
        TextView recibido = (findViewById(R.id.recibido));

        abrirArchivo.setVisibility(View.GONE);
        noHayCorreo.setVisibility(View.GONE);


        deleteCache(this);

        animarTodasLasLineas();

        abrirAndroidTR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://androidtr.es"));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);

            }
        });

        abrirDevelopmentColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=4927050975116994908"));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);

            }
        });


        try{

            String email = getEmail(getIntent().getData().toString());

            if(email.contains("@")) {
                texto.setText(" EMAIL:   " + email + "   ");

                TextView funcion = findViewById(R.id.funcion);
                funcion.setText("getIntent().getData().toString()");

                abrirArchivo.setVisibility(View.VISIBLE);

                abrirArchivo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                        abrirArchivoSinEmail();
                    }
                });
            }else{

                texto.setText(R.string.se_ha_recibido);
                recibido.setText(getIntent().getData().toString());
                noHayCorreo.setVisibility(View.VISIBLE);

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        TextView abrirgmail = findViewById(R.id.abrir_gmail);
        abrirgmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        try {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if (dir != null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void abrirArchivoSinEmail(){

        Activity context = this;


            File file = new File(getCacheDir(), "file_"+getFileName(getIntent().getData()));

            try {
                copyInputStreamToFile(getContentResolver().openInputStream(getIntent().getData()),file);


            }catch (Exception e){
                e.printStackTrace();
            }


        Uri uri = FileProvider.getUriForFile(context, "androidtr.es.hack.email", file);

        Intent intent = ShareCompat.IntentBuilder.from(context)
                .setType("*/*")
                //.setSubject(context.getString(R.string.share_subject))
                .setStream(uri)
                //.setChooserTitle(R.string.share_title)
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.setDataAndType(uri,"*/*");
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {

        }

    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if ( out != null ) {
                    out.close();
                }
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public static String getEmail(String text){
        String email = "";

        if(text.length()>0) {
            String[] split = text.split("/");

            for (String s : split) {
                if (s.contains("@")) {
                    return s;
                }
            }
        }

        return email;
    }


    long espera_animacion = 200;

    public void animarTodasLasLineas(){

        int posicion = 1;

        findViewById(R.id.imagen_titulo).setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.imagen_titulo).setVisibility(View.VISIBLE);
            }
        },espera_animacion*9);


        posicion = animarLinea(findViewById(R.id.linea_1),posicion);
        posicion = animarLinea(findViewById(R.id.linea_2),posicion);
        posicion = animarLinea(findViewById(R.id.linea_3),posicion);
        posicion = animarLinea(findViewById(R.id.linea_4),posicion);
        posicion = animarLinea(findViewById(R.id.linea_5),posicion);
        posicion = animarLinea(findViewById(R.id.linea_6),posicion);
        posicion = animarLinea(findViewById(R.id.linea_7),posicion);
        posicion = animarLinea(findViewById(R.id.linea_8),posicion);
        posicion = animarLinea(findViewById(R.id.linea_9),posicion);
        posicion = animarLinea(findViewById(R.id.linea_10),posicion);
        posicion = animarLinea(findViewById(R.id.linea_11),posicion);
        posicion = animarLinea(findViewById(R.id.linea_12),posicion);
        posicion = animarLinea(findViewById(R.id.linea_13),posicion);
        posicion = animarLinea(findViewById(R.id.linea_14),posicion);
        posicion = animarLinea(findViewById(R.id.linea_15),posicion);
        posicion = animarLinea(findViewById(R.id.linea_16),posicion);
        posicion = animarLinea(findViewById(R.id.linea_17),posicion);
        posicion = animarLinea(findViewById(R.id.linea_18),posicion);
        posicion = animarLinea(findViewById(R.id.linea_19),posicion);
        posicion = animarLinea(findViewById(R.id.linea_20),posicion);
        posicion = animarLinea(findViewById(R.id.linea_21),posicion);
        posicion = animarLinea(findViewById(R.id.linea_22),posicion);
        posicion = animarLinea(findViewById(R.id.linea_23),posicion);
        posicion = animarLinea(findViewById(R.id.linea_24),posicion);
        posicion = animarLinea(findViewById(R.id.linea_25),posicion);
        posicion = animarLinea(findViewById(R.id.linea_26),posicion);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation parpadeo = AnimationUtils.loadAnimation(MainActivity.this, R.anim.parpadeo);
                findViewById(R.id.ultima_linea).startAnimation(parpadeo);
            }
        },espera_animacion*posicion);

    }

    public int animarLinea(final View view, int posicion){

        view.setVisibility(View.GONE);

        long retraso = espera_animacion*posicion;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
                Animation fadein = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                view.startAnimation(fadein);
            }
        },retraso);

        return posicion+1;
    }
}
