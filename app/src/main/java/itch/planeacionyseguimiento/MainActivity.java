package itch.planeacionyseguimiento;


import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    // Declaracion de variables globales

    HttpTransportSE transporte;
    SoapObject request;
    SoapSerializationEnvelope sobre;

    SQLHelper sqlhelper;
    SQLiteDatabase db;

    EditText user, pass;
    Button iniciar;

    String namespace = "urn:wsplaneacion";

    String url = "http://hernandezsomos.com/wsplaneacion/service.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ver si hay sesion inciada de un docente
        // Leemos archivo login.txt
        try
        {
            Log.e("EZH","OnCreate 1");
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("login.txt")));
            String datos = br.readLine();
            String texto_string = "";

            while(datos != null)
            {
                texto_string += datos;
                datos = br.readLine();
            }
            br.close();
            Log.e("EZH","OnCreate 2");
            // Validamos que sea diferente de Sesion finalizada
            if(!texto_string.equals("Sesion finalizada"))
            {
                // Nos movemos a la Activity Planeaciones
                // Obtenemos el id del docente
                String array_datos[] = texto_string.split("-");
                Log.e("EZH","OnCreate 3: "+texto_string);
                navigationToPlaneacion(array_datos[0]);
            }
            Log.e("EZH","OnCreate 4");
        }
        catch (Exception e)
        {
            Log.e("EZH","Oncreate  - No existe el archivo login.txt: "+e.getMessage());
            Log.e("EZH","OnCreate 5");
        }

        user = (EditText) findViewById(R.id.usuario);
        pass = (EditText) findViewById(R.id.password);

        iniciar = (Button) findViewById(R.id.btnInciar);

        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String respuesta = iniciar_sesion(user.getText().toString(), pass.getText().toString());

                if (!respuesta.equals("NO"))
                {
                    String idDocente = respuesta.substring(3, respuesta.length());

                    // Descargar informacion del Docente y guardarla en SQLite
                    // navigationToPlaneacion(idDocente);
                    descargarInfoPersona(idDocente);
                    descargarInfoDocente(idDocente);

                    descargarInfoDocenteMateria(idDocente);
                    descargarInfodocentePlaneacion(idDocente);
                    insertInfoMateria(idDocente);
                    insertMateriaPlan(idDocente);
                    insertTema();

                    // Autentificación correcta y descarga de informacion exitosa
                    // Pasamos a la Activity de Planeaciones
                    // Guardamos sesion del docente en un archivo de texto
                    try
                    {
                        OutputStreamWriter osw = new OutputStreamWriter(openFileOutput("login.txt",MODE_PRIVATE));
                        osw.write(idDocente+"-"+user.getText().toString()+"-"+pass.getText().toString());
                        osw.flush();
                        osw.close();

                        // Pasamos a la Activity Planeaiones
                        navigationToPlaneacion(idDocente);

                    }
                    catch (Exception e)
                    {
                        Log.e("EZH"," - OnCreate - iniciar.setOnClickListener : "+e.getMessage());
                    }


                    Toast.makeText(getApplicationContext(), "¡Autentificación Correcta!", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(), "¡Autentificación Erronea!", Toast.LENGTH_LONG).show();
                }


            }
        });

    }

    public void insertTema()
    {
        // Borramos la informacion de la tabla tema
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from tema");
        Log.e("EZH","insertTema 1");
        // Consultamos los tema de cada materia
        try
        {
            String consulta = "SELECT temaId FROM materiatema";
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","insertTema 2");
            if (c.moveToFirst())
            {
                Log.e("EZH","insertTema 3");
                do
                {
                    String idTema = c.getString(0);
                    // Llamamos al metodo descargaInfoTema
                    descargarInfoTema(idTema);
                }
                while (c.moveToNext());
                Log.e("EZH","insertTema 4");
            }
            db.close();
            Log.e("EZH","insertTema 5");
        }
        catch (Exception e)
        {
            Log.e("EZH","Error - insertTema: "+e.getMessage());
        }

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfoTema(String temaId)
    {
        Log.e("EZH","descargarInfoTema 1");
        sqlhelper = new SQLHelper(this);
        String accionSoap = "urn:infoTema#metodoinfoTema";
        String metodo = "infoTema";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfoTema 2");
            //Mandamos datos al WebService
            request.addProperty("temaId",temaId);

            Log.e("EZH","descargarInfoTema 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfoTema 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfoTema 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfoTema 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfoTema 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfoTema 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfoTema 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfoTema 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfoTema 11 "+cadena);

            // Validamos que la respuesta sea diferente de NO
            if(!cadena.equals("NO"))
            {
                // Guardamos los datos en la tabla tema
                db = sqlhelper.getWritableDatabase();
                //Separamos la respuesta por Tokens
                StringTokenizer st = new StringTokenizer(cadena,"$");

                while(st.hasMoreElements())
                {
                    String idTema = st.nextToken();
                    String tema = st.nextToken();
                    String actAprendizaje = st.nextToken();
                    String competencia = st.nextToken();
                    String subtemas = st.nextToken();

                    // Registramos la informacion
                    String insert = "insert into tema (temaId, tema, temaActividadesAprendizaje, temaCompetenciaGenerica, temaSubtemas) values ('"+idTema+"', '"+tema+"', '"+actAprendizaje+"', '"+competencia+"', '"+subtemas+"')";

                    db.execSQL(insert);
                }
                db.close();
            }
        }
        catch (Exception e)
        {
            Log.e("EZH","Error - descargarInfoTema: "+e.getMessage());
        }

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descagarInfoMateriaTema(String claveMateria)
    {
        Log.e("EZH","descagarInfoMateriaTema 1");
        sqlhelper = new SQLHelper(this);
        String accionSoap = "urn:infoMateriaTema#metodoinfoMateriaTema";
        String metodo = "infoMateriaTema";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descagarInfoMateriaTema 2");
            //Mandamos datos al WebService
            request.addProperty("claveMateria",claveMateria);

            Log.e("EZH","descagarInfoMateriaTema 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descagarInfoMateriaTema 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descagarInfoMateriaTema 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descagarInfoMateriaTema 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descagarInfoMateriaTema 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descagarInfoMateriaTema 8");

            transporte.debug = true;

            Log.e("EZH","descagarInfoMateriaTema 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descagarInfoMateriaTema 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descagarInfoMateriaTema 11 "+cadena);

            // Validamos que la respuesta sea diferente de NO
            if(!cadena.equals("NO"))
            {
                // Guardamos los datos en la tabla materiatema
                db = sqlhelper.getWritableDatabase();
                // Separamos la respuesta en Tokens
                StringTokenizer st = new StringTokenizer(cadena,"$");
                //Recorremos Tokens
                while (st.hasMoreElements())
                {
                    String idTema = st.nextToken();

                    String insert = "insert into materiatema (materiaClave, temaId) values ('"+claveMateria+"', '"+idTema+"')";
                    db.execSQL(insert);
                }
                db.close();
            }
        }
        catch (Exception e)
        {
            Log.e("EZH","Error - descagarInfoMateriaTema: "+e.getMessage());
        }
    }

    public void insertMateriaPlan(String idDocente)
    {
        // Borramos los datos de la tabla materiaplan
        Log.e("EZH","insertMateriaPlan 1");
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from materiaplan");
        db.execSQL("delete from materiatema");
        db.close();

        // Ahora consultamos las materias asignadas de la tabla docentemateria
        try
        {
            db = sqlhelper.getWritableDatabase();
            Log.e("EZH","insertMateriaPlan 2");

            // Preparamos consulta
            String consulta = "SELECT materiaClave FROM docentemateria WHERE docenteId = "+idDocente;
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","insertMateriaPlan 2");
            // Validamos que se obtuvo informacion
            if(c.moveToFirst())
            {
                do
                {
                    String cm = c.getString(0);
                    // Llamamos al metodo descargarInfomateria
                    descargarInfoMateriaPlan(cm);
                    descagarInfoMateriaTema(cm);
                }
                while(c.moveToNext());
            }
            // Cerramos la base de datos
            db.close();
            Log.e("EZH","insertMateriaPlan 3");
        }catch (Exception e)
        {
            Log.e("EZH","Error - insertMateriaPlan: "+e.getMessage());
        }


    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfoMateriaPlan(String claveMateria)
    {
        Log.e("EZH","descargarInfoMateriaPlan 1");
        sqlhelper = new SQLHelper(this);
        String accionSoap = "urn:infoMateriaPlan#metodoinfoMateriaPlan";
        String metodo = "infoMateriaPlan";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfoMateriaPlan 2");
            //Mandamos datos al WebService
            request.addProperty("claveMateria",claveMateria);

            Log.e("EZH","descargarInfoMateriaPlan 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfoMateriaPlan 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfoMateriaPlan 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfoMateriaPlan 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfoMateriaPlan 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfoMateriaPlan 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfoMateriaPlan 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfoMateriaPlan 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfoMateriaPlan 11 "+cadena);

            // Validamos que la respuesta sea diferente de No
            if(!cadena.equals("NO"))
            {
                // Guardamos datos en la tabla materiaplan
                db = sqlhelper.getWritableDatabase();
                Log.e("EZH","descargarInfoMateriaPlan 12");
                // Separamos por Tokens la respuesta
                StringTokenizer st = new StringTokenizer(cadena,",");
                // Recorremos datos
                Log.e("EZH","descargarInfoMateriaPlan 13");
                while(st.hasMoreElements())
                {
                    StringTokenizer st2 = new StringTokenizer(st.nextToken(),"$");
                    String planEstudio = st2.nextToken();
                    String cm = st2.nextToken();

                    String insert = "insert into materiaplan (planestudioClave, materiaClave) values ('"+planEstudio+"', '"+cm+"')";
                    Log.e("EZH","Insert: "+insert);
                    db.execSQL(insert);
                }

                db.close();
                Log.e("EZH","descargarInfoMateriaPlan 14");
            }
        }
        catch (Exception e)
        {
            Log.e("EZH","Error - descargarInfoMateriaPlan: "+e.getMessage());
        }

    }
    public void insertInfoMateria(String idDocente)
    {
        Log.e("EZH","insertInfoMateria 1");
        // Borramos los datos de la tabla materia
        sqlhelper = new SQLHelper (this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from materia");
        db.close();

        // Ahora las materias asignadas de la tabla docentemateria
        try
        {
            db = sqlhelper.getWritableDatabase();
            Log.e("EZH","insertInfoMateria 2");

            // Preparamos consulta
            String consulta = "SELECT materiaClave FROM docentemateria WHERE docenteId = "+idDocente;
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","insertInfoMateria 3");
            // Validamos que se obtuvo informacion
            if(c.moveToFirst())
            {
                Log.e("EZH","insertInfoMateria 4");
                do
                {
                    String cm = c.getString(0);
                    // Llamamos al metodo descargarInfomateria
                    descargarInfomateria(cm);
                }
                while(c.moveToNext());
                Log.e("EZH","insertInfoMateria 5");
            }
            // Cerramos la base de datos
            db.close();
            Log.e("EZH","insertInfoMateria 6");
        }
        catch(Exception e)
        {
            Log.e("EZH","Error - insertInfoMateria: "+e.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfomateria(String claveMateria)
    {
        Log.e("EZH","descargarInfomateria 1");
        sqlhelper = new SQLHelper(this);
        String accionSoap = "urn:infoMateria#metodoinfoMateria";
        String metodo = "infoMateria";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfomateria 2");
            //Mandamos datos al WebService
            request.addProperty("claveMateria",claveMateria);

            Log.e("EZH","descargarInfomateria 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfomateria 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfomateria 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfomateria 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfomateria 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfomateria 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfomateria 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfomateria 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfomateria 11 "+cadena);

            // Validamos que la respuesta sea diferente de No
            if(!cadena.equals("NO"))
            {
                // Guardamos datos en la tabla materia
                db = sqlhelper.getWritableDatabase();

                // Separamos el resultado en Tokens
                StringTokenizer st = new StringTokenizer(cadena,"$");

                String cm = st.nextToken();
                String nm = st.nextToken();
                String creditos = st.nextToken();
                String intencion = st.nextToken();
                String competencia = st.nextToken();
                String caracterizacion = st.nextToken();
                String fuentesInfo = st.nextToken();
                String apoyoDidactico = st.nextToken();

                String insert = "insert into materia (materiaClave, materiaNombre, materiaCreditos, materiaIntencion, materiaCompetencia, materiaCaracterizacion, materiaFuentesInformacion, materiaApoyoDidactico) " +
                        "values ('"+cm+"', '"+nm+"', '"+creditos+"', '"+intencion+"', '"+competencia+"', '"+caracterizacion+"', '"+fuentesInfo+"', '"+apoyoDidactico+"')";
                db.execSQL(insert);

                // Cerramos la base de datos
                db.close();

            }
        }
        catch(Exception e)
        {
            Log.e("EZH","Error - descargarInfomateria: "+e.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfodocentePlaneacion(String idDocente)
    {
        Log.e("EZH","descargarInfodocentePlaneacion 1");
        sqlhelper = new SQLHelper (this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from docenteplaneacion");
        db.close();

        String accionSoap = "urn:infodocentePlaneacion#metodoinfodocentePlaneacion";
        String metodo = "infodocentePlaneacion";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfodocentePlaneacion 2");
            //Mandamos datos al WebService
            request.addProperty("idDocente",idDocente);

            Log.e("EZH","descargarInfodocentePlaneacion 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfodocentePlaneacion 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfodocentePlaneacion 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfodocentePlaneacion 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfodocentePlaneacion 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfodocentePlaneacion 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfodocentePlaneacion 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfodocentePlaneacion 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfodocentePlaneacion 11 "+cadena);

            // Validamos que la respuesta sea diferente de No
            if(!cadena.equals("NO"))
            {
                // Guardamos datos en la tabla docenteplaneacion
                db = sqlhelper.getWritableDatabase();
                // Separamos por Tokens
                StringTokenizer st = new StringTokenizer(cadena,",");
                // Recorremos el StringTokenizer
                while (st.hasMoreElements())
                {
                    StringTokenizer st1 = new StringTokenizer(st.nextToken(),"_");
                    String docentePlaneacionId = st1.nextToken();
                    String idPlaneacion = st1.nextToken();
                    String iddocente = st1.nextToken();
                    String claveMateria =  st1.nextToken();
                    String periodo = st1.nextToken();

                    String insert = "insert into docenteplaneacion (planeacionId, docenteId, materiaClave, docenteplaneacionPeriodo) values ('"+idPlaneacion+"', '"+iddocente+"', '"+claveMateria+"', '"+periodo+"')";

                    db.execSQL(insert);
                }
                db.close();
            }

        }
        catch (Exception e)
        {
            Log.e("EZH","Error - descargarInfodocentePlaneacion: "+e.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfoDocenteMateria(String idDocente)
    {
        Log.e("EZH","descargarInfoDocenteMateria 1");
        sqlhelper = new SQLHelper (this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from docentemateria");
        db.close();

        String accionSoap = "urn:infodocenteMateria#metodoinfodocenteMateria";
        String metodo = "infodocenteMateria";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfoDocenteMateria 2");
            //Mandamos datos al WebService
            request.addProperty("idDocente",idDocente);

            Log.e("EZH","descargarInfoDocenteMateria 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfoDocenteMateria 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfoDocenteMateria 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfoDocenteMateria 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfoDocenteMateria 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfoDocenteMateria 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfoDocenteMateria 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfoDocenteMateria 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfoDocenteMateria 11 "+cadena);

            // Validamos que la respuesta sea diferente de No
            if(!cadena.equals("NO"))
            {
                // Guardamos datos en la tabla docentemateria
                db = sqlhelper.getWritableDatabase();
                StringTokenizer st1 = new StringTokenizer(cadena,",");

                // Recorremos StringTokenizer
                while(st1.hasMoreElements())
                {
                    StringTokenizer st2 = new StringTokenizer(st1.nextToken(),"_");
                    String iddocente = st2.nextToken();
                    String claveMateria = st2.nextToken();

                    String insert = "insert into docentemateria (docenteId, materiaClave) values ('"+iddocente+"', '"+claveMateria+"')";

                    db.execSQL(insert);

                }
                // Cerramos la base de datos
                db.close();
            }

        }
        catch(Exception e)
        {
            Log.e("EZH","Error - descargarInfoDocenteMateria: "+e.getMessage());
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfoDocente(String idDocente)
    {
        Log.e("EZH","descargarInfoDocente 1");
        sqlhelper = new SQLHelper (this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from docente");
        db.close();

        String accionSoap = "urn:infoDocente#metodoinfoDocente";
        String metodo = "infoDocente";
        String cadena = "";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfoDocente 2");
            //Mandamos datos al WebService
            request.addProperty("idDocente",idDocente);

            Log.e("EZH","descargarInfoDocente 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfoDocente 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfoDocente 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfoDocente 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfoDocente 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfoDocente 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfoDocente 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfoDocente 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfoDocente 11 "+cadena);

            // Validamos que la respuesta sea diferente de No
            if(!cadena.equals("NO"))
            {
                // Guardamos datos en la tabla docente
                db = sqlhelper.getWritableDatabase();
                StringTokenizer st1 = new StringTokenizer(cadena,"$");

                String id = st1.nextToken();
                String telefono = st1.nextToken();
                String email = st1.nextToken();
                String usuario = st1.nextToken();
                String password = st1.nextToken();

                String insert = "insert into docente (personaId, docenteTelefono, docenteEmail, docenteUsuario, docentePassword) values ('"+id+"', '"+telefono+"', '"+email+"', '"+usuario+"', '"+password+"')";

                db.execSQL(insert);

                // Cerramos la base de datos
                db.close();
            }
        }
        catch(Exception e)
        {
            Log.e("EZH","Error - descargarInfoDocente: "+e.getMessage());
        }


    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void descargarInfoPersona(String idDocente)
    {

        Log.e("EZH","descargarInfoPersona 1");
        sqlhelper = new SQLHelper (this);
        db = sqlhelper.getWritableDatabase();
        db.execSQL("delete from persona");
        db.close();

        String accionSoap = "urn:infoPersona#metodoinfoPersona";
        String metodo = "infoPersona";
        String cadena = "";


        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","descargarInfoPersona 2");
            //Mandamos datos al WebService
            request.addProperty("idDocente",idDocente);

            Log.e("EZH","descargarInfoPersona 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","descargarInfoPersona 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","descargarInfoPersona 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","descargarInfoPersona 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","descargarInfoPersona 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","descargarInfoPersona 8");

            transporte.debug = true;

            Log.e("EZH","descargarInfoPersona 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","descargarInfoPersona 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","descargarInfoPersona 11 "+cadena);

            // Validamos que la respuesta sea diferente de No
            if(!cadena.equals("NO"))
            {
                // Guardamos datos en la tabla persona
                db = sqlhelper.getWritableDatabase();
                StringTokenizer st1 = new StringTokenizer(cadena,"_");
                String id = st1.nextToken();
                String rfc = st1.nextToken();
                String nombreDocente = st1.nextToken();
                String apellidoPaterno = st1.nextToken();
                String apellidoMaterno = st1.nextToken();

                Log.e("EZH","Datos obtenidos: "+id+" "+rfc+" "+" "+nombreDocente+" "+apellidoPaterno+" "+apellidoMaterno);
                // Preparamos sentencia sql
                String insert = "insert into persona (personaId, personaRfc, personaNombre, personaApellidoPaterno, personaApellidoMaterno) values ('"+id+"', '"+rfc+"', '"+nombreDocente+"', '"+apellidoPaterno+"', '"+apellidoMaterno+"')";

                db.execSQL(insert);

                // Cerramos base de datos
                db.close();

            }

        }
        catch(Exception e)
        {
            Log.e("EZH","Error - descargarInfoPersona: "+e.getMessage());
        }


    }


    public void navigationToPlaneacion(String idDocente)
    {
        // Pasar al Activity Planeaciones
        Intent intent = new Intent(getApplicationContext(),Planeaciones.class);
        intent.putExtra("idDocente", idDocente);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public String iniciar_sesion(String usuario, String password)
    {
        Log.e("EZH","Iniciar sesion 1");
        String accionSoap = "urn:loginDocentewsdl#metodoLogin";
        String metodo = "loginDocente";
        String cadena = "NO";

        try
        {
            request = new SoapObject(namespace, metodo);
            Log.e("EZH","Iniciar sesion 2");
            //Mandamos datos al WebService
            request.addProperty("user",usuario);
            request.addProperty("password",password);
            Log.e("EZH","Iniciar sesion 3");

            sobre = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);

            Log.e("EZH","Iniciar sesion 4");

            sobre.setOutputSoapObject(request);

            Log.e("EZH","Iniciar sesion 5");

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            Log.e("EZH","Iniciar sesion 6");

            StrictMode.setThreadPolicy(policy);

            Log.e("EZH","Iniciar sesion 7");
            transporte = new HttpTransportSE(url);

            Log.e("EZH","Iniciar sesion 8");

            transporte.debug = true;

            Log.e("EZH","Iniciar sesion 9");

            transporte.call(accionSoap, sobre);

            Log.e("EZH","Iniciar sesion 10");
            cadena = sobre.getResponse().toString();

            Log.e("EZH","Iniciar sesion 11 "+cadena);



        }
        catch(Exception e)
        {
            Toast.makeText(this,"¡Error de conexión a la red!",Toast.LENGTH_LONG).show();
            Log.e("EZH","Error - Iniciar sesion: "+e.getMessage());
        }
        return cadena;
    }
}
