package pollub.ism.lab08;

import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pollub.ism.lab08.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static String wybraneWarzywoNazwa = null;
    private static String Historia;
    private static String HistoriaDanych;
    Date currTime;
    DateFormat date = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    String currTimeFormated;
    private ActivityMainBinding binding;
    private ArrayAdapter<CharSequence> adapter;

    ;
    private Integer wybraneWarzywoIlosc = null;
    private Boolean spinnInitSetup = true;
    private BazaMagazynowa bazaDanych;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        adapter = ArrayAdapter.createFromResource(this, R.array.Asortyment, android.R.layout.simple_dropdown_item_1line);
        binding.spinner.setAdapter(adapter);


        try {
            bazaDanych = Room.databaseBuilder(getApplicationContext(), BazaMagazynowa.class, BazaMagazynowa.NAZWA_BAZY).allowMainThreadQueries().build();
        } catch (SQLiteCantOpenDatabaseException e) {
            e.printStackTrace();
        }


        if (bazaDanych.pozycjaMagazynowaDAO().size() == 0) {
            String[] asortyment = getResources().getStringArray(R.array.Asortyment);
            for (String nazwa : asortyment) {
                PozycjaMagazynowa pozycjaMagazynowa = new PozycjaMagazynowa();
                pozycjaMagazynowa.NAME = nazwa;
                pozycjaMagazynowa.QUANTITY = 0;
                bazaDanych.pozycjaMagazynowaDAO().insert(pozycjaMagazynowa);
            }
        }

        binding.przyciskSkladuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zmienStan(OperacjaMagazynowa.SKLADUJ);
            }
        });

        binding.przyciskWydaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zmienStan(OperacjaMagazynowa.WYDAJ);
            }
        });

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                wybraneWarzywoNazwa = adapter.getItem(position).toString();
                aktualizuj();

                if (!spinnInitSetup) {
                    binding.tekstHistoria.setText("");
                    binding.tekstJednostka.setText("");
                }
                spinnInitSetup = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void aktualizuj() {
        wybraneWarzywoIlosc = bazaDanych.pozycjaMagazynowaDAO().findQuantityByName(wybraneWarzywoNazwa);
        binding.tekstStanMagazynu.setText("Stan magazynu dla " + wybraneWarzywoNazwa + " wynosi: " + wybraneWarzywoIlosc);
    }

    private void zmienStan(OperacjaMagazynowa operacja) {

        Integer zmianaIlosci = null, nowaIlosc = null;

        try {
            zmianaIlosci = Integer.parseInt(binding.edycjaIlosc.getText().toString());
        } catch (NumberFormatException ex) {
            return;
        } finally {
            binding.edycjaIlosc.setText("");
        }

        switch (operacja) {
            case SKLADUJ:
                nowaIlosc = wybraneWarzywoIlosc + zmianaIlosci;
                break;
            case WYDAJ:
                if (wybraneWarzywoIlosc - zmianaIlosci < 0) {

                    Toast.makeText(this, R.string.bladZaMaloDoWydaniaProduktu, Toast.LENGTH_LONG).show();
                    return;
                }
                nowaIlosc = wybraneWarzywoIlosc - zmianaIlosci;
                break;
        }


        bazaDanych.pozycjaMagazynowaDAO().updateQuantityByName(wybraneWarzywoNazwa, nowaIlosc);


        dodajRekordZmianyDoBazy(nowaIlosc);

        wyswietlHistorie();

        Historia = binding.tekstHistoria.getText().toString();
        HistoriaDanych = binding.tekstJednostka.getText().toString();

        aktualizuj();
    }

    private void dodajRekordZmianyDoBazy(Integer _nowaIlosc) {

        currTime = Calendar.getInstance().getTime();
        currTimeFormated = date.format(currTime);


        StanPozycjiMagazynowej stanObiekt = new StanPozycjiMagazynowej();
        stanObiekt.OLDVALUE = wybraneWarzywoIlosc;
        stanObiekt.NEWVALUE = _nowaIlosc;
        stanObiekt.CHANGEDATE = currTimeFormated;
        stanObiekt._idPozycji = bazaDanych.pozycjaMagazynowaDAO().getIDOfProduct(wybraneWarzywoNazwa);


        bazaDanych.pozycjaMagazynowaDAO().insert(stanObiekt);
    }

    private void wyswietlHistorie() {
        try {

            ZmianaPozycjiMagazynowej listaPolaczen = bazaDanych.pozycjaMagazynowaDAO().getZmianyPozycjiMagazynowej(wybraneWarzywoNazwa);


            List<StanPozycjiMagazynowej> stanyWarzywa = listaPolaczen.listaStanow;
            binding.tekstHistoria.setText("");


            stanyWarzywa.forEach(var -> {
                String output = "";

                String[] _dateExplode = var.CHANGEDATE.split(" ");

                output += _dateExplode[0] + " , " + _dateExplode[1] + " , " + var.OLDVALUE + " -> " + var.NEWVALUE + "\n";
                binding.tekstHistoria.append(output);
            });


            binding.tekstJednostka.setText(stanyWarzywa.get(stanyWarzywa.size() - 1).CHANGEDATE);
        } catch (SQLiteCantOpenDatabaseException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.bladOdczytuHistorii, Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.tekstHistoria.setText(Historia);
        binding.tekstJednostka.setText(HistoriaDanych);
    }

    public enum OperacjaMagazynowa {SKLADUJ, WYDAJ}
}