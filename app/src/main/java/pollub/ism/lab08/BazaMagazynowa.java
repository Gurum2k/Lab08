package pollub.ism.lab08;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PozycjaMagazynowa.class, StanPozycjiMagazynowej.class}, version = pollub.ism.lab08.BazaMagazynowa.WERSJA, exportSchema = false)
public abstract class BazaMagazynowa extends RoomDatabase {

    public static final String NAZWA_BAZY = "Stoisko z warzywami";
    public static final int WERSJA = 1;

    public abstract PozycjaMagazynowaDAO pozycjaMagazynowaDAO();
}
