package pollub.ism.lab08;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "StanWarzywniaka")
public class StanPozycjiMagazynowej {
    @PrimaryKey(autoGenerate = true)
    public long _idStanu;
    public String CHANGEDATE;
    public int _idPozycji;      //zmienna do wskazywania na ID parenta
    public int OLDVALUE;
    public int NEWVALUE;
}
