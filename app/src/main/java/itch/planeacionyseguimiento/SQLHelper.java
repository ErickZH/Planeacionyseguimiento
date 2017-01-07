package itch.planeacionyseguimiento;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper
{
    //nombre de nuestra base de datos
    public static final String DATABASE_NAME ="planeacion.db";
    //constructor

    public SQLHelper(Context context)
    {
        super(context,DATABASE_NAME,null,1);

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Definicion de la tabla persona
        db.execSQL("CREATE TABLE IF NOT EXISTS persona (personaId TEXT, personaRfc TEXT, personaNombre TEXT, personaApellidoPaterno TEXT, personaApellidoMaterno TEXT)");

        //Definicion de la tabla docente
        db.execSQL("CREATE TABLE IF NOT EXISTS docente (personaId TEXT, docenteTelefono TEXT, docenteEmail TEXT, docenteUsuario TEXT, docentePassword TEXT)");

        // Definicion de la tabla docentemateria
        db.execSQL("CREATE TABLE IF NOT EXISTS docentemateria (docenteId TEXT, materiaClave TEXT)");

        // Definicion de la tabla docentePlaneacion
        db.execSQL("CREATE TABLE IF NOT EXISTS docenteplaneacion (planeacionId INTEGER PRIMARY KEY AUTOINCREMENT, docenteId TEXT, materiaClave TEXT, docenteplaneacionPeriodo TEXT)");

        //Definicion de la tabla indicador
        db.execSQL("CREATE TABLE IF NOT EXISTS indicador (indicadorId TEXT, indicadorTexto TEXT, indicadorValoracion TEXT)");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('1','a) Se adapta a situaciones y contextos complejos. Puede trabajar en equipo, reflejar sus conocimientos en la interpretación de la realidad. Inferir comportamientos o consecuencias de los fenómenos o problemas en estudio. Incluir más variables en dichos casos de estudio.','95-100')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('2','b) Hace aportaciones a las actividades académicas desarrolladas. Pregunta integrando conocimientos de otras asignaturas o de casos anteriores de la misma asignatura. Presenta otros puntos de vista que complementan al presentado en la clase. Presenta fuentes de información adicionales (Internet, documentales), usa más bibliografía, consulta fuentes en un segundo idioma, etc.','95-100')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('3','c) Propone y/o explica soluciones o procedimientos no vistos en clase (creatividad). Ante problemas o casos de estudio propone perspectivas diferentes, para abordarlos y sustentarlos correctamente. Aplica procedimientos aprendidos en otra asignatura o contexto para el problema que se está resolviendo','95-100')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('4','d) Introduce recursos y experiencias que promueven un pensamiento crítico; (por ejemplo el uso de las tecnologías de la información estableciendo previamente un criterio). Ante temas de una asignatura, introduce cuestionamientos de tipo ético, ecológico, histórico, político, económico, etc.; que deben tomarse en cuenta para comprender mejor, o a futuro dicho tema. Se apoya en foros, autores, bibliografía, documentales, etc. para sustentar su punto de vista.','95-100')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('5','e) Incorpora conocimientos y actividades interdisciplinarias en su aprendizaje. En el desarrollo de los temas de la asignatura, incorpora conocimientos y actividades desarrollados en otras asignaturas para lograr la competencia.','95-100')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('6','f) Realiza su trabajo de manera autónoma y autorregulada. Es capaz de organizar su tiempo y trabajar sin necesidad de una supervisión estrecha y/o coercitiva. Aprovecha la planeación de la asignatura presentada por el (la) profesor(a) (instrumentación didáctica) para presentar propuestas de mejora de la temática vista durante el curso. Realiza actividades de investigación para participar activamente durante el curso.','95-100')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('7','Cumple cuatro de los indicadores definidos en desempeño excelente.', '85-94')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('8','Cumple tres de los indicadores definidos en el desempeño excelente.', '75-84')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('9','Cumple dos de los indicadores definidos en el desempeño excelente.', '70-74')");

        db.execSQL("INSERT INTO indicador (indicadorId, indicadorTexto, indicadorValoracion) VALUES ('10','No se cumple con el 100% de evidencias conceptuales, procedimentales y actitudinales de los indicadores definidos en el desempeño excelente.', 'NA')");

        // Definicion de la tabla indicadores de alcance
        db.execSQL("CREATE TABLE IF NOT EXISTS indicadoresalcance (indicadoresId TEXT, indicadoresDesempeno TEXT, indicadorId TEXT)");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('1', 'Excelente', '1')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('2', 'Excelente', '2')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('3', 'Excelente', '3')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('4', 'Excelente', '4')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('5', 'Excelente', '5')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('6', 'Excelente', '6')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('7', 'Notable', '7')");

        db.execSQL("INSERT INTO indicadoresalcance (indicadoresId, indicadoresDesempeno, indicadorId) VALUES ('8', 'Bueno', '8')");

        // Definicion de la tabla materia
        db.execSQL("CREATE TABLE IF NOT EXISTS materia (materiaClave TEXT, materiaNombre TEXT, materiaCreditos TEXT, materiaIntencion TEXT, materiaCompetencia TEXT, materiaCaracterizacion TEXT, materiaFuentesInformacion TEXT, materiaApoyoDidactico TEXT)");

        // Definicion de la tabla materiaplan

        db.execSQL("CREATE TABLE IF NOT EXISTS materiaplan (planestudioClave TEXT, materiaClave TEXT)");

        // Definicion de la tabla materiatema
        db.execSQL("CREATE TABLE IF NOT EXISTS materiatema (materiaClave TEXT, temaId TEXT)");
        // Definicion de la tabla tema

        db.execSQL("CREATE TABLE IF NOT EXISTS tema (temaId TEXT, tema TEXT, temaActividadesAprendizaje TEXT, temaCompetenciaGenerica TEXT, temaSubtemas TEXT)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
