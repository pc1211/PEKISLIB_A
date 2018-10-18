package com.example.pgyl.pekislib_a;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.Locale;

// Activité de sélection de fichier ou de directory
// Paramètres d'appel (sous forme de intent.putExtra):
//      title                   Titre de la fenêtre
//      init_dir_name           Nom de directory de départ (Chemin complet sans "/" final)
//      init_file_name_simple   Nom du fichier de départ (sans chemin)
//      select_file             "true" pour sélectionner un fichier
//                              "false" pour sélectionner un directory
//      display_files           "true" pour afficher les fichiers en plus des directories
//                              "false" pour uniquement les directories
//      ext_list                "" pour ne rien filtrer dans les fichiers
//                              <extension> pour filtrer les fichiers avec l'extension spécifiée (p.ex. "doc") 
//                              <extension1>/<extension2>/...<extensionx> pour plusieurs extensions (p.ex. "doc/xls")
//      create_dir              "true" si permet de créer un nouveau directory
//                              "false" si interdit de créer un nouveau directory
//      edit_file_name_simple   "true" si permet d'éditer le nom de fichier (sans le créer)
//                              "false" si interdir de préciser un nouveau nom de fichier
//      name                    Nom associé à cete occurence de l'activité InputButtons (p.ex. le nom du contrôle concerné dans l'activité appelante)
//
// Valeurs retournées (sous forme de intent.getStringExtra):
// Si Select_file "true":
//      file_name           "" ou Nom du fichier sélectionné (avec chemin complet)
//      dir_name            Nom du directory contenant ("" ou le fichier sélectionné (Chemin complet sans "/" final))
//      file_name_simple    "" ou Nom du fichier sélectionné (sans chemin)
//      name                Idem que lors de l'appel
// Si Select_file "false":
//      file_name           ""
//      dir_name            Nom du directory sélectionné (Chemin complet sans "/" final)
//      file_name_simple    ""
//      name                Idem que lors de l'appel
//
// Recommandations:
//  Tous les paramètres d'appel doivent être spécifiés
//  select_file doit être true si allow_new_file_name est true
//  allow_new_file_name doit être false si select_file est false
//  diplay_files doit être true si select_file est true ou si allow_new_file_name est true
//
//      Pour "Open file":       select_file             "true"
//                              display_files           "true"
//                              ext_list                liste des extensions pour filtrer les fichiers existants
//                              create_dir              "false"
//                              edit_file_name_simple   "false"
//      Pour "Save file as":    select_file             "true"
//                              display_files           "true"
//                              ext_list                liste des extensions possibles pour le fichier à sauvegarder
//                              create_dir              "true" ou "false"
//                              edit_file_name_simple   "true"
//      Pour "Select folder":   select_file             "false"
//                              display_files           "true" or "false"
//                              ext_list                ""
//                              create_dir              "true" ou "false"
//                              edit_file_name_simple   "false"

public class SelectDirFileActivity extends Activity {

    private ListView lvbrowser;     // File Browser avec les noms des directory (sous forme "nom/") et des fichiers
    private Button btnfile;         // Contrôle pour entrer le nom de fichier sélectionné
    private Button btnnewdir;       // Bouton pour créer un nouveau directory
    private Button btnok;           // Bouton pour quitter l'activité avec le directory ou fichier sélectionné
    private Button btnupdir;        // Bouton pour afficher le nom du directory courant et aller à son parent

    final String C_FILE_EXT_SEP = "/";     //  Séparateur d'extensions si plusieurs extensions
    final int C_DIR_COL = Color.WHITE;     // Couleur des directories dans le File Browser
    final int C_FILE_MATCH_COL = Color.GREEN;  // Couleur des fichiers (éligibles) dans le File Browser
    final int C_FILE_NO_MATCH_COL = Color.RED;  // Couleur de l'item supplémentaire du File Browser (si pas de fichier ou pas de fichier éligible)
    final String C_FILE_NO_MATCH_TEXT = "No matching files";  //  Texte de l'item supplémentaire du File Browser (si pas de fichier ou pas de fichier éligible)
    final int C_CODE_ACTIVITY = 1;    // Code (arbitraire) de l'activité InputButtons
    final String C_ALPHANUM_BUTTONS_P = "a;b;c;BACK;d;e;f;CLEAR;g;h;i;CASE;j;k;l;NEXTP;m;n;o;p;q;r;s;t;u;v;w;x;y;z;.; ;1;2;3;BACK;4;5;6;CLEAR;7;8;9;CASE;#;0;.;NEXTP;+;-;*;/;(;);[;];{;};<;>;=;$;£;@;&;§;~;BACK;?;!;|;CLEAR;\\;_;^;CASE;\";';.;NEXTP;,;SEP;:;NA;NA;NA;NA;NA;NA;NA;NA;NA;NA;NA;NA;NA;à;â;ä;BACK;é;è;ê;CLEAR;ë;î;ï;CASE;ô;ö;ù;NEXTP;û;ü;ç;NA";
    final String C_ALPHANUM_BUTTONS_L = "a;b;c;d;e;f;g;BACK;h;i;j;k;l;m;n;CLEAR;o;p;q;r;s;t;u;CASE;v;w;x;y;z;.; ;NEXTP;1;2;3;+;-;*;/;BACK;4;5;6;(;);[;];CLEAR;7;8;9;{;};<;>;CASE;#;0;.;=;$;£;@;NEXTP;&;§;~;?;!;|;\\;BACK;_;^;\";';.;,;SEP;CLEAR;:;NA;NA;NA;NA;NA;NA;CASE;NA;NA;NA;NA;NA;NA;NA;NEXTP;à;â;ä;é;è;ê;ë;BACK;î;ï;ô;ö;ù;û;ü;CLEAR;ç;NA;NA;NA;NA;NA;NA;CASE;NA;NA;NA;NA;NA;NA;NA;NEXTP";
    String p_title;      // Variables contenant les paramètres d'appel de l'activité
    String p_name;
    String p_init_dir_name;
    String p_init_file_name_simple;
    boolean p_select_file;
    boolean p_display_files;
    String p_ext_list;
    boolean p_create_dir;
    boolean p_edit_file_name_simple;

    String[] ext_list;  // Liste des extensions des fichiers éligibles    
    String root_dir;   // Chemin du vrai root directory
    char sep_char;    // Séparateur des noms de directory
    boolean all_files;  // True si ext_list=""  (pas d'extension particulière choisie => Tous les fichiers)

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent callingIntent;
        String s;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectdirfile);

        btnfile = (Button) findViewById(R.id.BTN_FILE);
        btnnewdir = (Button) findViewById(R.id.BTN_NEWDIR);
        btnupdir = (Button) findViewById(R.id.BTN_UPDIR);
        btnok = (Button) findViewById(R.id.BTN_OK);
        lvbrowser = (ListView) findViewById(R.id.LV_BROWSER);

        sep_char = File.separatorChar;        // Séparateur de noms de directory
        //root_dir = MiscUtils.getRootDir();    // Identifier le vrai root directory (normalement "/")

        callingIntent = getIntent();
        p_title = callingIntent.getStringExtra("title");   //  Récupération des paramètres
        p_init_dir_name = callingIntent.getStringExtra("init_dir_name");
        p_init_file_name_simple = callingIntent.getStringExtra("init_file_name_simple");
        p_select_file = callingIntent.getStringExtra("select_file").toUpperCase(Locale.ENGLISH).equals("TRUE");
        p_display_files = callingIntent.getStringExtra("display_files").toUpperCase(Locale.ENGLISH).equals("TRUE");
        p_ext_list = callingIntent.getStringExtra("ext_list");
        p_create_dir = callingIntent.getStringExtra("create_dir").toUpperCase(Locale.ENGLISH).equals("TRUE");
        p_edit_file_name_simple = callingIntent.getStringExtra("edit_file_name_simple").toUpperCase(Locale.ENGLISH).equals("TRUE");
        p_name = callingIntent.getStringExtra("name");

        setTitle(p_title);                          //  Initialisations
        if (!p_create_dir) {
            btnnewdir.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }   // "Désactivé"
        if (!p_edit_file_name_simple) {
            btnfile.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
        btnfile.setText(p_init_file_name_simple);
        if (p_display_files) {
            all_files = false;
            if (!(p_ext_list.equals("")))  //  Au moins une extension
            {
                ext_list = p_ext_list.split(C_FILE_EXT_SEP);
            }     // Obtenir un tableau des extensions des fichiers éligibles
            else   //  Tous les fichiers
            {
                all_files = true;
            }
        }  // Liste des extensions des fichiers éligibles, p.ex.: "DOC/XLS;...."}

        browse_dir(p_init_dir_name);   // Afficher dans le Browser le contenu du répertoire de départ

        btnfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p_edit_file_name_simple) {
                    get("FILE");
                }
            }
        });
        btnnewdir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p_create_dir) {
                    get("DIR");
                }
            }
        });
        btnupdir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnupdir_click();
            }
        });
        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnok_click();
            }
        });
        lvbrowser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lvbrowser_click(position);
            }
        });
    }

    private void get(String arg_name) {
        /*if (arg_name.equals("DIR")) {
            inputbuttons_activity(arg_name, "Enter dirname", "", "ALPHANUM", "NULL", C_CODE_ACTIVITY, getApplicationContext(), this);
        }
        if (arg_name.equals("FILE")) {
            inputbuttons_activity(arg_name, "Enter filename.ext", btnfile.getText().toString(), "ALPHANUM", "NULL", C_CODE_ACTIVITY, getApplicationContext(), this);
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        if (requestCode == C_CODE_ACTIVITY)  // Résultat de l'activité InputButtons
        {
            if (resultCode == RESULT_OK)  // Sélection d'une chaîne
            {
                set(returnIntent.getStringExtra("name"), returnIntent.getStringExtra("final_display"));
            }
        }
    }

    private void set(String arg_name, String arg_value)  //  Un nouveau nom de directory ou de fichier a été entré par l'activité InputButtond
    {
        String s;
        String t;
        String u;
        File f;

        if (arg_name.equals("DIR")) {
            if (!(arg_value.equals(""))) {
                s = btnupdir.getText().toString();
                if (!(s.equals(root_dir))) {
                    s = s + sep_char + arg_value;
                } else {
                    s = s + arg_value;
                }
                f = new File(s);
                {
                    if (f.mkdirs())   //  Créer le directory (OK car p_create_dir conditionnait le click sur btnnewdir)
                    {
                        MiscUtils.msgBox("OK: Created directory \"" + s + "\"",this);
                        browse_dir(s);
                    }  // et y aller
                    else  // Problème de création ou existe déjà
                    {
                        MiscUtils.msgBox("ERROR: Unable to create directory \"" + s + "\"", this);
                    }
                }
            }
        }
        if (arg_name.equals("FILE")) {
            btnfile.setText(arg_value);
        }
    }

    private void btnupdir_click()  //  Aller au directory parent de celui affiché dans le Browser
    {
        File f;
        String s;

        s = btnupdir.getText().toString();
        if (!(s.equals(root_dir))) {
            f = new File(s);
            s = f.getParent();
        }
        browse_dir(s);
    }   // Afficher le contenu de ce directory dans le Browser

    private void btnok_click()  //  Retour à l'activité appelante
    {
        String s;
        String t;
        String u;
        Intent returnIntent;

        u = "";
        s = "";
        t = btnupdir.getText().toString();
        if (p_select_file) {
            s = btnfile.getText().toString();
            if (!(s.equals(""))) {
                if (!(t.equals(root_dir))) {
                    u = t + sep_char + s;
                } else {
                    u = t + s;
                }
            }
        }    // Nom de fichier complet
        returnIntent = new Intent();
        returnIntent.putExtra("file_name", u);
        returnIntent.putExtra("dir_name", t);
        returnIntent.putExtra("file_name_simple", s);
        returnIntent.putExtra("name", p_name);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void lvbrowser_click(int arg_pos)   // Valeur en position arg_pos dans le Browser a été cliquée
    {
        File f;
        String s;
        String t;
        Intent returnIntent;

        s = btnupdir.getText().toString();
        t = lvbrowser.getItemAtPosition(arg_pos).toString();
        if (t.charAt(t.length() - 1) == sep_char) {
            t = t.substring(0, t.length() - 1);
        }
        if (!(s.equals(root_dir))) {
            s = s + sep_char + t;
        } else {
            s = s + t;
        }
        f = new File(s);
        if (f.exists())  // Pour exclure ligne éventuelle "No matching files" ...
        {
            if (f.isDirectory()) {
                browse_dir(s);
            } else {
                if (p_select_file) {
                    btnfile.setText(t);
                }
            }
        }
    }    // Afficher le nom (simple) de fichier

    private void browse_dir(String arg_dir)    // Afficher le contenu d'un directory dans le Browser
    {
        SelectDirFileAdapter lv_adapter;   // Voir ma classe SelectFileAdapter.java
        File[] files;
        String[] item_names;
        int[] item_colors;
        String[] sat;
        int[] iat;
        String s;
        File f;
        int i;
        int j;
        int n;
        int fi;
        int dc;
        int fc;
        int ftot;

        btnupdir.setText(arg_dir);

        f = new File(arg_dir);
        files = f.listFiles();
        if (files != null) {
            n = files.length;
        } else {
            n = 0;
        }
        sat = new String[n + 1];  // Pour les noms de directory ou fichiers convertibles, inclus un éventuel item C_NO_FILES
        iat = new int[sat.length];  // Pour leurs couleurs dans le Browser
        if (n > 0)  // Il y a des directories ou des fichiers
        {
            fi = 0;
            for (i = 1; i <= n; i = i + 1) {
                if (files[i - 1].isDirectory())  // Directory
                {
                    sat[fi] = files[i - 1].getName() + sep_char;  // Nom du directory + "/"
                    iat[fi] = C_DIR_COL;  // Couleur normale par défaut
                    fi = fi + 1;
                }
            }
            dc = fi;  // Nombre de directories
            if (dc > 0) {
                java.util.Arrays.sort(sat, 0, dc, String.CASE_INSENSITIVE_ORDER);
            }  // Trier les directories
            if (p_display_files)  // OK pour afficher les fichiers
            {
                fi = 0;
                for (i = 1; i <= n; i = i + 1) {
                    if (!(files[i - 1].isDirectory()))   // => Fichier
                    {
                        s = files[i - 1].getName();
                        if (matchingFile(s))  // Fichier éligible
                        {
                            sat[dc + fi] = s;  // Nom du fichier
                            iat[dc + fi] = C_FILE_MATCH_COL;   // Couleur "OK éligible"
                            fi = fi + 1;
                        }
                    }
                }
                fc = fi;   // Nombre de fichiers éligibles
                if (fc > 0) {
                    java.util.Arrays.sort(sat, dc, dc + fc, String.CASE_INSENSITIVE_ORDER);
                }
            }   // Trier les fichiers
            else   // Ne pas afficher les éventuels fichiers
            {
                fc = 0;
            }
        }   // Pas de fichiers à afficher
        else  //  Aucun directory et aucun fichier
        {
            dc = 0;
            fc = 0;
        }
        ftot = dc + fc;  // Nombre total d'items dans le Browser (directories+fichiers)
        if ((p_display_files) && (fc == 0)) {
            sat[ftot] = C_FILE_NO_MATCH_TEXT;     // Ajouter un item avec C_NO_MATCH (No matching files)
            iat[ftot] = C_FILE_NO_MATCH_COL;       // Couleur "KO"
            ftot = ftot + 1;
        }   // Ajuster le nombre total d'items dans le Browser
        item_names = new String[ftot];
        item_colors = new int[item_names.length];
        for (i = 1; i <= ftot; i = i + 1) {
            item_names[i - 1] = sat[i - 1];   // Recopier les valeurs
            item_colors[i - 1] = iat[i - 1];
        }
        sat = null;
        iat = null;
        lv_adapter = new SelectDirFileAdapter(this, item_names, item_colors);
        lvbrowser.setAdapter(lv_adapter);
    }

    private boolean matchingFile(String arg_f)   // Vérifier si un nom de fichier (existant ou non) est éligible (selon son extension)
    {
        boolean ef;
        int i;
        String s;

        ef = false;   // Si true: Fichier éligible
        if (!(arg_f.equals(""))) {
            s = arg_f.toUpperCase(Locale.ENGLISH);
            if (!all_files)  // Au moins une extension à vérifier
            {
                for (i = 1; i <= ext_list.length; i = i + 1)  // Parcourir la liste des extensions éligibles
                {
                    if (s.lastIndexOf("." + ext_list[i - 1]) != -1) {
                        ef = true;
                    }
                }
            }  // Fichier éligible
            else  // Aucune extension particulière choisie => Tous les fichiers sont bons
            {
                ef = true;
            }
        }
        return ef;
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        throw new UnsupportedOperationException("Not supported yet.");
    } //To change body of generated methods, choose Tools | Templates.
}
