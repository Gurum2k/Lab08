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
    private static String HistoryDump;
    private static String DataDump;
    Date currentTime;
    DateFormat dateFull = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    String currentTimeFormated;
    private ActivityMainBinding binding;
    private ArrayAdapter<CharSequence> adapter;

    ;
    private Integer wybraneWarzywoIlosc = null;
    private Boolean spinnerInitialSetup = true;
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

                //czysci pola tekstowe tylko w przypadku gdy uzytkownik zmienia stan spinnera
                //(obejscie problemu gdzie spinner wywoluje ta metode w metodzie onCreate co czyscilo pola przy obracaniu ekranu)
                //z jakiegoś powodu przy zmianie warzywa na inne niż domyślne OnItemSelected wywołuje się dwa razy i nie umiem zrobić żeby nie czyściło pól wtedy
                if (!spinnerInitialSetup) {
                    binding.tekstHistoria.setText("");
                    binding.tekstData.setText("");
                }
                spinnerInitialSetup = false;
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
                    //wyswietlenie komunikatu o bledzie na ekranie urzadzenia
                    Toast.makeText(this, R.string.bladZaMaloDoWydaniaProduktu, Toast.LENGTH_LONG).show();
                    return;
                }
                nowaIlosc = wybraneWarzywoIlosc - zmianaIlosci;
                break;
        }

        //aktualizacja danych o ilości produktu w bazie
        bazaDanych.pozycjaMagazynowaDAO().updateQuantityByName(wybraneWarzywoNazwa, nowaIlosc);


        dodajRekordZmianyDoBazy(nowaIlosc);

        wyswietlHistorieiDate();

        //przepisanie pól tekstowych aby zachować je po obrocie ekranu
        HistoryDump = binding.tekstHistoria.getText().toString();
        DataDump = binding.tekstData.getText().toString();

        aktualizuj();
    }

    private void dodajRekordZmianyDoBazy(Integer _nowaIlosc) {
        //pobranie aktualnej daty wraz z godziną i zamiana z formatu z Date do String
        currentTime = Calendar.getInstance().getTime();
        currentTimeFormated = dateFull.format(currentTime);

        //pobranie informacji o zmianie i zapis do obiektu w celu wysłania ich do bazy
        StanPozycjiMagazynowej stanObiekt = new StanPozycjiMagazynowej();
        stanObiekt.OLDVALUE = wybraneWarzywoIlosc;
        stanObiekt.NEWVALUE = _nowaIlosc;
        stanObiekt.CHANGEDATE = currentTimeFormated;
        stanObiekt._idPozycji = bazaDanych.pozycjaMagazynowaDAO().getIDOfProduct(wybraneWarzywoNazwa);

        //wysłanie informacji do bazy danych do tabeli z historią
        bazaDanych.pozycjaMagazynowaDAO().insert(stanObiekt);
    }

    private void wyswietlHistorieiDate() {
        try {
            //pobranie obiektu z lista zmian i id danego produktu/warzywa
            ZmianaPozycjiMagazynowej listaPolaczen = bazaDanych.pozycjaMagazynowaDAO().getZmianyPozycjiMagazynowej(wybraneWarzywoNazwa);

            //wyluskanie listy zmian z obiektu
            List<StanPozycjiMagazynowej> stanyWarzywa = listaPolaczen.listaStanow;
            binding.tekstHistoria.setText("");

            //wyluskanie z listy poszczegolnych danych i wypisanie ich na polu multilinetext
            stanyWarzywa.forEach(var -> {
                String output = "";
                //rozbicie daty na dzień i na godzinę
                String[] _dateExplode = var.CHANGEDATE.split(" ");

                output += _dateExplode[0] + " , " + _dateExplode[1] + " , " + var.OLDVALUE + " -> " + var.NEWVALUE + "\n";
                binding.tekstHistoria.append(output);
            });

            //wyluskanie z ostatniego elementu listy daty ostatniej modyfikacji i wypisanie jej obok pola tekstowego do wprowadzania ilosci produktu
            binding.tekstData.setText(stanyWarzywa.get(stanyWarzywa.size() - 1).CHANGEDATE);
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
        binding.tekstHistoria.setText(HistoryDump);
        binding.tekstData.setText(DataDump);
    }

    public enum OperacjaMagazynowa {SKLADUJ, WYDAJ}
}