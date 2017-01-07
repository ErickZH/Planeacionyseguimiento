package itch.planeacionyseguimiento;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class Planeaciones extends AppCompatActivity {


    private final String NOMBRE_CARPETA = "planeaciones";
    ArrayList<String> idPlaneaciones;
    private final String GENERADOS = "MisPlaneaciones";

    SQLHelper sqlhelper;
    SQLiteDatabase db;

    ListView lsv;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planeaciones);
        // Mapeamos xml con Java :3
        Log.e("EZH","onCreate 1");
        lsv = (ListView)findViewById(R.id.lsvw);

        actualiza_list_view(getPlaneaciones());
        Log.e("EZH","onCreate 2");
        // Asignamos OnItemClick Listener al ListView
        lsv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = lsv.getItemAtPosition(position).toString();
                Log.e("EZH","onCreate 3 ");
                Log.e("EZH","item: "+item);
                if(!item.equals("SIN PLANEACIONES"))
                {
                    // Obtenemos datos
                    String idPlaneacion = idPlaneaciones.get(position);
                    // Separamos item por guion intermedio
                    String datos[] = item.split(",");
                    String claveMateria = datos[0];
                    String nombreMateria = datos[1];
                    String periodo = datos[2];
                    periodo = periodo.replace(" ","");
                    // Lanzamos alerta
                    lanzar_alerta(idPlaneacion,claveMateria,nombreMateria,periodo);
                }
            }
        });




    }

    public void lanzar_alerta(final String idPlaneacion, final String cm, final String nm, final String p)
    {
        // Creamos la caja de dialogo
        AlertDialog.Builder alert = new AlertDialog.Builder(Planeaciones.this);

        // Asignamos titulo a nuestra caja de dialogo
        alert.setTitle("¿Que desea realizar con la planeación?");
        // Asignamos el mesaje a mostrar
        alert.setMessage(cm+" - "+nm);
        // La caja de dialogo podra ser cancelada
        alert.setCancelable(true);
        // Asignamos boton positivo en este caso sera para ver la planeacion
        alert.setPositiveButton("Ver", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Llamar al metodo para visualizar la planeacion
                visualiza_planeacion(cm,p);
            }
        });

        // Asignamos boton negativo que en este caso sera para eliminar la planeacion
        alert.setNegativeButton("Eliminar", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Llamamos metodo para eliminar la planeacion
            }
        });

        //Mostramos la alerta
        alert.show();
    }

    public void visualiza_planeacion(String cm, String p)
    {
        Log.e("EZH","visualiza_planeacion 1");

        Toast.makeText(this,"Leyendo planeación",Toast.LENGTH_LONG).show();
        String NOMBRE_ARCHIVO = cm+"_"+p+".pdf";
        // Obtenemos nombre completo
        String nombre_completo = Environment.getExternalStorageDirectory().toString()
                + File.separator + NOMBRE_CARPETA + File.separator + GENERADOS+ File.separator + NOMBRE_ARCHIVO;
        File file = new File(nombre_completo);
        Log.e("EZH","visualiza_planeacion 2");
        Log.e("EZH","Ruta: "+nombre_completo);

        // Creamos una intencion para abrir una aplicacion lectora de pdf
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),"application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.e("EZH","visualiza_planeacion 3");
        // Abrimos Try -  Catch para controlar excepciones
        try
        {
            startActivity(intent);
            Log.e("EZH","visualiza_planeacion 4");
        }
        catch (ActivityNotFoundException e)
        {
            Log.e("EZH","visualiza_planeacion 5");
            Log.e("EZH","Error visualiza_planeacion: "+e.getMessage());
            Toast.makeText(this,"No tiene una aplicación para abrir este tipo de archivo",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_planeacion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
            case R.id.generar_planeacion:
                // Ir a la Activity GenerarPlaneacion
                Intent intent = new Intent(getApplicationContext(), GenerarPlaneacion.class);

                String t = (String)getIntent().getExtras().getSerializable("idDocente");
                intent.putExtra("idDocente", t);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
        {
            // actualizamos el list view
            actualiza_list_view(getPlaneaciones());
        }
    }

    public void actualiza_list_view(String planeaciones)
    {
        Log.e("EZH","actualiza_list_view 1");

        // Tratamos las planeaciones
        if (!planeaciones.equals("NO"))
        {
            // Separamos por ,
            String array_planeaciones [] = planeaciones.split(",");
            // Creamos ArrayList
            ArrayList<String> items =
                    new ArrayList<String>(array_planeaciones.length);
            idPlaneaciones = new ArrayList<String>();
            Log.e("EZH","actualiza_list_view 2");


            Log.e("EZH","actualiza_list_view 3");
            //Recorremos el array_planeaciones
            for(String values:array_planeaciones)
            {
                // Separamos por _
                String datos[] = values.split("_");
                // Obtenemos los id de las planeaciones

                // Agregamos los datos que se mostraran en el ListView
                Log.e("EZH","DATOS: "+datos[0]+", "+datos[1]+", "+datos[2]+", "+datos[3]);
                idPlaneaciones.add(datos[0]);
                // Agregamos al ArrayList items los datos a mostrar
                items.add(datos[1]+", "+datos[2]+", "+datos[3]);

            }
            // Creamos Adapter para el ListView
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            items);
            lsv.setAdapter(adapter);
            Log.e("EZH","actualiza_list_view 4");

        }
        else
        {
            ArrayList<String> arreglo = new ArrayList<String>(1);
            arreglo.add("SIN PLANEACIONES");
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            arreglo);
            lsv.setAdapter(adapter);
            Log.e("EZH","actualiza_list_view 6");
        }

    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public String getPlaneaciones()
    {
        Log.e("EZH","getPlaneaciones 1");
        String id = (String)getIntent().getExtras().getSerializable("idDocente");

        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();

        String cadena = "";
        Log.e("EZH","getPlaneaciones 2");
        try
        {
            // Preparamos consulta
            String consulta = "SELECT docenteplaneacion.planeacionId,docenteplaneacion.materiaClave, materia.materiaNombre, docenteplaneacion.docenteplaneacionPeriodo FROM docenteplaneacion, materia WHERE docenteplaneacion.materiaClave = materia.materiaClave AND docenteplaneacion.docenteId = "+id;
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","getPlaneaciones 3");
            if(c.moveToFirst())
            {
                do
                {
                    String idPlaneacion = c.getString(0);
                    String mc = c.getString(1);
                    String mn = c.getString(2);
                    String periodo = c.getString(3);

                    cadena += idPlaneacion+"_"+mc+"_"+mn+"_"+periodo+",";
                }
                while (c.moveToNext());
                Log.e("EZH","getPlaneaciones 4");
            }
            else
            {
                cadena = "NO";
            }
            Log.e("EZH","getPlaneaciones 5");
        }
        catch(Exception e)
        {
            Log.e("EZH","Error: "+e.getMessage());
        }
        Log.e("EZH","getPlaneaciones "+cadena);
        return cadena;
    }

}
