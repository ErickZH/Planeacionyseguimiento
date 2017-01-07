
package itch.planeacionyseguimiento;


import android.content.ContentValues;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;


public class GenerarPlaneacion extends AppCompatActivity
{

    ArrayList<String> m;
    ArrayList<String> cm;

    Spinner materias, periodo;

    Button generar;

    SQLHelper sqlhelper;
    SQLiteDatabase db;


    private final String NOMBRE_CARPETA = "planeaciones";

    private final String GENERADOS = "MisPlaneaciones";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_planeacion);


        // Recibimos datos del Intent
        final String idDocente = (String)getIntent().getExtras().getSerializable("idDocente");
        // Mapeamos xml
        periodo = (Spinner) findViewById(R.id.spinnerPeriodo);

        generar = (Button) findViewById(R.id.generar);

        materias = (Spinner)findViewById(R.id.spinnerMaterias);


        cargarMaterias(getMateriasAsignadas(idDocente));

        // Asignamos evento OnClick al boton generar
        generar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                // Obtenemos datos seleccionados
                String nombreMateria = materias.getSelectedItem().toString();
                String pe = periodo.getSelectedItem().toString();

                // Obtenemos el año del sistema
                Calendar calendar = Calendar.getInstance();
                String annio = Integer.toString(calendar.get(Calendar.YEAR));
                // Concatenamos año con el periodo
                String pinsert = pe + " "+annio;
                String pplaneacion = pe + ""+annio;

                String respuesta = insertPlaneacion(idDocente,nombreMateria,pinsert);
                if (!respuesta.equals("NO"))
                {
                    // Llamamos metodo que genera la planeacion (Instrumentacion didactica uwu)
                    String idPlaneacion = respuesta.substring(3, respuesta.length());
                    generarPlaneacion(idPlaneacion,nombreMateria,pplaneacion);
                    irAplaneaciones(idDocente);
                }

            }
        });


    }

    public String insertPlaneacion(String idDocente, String nombreMateria, String periodo)
    {
        // Insertamos en la tabla docenteplaneacion
        Log.e("EZH","insertPlaneacion 1");

        String respuesta = "";
        try
        {
            sqlhelper = new SQLHelper(this);
            db = sqlhelper.getWritableDatabase();
            db.close();
            Log.e("EZH","insertPlaneacion 2");
            // Llamamos al metodo getClaveMateria
            String claveMateria = getClaveMateria(nombreMateria);
            db = sqlhelper.getWritableDatabase();
            // Preparamos insert
            String insert = "insert into docenteplaneacion (planeacionId, docenteId, materiaClave, docenteplaneacionPeriodo) values (null, '"+idDocente+"', '"+claveMateria+"', '"+periodo+"')";
            Log.e("EZH","INSERT: "+insert);
            Log.e("EZH","insertPlaneacion 3");

            db.execSQL(insert);
            Log.e("EZH","insertPlaneacion 4");
            // Obtenemos el id generado
            String consulta = "select max(planeacionId) from docenteplaneacion";
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","insertPlaneacion 5");
            if(c.moveToFirst())
            {
                Log.e("EZH","insertPlaneacion 6");
                String idPlaneacion = c.getString(0);
                respuesta = "OK,"+idPlaneacion;
                Log.e("EZH","respuesta: "+respuesta);
            }
            else
            {
                respuesta = "NO";
            }

            Log.e("EZH","insertPlaneacion 7");
            db.close();
            Log.e("EZH","insertPlaneacion 8");

        }
        catch (Exception e)
        {
            Log.e("EZH","Error insertPlaneacion: "+e.getMessage());
            respuesta = "NO";
        }

        return respuesta;
    }

    public String getClaveMateria(String nombreMateria)
    {
        Log.e("EZH","getClaveMateria 1");
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        String cadena = "";
        // Obtenemos la clave de la materia
        try
        {
            // Preparamos consulta
            String consulta = "select materiaClave from materia where materiaNombre = '"+nombreMateria+"'";
            Cursor cursor = db.rawQuery(consulta,null);
            Log.e("EZH","getClaveMateria 2");
            // Validamos que se obtuvo la clave de la materia
            if (cursor.moveToFirst())
            {
                Log.e("EZH","getClaveMateria 3");
                do
                {
                    String claveMateria = cursor.getString(0);
                    cadena = claveMateria;
                }
                while (cursor.moveToNext());
                Log.e("EZH","getClaveMateria 4");
            }
            else
            {
                Log.e("EZH","getClaveMateria 5");
                cadena = "NO";
            }
            db.close();

        }
        catch (Exception e)
        {
            Log.e("EZH","Error getClaveMateria: "+e.getMessage());
        }
        return cadena;

    }

    public String configuracionDirectorios(String nm, String periodo)
    {
        Log.e("EZH","configuracionDirectorios 1");
        String cm = getClaveMateria(nm);
        String NOMBRE_ARCHIVO = cm+"_"+periodo+".pdf";

        // Obtenemos direccion raiz de la tarjeta de memoria
        String tarjeta = Environment.getExternalStorageDirectory().toString();
        Log.e("EZH","configuracionDirectorios 2");
        // Creamos directorio planeaciones
        File pdfDir = new File(tarjeta + File.separator + NOMBRE_CARPETA);
        if (!pdfDir.exists())
        {
            pdfDir.mkdir();
        }
        Log.e("EZH","configuracionDirectorios 3");
        // Creamos Sub Directorio
        File pdfSubDir = new File(pdfDir.getPath() + File.separator + GENERADOS);
        if(!pdfSubDir.exists())
        {
            pdfSubDir.mkdir();
        }
        Log.e("EZH","configuracionDirectorios 4");
        // Obtenemos nombre completo
        String nombre_completo = Environment.getExternalStorageDirectory().toString()
                + File.separator + NOMBRE_CARPETA + File.separator + GENERADOS+ File.separator + NOMBRE_ARCHIVO;
        File output_file = new File(nombre_completo);
        Log.e("EZH","configuracionDirectorios 5");
        // Eliminar el archivo si existe
        if(output_file.exists())
        {
            output_file.delete();
        }
        Log.e("EZH","configuracionDirectorios 6");
        Log.e("EZH","nombre completo: "+nombre_completo);
        return nombre_completo;
    }
    public void generarPlaneacion(String idPlaneacion, String nm, String periodo)
    {
        Log.e("EZH","generarPlaneacion 1");
        String nombre_completo = configuracionDirectorios(nm,periodo);
        try
        {
            Log.e("EZH","generarPlaneacion 2");
            // Generamos pdf
            // Creamos un Document
            Document document = new Document(PageSize.LETTER.rotate());
            Log.e("EZH","generarPlaneacion 3");
            // Creamos pdfWriter
            PdfWriter pdfWriter = PdfWriter.getInstance(document,new FileOutputStream(nombre_completo));
            Log.e("EZH","generarPlaneacion 4");
            // Abrimos el documento
            document.open();
            Log.e("EZH","generarPlaneacion 5");
            // Agregamos encabezado al pdf
            // Insertamos una imagen que se encuentra en los recursos de la aplicación.
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.sep);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            Image imagen = Image.getInstance(stream.toByteArray());
            imagen.setAbsolutePosition(100,530);
            //imagen.setAlignment(Element.ALIGN_LEFT);
            document.add(imagen);

            Log.e("EZH","generarPlaneacion 6");
            // Agregamos la leyenda Tecnologico Nacional de Mexico
            Font font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GRAY);
            Paragraph tnm = new Paragraph("TECNOLÓGICO NACIONAL DE MÉXICO",font);
            tnm.setAlignment(Element.ALIGN_RIGHT);
            document.add(tnm);
            Log.e("EZH","generarPlaneacion 7");
            // Agregamos leyenda Instituto Tecnológico de Chilpancingo
            Paragraph itch = new Paragraph("Instituto Tecnológico de Chilpancingo\n\n",font);
            itch.setAlignment(Element.ALIGN_RIGHT);
            document.add(itch);
            Log.e("EZH","generarPlaneacion 8");
            // Agregamos la leyenda ANEXO I
            Font font1 = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,BaseColor.BLACK);
            Paragraph anexo1 = new Paragraph("ANEXO I. INSTRUMENTACIÓN DIDÁCTICA PARA LA FORMACIÓN Y\n DESARROLLO DE COMPETENCIAS\nPROFESIONALES.\nTECNOLÓGICO NACIONAL DE MÉXICO.\nSUBDIRECCIÓN ACADÉMICA.\n\n",font1);
            anexo1.setAlignment(Element.ALIGN_CENTER);
            document.add(anexo1);
            // Agregamos datos de la planeacion como: periodo, Nombre de la asignatura, Plan de estudios, Clave de asignatura, creditos
            // Obtenemos informacion del metodo getDatosPlaneacion
            String respuesta = getDatosPlaneacion(idPlaneacion);
            Log.e("EZH","generarPlaneacion 9");
            if(!respuesta.equals("NO")) {
                String respuesta_array[] = respuesta.split("_");
                String speriodo = respuesta_array[0];
                String sNombreMateria = respuesta_array[1];
                String claveMateria = respuesta_array[2];
                String creditos = respuesta_array[3];
                Log.e("EZH", "generarPlaneacion 10");
                String planEstudio = getPlanesEstudios(claveMateria);
                // Agregamos el periodo
                Font fperiodo = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                Paragraph pperiodo = new Paragraph("Periodo: " + speriodo + "\n\n", fperiodo);
                pperiodo.setAlignment(Element.ALIGN_CENTER);
                document.add(pperiodo);

                Log.e("EZH", "generarPlaneacion 11");

                // Agregamos el nombre de la asignatura
                Paragraph pmateria = new Paragraph("Nombre de la asignatura: " + sNombreMateria + "\n\n", fperiodo);
                pperiodo.setAlignment(Element.ALIGN_LEFT);
                document.add(pmateria);

                // Agregamos el plan de estudio
                Paragraph pplan = new Paragraph("Plan de estudios: " + planEstudio + "\n\n", fperiodo);
                pperiodo.setAlignment(Element.ALIGN_LEFT);
                document.add(pplan);
                //Agregamos la clave de la materia
                Paragraph pclavem = new Paragraph("Clave de asignatura: " + claveMateria + "\n\n", fperiodo);
                pperiodo.setAlignment(Element.ALIGN_LEFT);
                document.add(pclavem);

                // Agregamos los creditos
                Paragraph pcreditos = new Paragraph("Horas teoría - horas prácticas - créditos: " + creditos + "\n\n\n");
                pcreditos.setAlignment(Element.ALIGN_LEFT);
                document.add(pcreditos);
                Log.e("EZH", "generarPlaneacion 12");


                //Pie de pagina
                // Imagen del itch
                Bitmap bitch = BitmapFactory.decodeResource(this.getResources(), R.drawable.logoitchaux);
                ByteArrayOutputStream sitch = new ByteArrayOutputStream();
                bitch.compress(Bitmap.CompressFormat.JPEG, 100, sitch);
                Image iitch = Image.getInstance(sitch.toByteArray());
                //iitch.setAlignment(Element.ALIGN_LEFT);
                iitch.setAbsolutePosition(100, 50);
                iitch.scaleAbsolute(48, 48);
                document.add(iitch);
                Log.e("EZH", "generarPlaneacion 13");
                // Agregamos la direccion del itch
                Font direccionItch = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL, BaseColor.GRAY);
                Paragraph direccion = new Paragraph("\n\n\nAv. Jóse Francisco Ruíz Massieu No. 5, Colonia Villa Moderna, C.P. 39090 Chilpancingo, Guerrero.\nTeléfono: (747) 48 01022, Tel/Fax: 47 2 10 14 www.itchilpancingo.edu.mx, email: itchilpo@hotmail.com\n", direccionItch);
                direccion.setAlignment(Element.ALIGN_CENTER);
                direccion.setSpacingBefore(60);
                document.add(direccion);
                //Agregamos la direccion de la pagina de Facebook
                Chunk c = new Chunk("Facebook: ", direccionItch);
                Font facebook = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL | Font.UNDERLINE, BaseColor.BLUE);
                Chunk f = new Chunk("Tecnológico de Chilpancingo Comunicación", facebook);
                Paragraph face = new Paragraph();
                face.add(c);
                face.add(f);
                face.setAlignment(Element.ALIGN_CENTER);
                document.add(face);
                Log.e("EZH", "generarPlaneacion 14");
                // Agregamos la imagenes sga, equidad, iso
                Bitmap bsga = BitmapFactory.decodeResource(this.getResources(), R.drawable.sga);
                ByteArrayOutputStream ssga = new ByteArrayOutputStream();
                bsga.compress(Bitmap.CompressFormat.PNG, 100, ssga);
                Image isga = Image.getInstance(ssga.toByteArray());
                //isga.setAlignment(Element.ALIGN_RIGHT);
                isga.scaleAbsolute(48, 48);
                isga.setAbsolutePosition(600, 50);
                document.add(isga);
                Log.e("EZH", "generarPlaneacion 15");

                Bitmap bequidad = BitmapFactory.decodeResource(this.getResources(), R.drawable.equidad);
                ByteArrayOutputStream sequidad = new ByteArrayOutputStream();
                bequidad.compress(Bitmap.CompressFormat.JPEG, 100, sequidad);
                Image iequidad = Image.getInstance(sequidad.toByteArray());
                //iequidad.setAlignment(Element.ALIGN_RIGHT);
                iequidad.setAbsolutePosition(658, 50);
                iequidad.scaleAbsolute(48, 48);
                document.add(iequidad);

                Bitmap biso = BitmapFactory.decodeResource(this.getResources(), R.drawable.iso);
                ByteArrayOutputStream siso = new ByteArrayOutputStream();
                biso.compress(Bitmap.CompressFormat.PNG, 100, siso);
                Image iiso = Image.getInstance(siso.toByteArray());
                //iiso.setAlignment(Element.ALIGN_RIGHT);
                iiso.scaleAbsolute(48, 48);
                iiso.setAbsolutePosition(720, 50);
                document.add(iiso);

                // Nueva pagina
                document.newPage();
                // Configuramos el encabezado

                document.add(imagen);

                document.add(tnm);

                document.add(itch);
                // Aregamos la informacion de la matea la caracterizacion
                Font f1 = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
                Font texto = new Font(Font.FontFamily.HELVETICA,12,Font.NORMAL,BaseColor.BLACK);
                Paragraph uno = new Paragraph("\n1. Caracterización de la asignatura.\n", f1);
                uno.setAlignment(Element.ALIGN_LEFT);
                document.add(uno);
                // Agregamos una tabla de una fila
                PdfPTable tCaracterizacion = new PdfPTable(1);
                // Llamamos al metodo getCaracterizacion
                String caracterizacion = getCaracterizacion(claveMateria);
                Paragraph pcaracterizacion = new Paragraph(caracterizacion,texto);
                pcaracterizacion.setIndentationLeft(20);
                PdfPCell celda = new PdfPCell(pcaracterizacion);
                celda.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                tCaracterizacion.addCell(celda);
                // agregamos la tabla
                document.add(tCaracterizacion);

                // Agregamos la intención didactica
                Paragraph dos = new Paragraph("\n2. Intención didáctica.\n",f1);
                dos.setAlignment(Element.ALIGN_LEFT);
                document.add(dos);
                // Agregamos la tabla
                PdfPTable tIntencion = new PdfPTable(1);
                String intencion = getIntención(claveMateria);
                Paragraph pIntencion = new Paragraph(intencion,texto);
                pIntencion.setIndentationLeft(20);
                PdfPCell celdaIntencion = new PdfPCell(pIntencion);
                celdaIntencion.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                tIntencion.addCell(celdaIntencion);
                document.add(tIntencion);

                // Agregamos la competencia de la asignatura
                Paragraph tres = new Paragraph("\n3. Competencia de la asignatura.\n",f1);
                tres.setAlignment(Element.ALIGN_LEFT);
                document.add(tres);
                // Agregamos la tabla
                PdfPTable tcompetencia = new PdfPTable(1);
                String competencia = getCompetencia(claveMateria);
                Paragraph pCompetencia = new Paragraph(competencia,texto);
                pCompetencia.setIndentationLeft(20);
                PdfPCell celdaCompetencia = new PdfPCell(pCompetencia);
                celdaCompetencia.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
                tcompetencia.addCell(celdaCompetencia);
                document.add(tcompetencia);
                // Agregamos la direccion del tec
                document.add(iitch);
                document.add(direccion);
                document.add(face);
                direccion.add(isga);
                direccion.add(iequidad);
                direccion.add(iiso);

                // Agregamos la nueva pagina
                document.newPage();
                // Agregamos el encabezado

                document.add(imagen);

                document.add(tnm);

                document.add(itch);

                // Agregamos el punto 4
                Paragraph cuatro = new Paragraph("4. Análisis por competencias específicas.\n",f1);
                cuatro.setAlignment(Element.ALIGN_LEFT);
                document.add(cuatro);
                // Obtenemos los temas


                // Cerramos documento
                document.close();
                // Colocamos
            }
            Log.e("EZH","generarPlaneacion 16");

        }
        catch (Exception e)
        {
            Log.e("EZH","Error generarPlaneacion: "+e.getMessage());
        }


    }

    public String getCompetencia(String claveMateria)
    {
        Log.e("EZH","getCompetencia 1");
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        String respuesta = "";
        Log.e("EZH","getCompetencias 2");

        // Abrimos try - catch para el manejo de excepciones
        try
        {
            // Preparamos consulta
            String consulta = "select materiaCompetencia from materia where materiaClave = '"+claveMateria+"'";
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","getCompetencias 3");
            if(c.moveToFirst())
            {
                respuesta = c.getString(0);
                Log.e("EZH","getCompetencias 4");
            }
            else
            {
                respuesta = "NO";
                Log.e("EZH","getCompetencias 5");
            }
            // Cerramos base de datos
            db.close();
            Log.e("EZH","getCompetencias 6");
        }
        catch(Exception e)
        {
            Log.e("EZH","getCompetencias Error: "+e.getMessage());
            respuesta = "NO";
        }
        Log.e("EZH","getCompetencias respuesta: "+respuesta);
        return respuesta;

    }

    public String getIntención(String claveMateria)
    {
        Log.e("EZH","getIntención 1");
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        String respuesta = "";
        Log.e("EZH","getIntención 2");
        // Abrimos try - catch para el manejo de excepciones
        try
        {
            // Preparamos consulta
            String consulta = "select materiaIntencion from materia where materiaClave = '"+claveMateria+"'";
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","getIntención 3");
            if(c.moveToFirst())
            {
                respuesta = c.getString(0);
                Log.e("EZH","getIntención 4");
            }
            else
            {
                respuesta = "NO";
                Log.e("EZH","getIntención 5");
            }
            // Cerramos base de datos
            db.close();
            Log.e("EZH","getIntención 6");
        }
        catch(Exception e)
        {
            Log.e("EZH","getIntención Error: "+e.getMessage());
            respuesta = "NO";
        }
        Log.e("EZH","getIntención respuesta: "+respuesta);
        return respuesta;
    }
    public String getCaracterizacion(String claveMateria)
    {
        Log.e("EZH","getCaracterizacion 1");
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        String respuesta = "";
        Log.e("EZH","getCaracterizacion 2");
        // Abrimos try - catch para el manejo de excepciones
        try
        {
            // Preparamos consulta
            String consulta = "select materiaCaracterizacion from materia where materiaClave = '"+claveMateria+"'";
            Cursor c = db.rawQuery(consulta,null);
            Log.e("EZH","getCaracterizacion 3");
            if(c.moveToFirst())
            {
                respuesta = c.getString(0);
                Log.e("EZH","getCaracterizacion 4");
            }
            else
            {
                respuesta = "NO";
                Log.e("EZH","getCaracterizacion 5");
            }
            // Cerramos base de datos
            db.close();
            Log.e("EZH","getCaracterizacion 6");
        }
        catch(Exception e)
        {
            Log.e("EZH","getCaracterizacion Error: "+e.getMessage());
            respuesta = "NO";
        }
        Log.e("EZH","getCaracterizacion respuesta: "+respuesta);
        return respuesta;
    }
    public String getPlanesEstudios(String clave)
    {
        Log.e("EZH","getPlanesEstudios 1");
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        String respuesta = "";
        Log.e("EZH","getPlanesEstudios 2");
        try
        {
            // Consultamos los planes de estudios en los cuales esta asignado la materia
            String consulta = "select * from materiaplan where materiaClave = '"+clave+"'";
            Log.e("EZH","consulta: "+consulta);
            Cursor cursor = db.rawQuery(consulta,null);
            Log.e("EZH","getPlanesEstudios 3");
            // Validamos que la consulta nos arrojo datos
            if (cursor.moveToFirst())
            {
                Log.e("EZH","getPlanesEstudios 4");
                // Recorrer resultados
                do
                {
                    String plan = cursor.getString(0);
                    respuesta += plan + " ";
                }
                while (cursor.moveToNext());
                Log.e("EZH","getPlanesEstudios 5");
            }
            else
            {
                Log.e("EZH","getPlanesEstudios 6");
                respuesta = "NO";
            }
        }
        catch (Exception e)
        {
            respuesta = "NO";
            Log.e("EZH","getPlanesEstudios Error: "+e.getMessage());
        }
        Log.e("EZH","getPlanesEstudios respuesta: "+respuesta);
        return respuesta;
    }

    public String getDatosPlaneacion(String planeacionId)
    {
        Log.e("EZH","getDatosPlaneacion 1");
        String respuesta = "";
        // Obtenemos la lectura y escritura de la base de datos
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();
        Log.e("EZH","getDatosPlaneacion 2");
        //Abrimos try-catch para el control de excepciones
        try
        {
            // Preparamos consulta
            String consulta = "SELECT docenteplaneacion.docenteplaneacionPeriodo, materia.materiaNombre, " +
                    "materia.materiaClave, materia.materiaCreditos FROM docenteplaneacion, materia " +
                    "WHERE docenteplaneacion.planeacionId = "+planeacionId+" AND docenteplaneacion.materiaClave = materia.materiaClave";

            Log.e("EZH","Consulta: "+consulta);
            // Creamos cursor
            Cursor cursor = db.rawQuery(consulta,null);
            Log.e("EZH","getDatosPlaneacion 3");
            // Validamos que la consulta nos arrojo resultados
            if (cursor.moveToFirst())
            {
                // Obtenemos los datos
                String periodo = cursor.getString(0);
                String nombreMateria = cursor.getString(1);
                String materiaClave = cursor.getString(2);
                String creditos = cursor.getString(3);
                Log.e("EZH","getDatosPlaneacion 4");
                respuesta = periodo +"_"+nombreMateria+"_"+materiaClave+"_"+creditos;
                Log.e("EZH","getDatosPlaneacion respuesta: "+respuesta);
            }
            else
            {
                Log.e("EZH","getDatosPlaneacion 5");
                respuesta = "NO";
            }
        }
        catch(Exception e)
        {
            Log.e("EZH","getDatosPlaneacion 6");
            Log.e("EZH","getDatosPlaneacion Error: "+e.getMessage());
            respuesta = "NO";
        }
        return respuesta;
    }



    public void cargarMaterias(String ma)
    {
        Log.e("EZH","cargarMaterias 1");
        if (!ma.equals("NO"))
        {
            // Tratamos información - Seperamos por ,
            String array_materias[] = ma.split(",");

            //Creamos ArrayList
            cm = new ArrayList<String>(array_materias.length);
            m = new ArrayList<>(array_materias.length);
            Log.e("EZH","cargarMaterias 2");
            // Recorremos array_materias
            for(String items:array_materias)
            {
                // Separamos por guion bajo
                String datos[] = items.split("_");
                // Agregamos el nombre la materia al ArrayList m
                m.add(datos[1]);
                // Agregamos la clave de la materia al ArrayList cm
                cm.add(datos[0]);
            }
            Log.e("EZH","cargarMaterias 3");

            // Agregamos el Adaptador al Spinner
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            m);
            materias.setAdapter(adapter);
            Log.e("EZH","cargarMaterias 4");
        }
        else
        {
            Log.e("EZH","cargarMaterias 5");
            ArrayList<String> arreglo = new ArrayList<String>(1);
            arreglo.add("SIN MATERIAS ASIGNADAS");
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            arreglo
                    );
            materias.setAdapter(adapter);
            Log.e("EZH","cargarMaterias 6");
        }




    }

    public String getMateriasAsignadas(String id)
    {
        Log.e("EZH","getMateriasAsignadas 1");
        String cadena = "";
        sqlhelper = new SQLHelper(this);
        db = sqlhelper.getWritableDatabase();

        Log.e("EZH","getMateriasAsignadas 2");
        // Preparamos consulta
        try
        {
            String consulta = "SELECT materia.materiaClave,materia.materiaNombre FROM docentemateria,materia WHERE docentemateria.materiaClave = materia.materiaClave AND docentemateria.docenteId = "+id;
            Cursor c = db.rawQuery(consulta,null);

            Log.e("EZH","getMateriasAsignadas 3");
            if(c.moveToFirst())
            {

                Log.e("EZH","getMateriasAsignadas 4");
                do
                {
                    String mc = c.getString(0);
                    String mn = c.getString(1);

                    cadena += mc+"_"+mn+",";
                }
                while(c.moveToNext());

                Log.e("EZH","getMateriasAsignadas 5");
            }
            else
            {

                Log.e("EZH","getMateriasAsignadas 6");
                cadena = "NO";
            }
            db.close();

        }
        catch (Exception e)
        {
            Log.e("EZH","getMateriasAsignadas Error: "+e.getMessage());
        }

        return cadena;
    }

    public void irAplaneaciones(String id)
    {
        Intent intent = new Intent(getApplicationContext(),Planeaciones.class);
        intent.putExtra("idDocente", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }




}
