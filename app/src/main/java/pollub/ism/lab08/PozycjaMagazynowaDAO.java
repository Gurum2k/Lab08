package pollub.ism.lab08;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public interface PozycjaMagazynowaDAO {

    @Insert  //Automatyczna kwerenda wystarczy
    public void insert(PozycjaMagazynowa pozycja);

    @Update
        //Automatyczna kwerenda wystarczy
    void update(PozycjaMagazynowa pozycja);

    @Query("SELECT QUANTITY FROM Warzywniak WHERE NAME= :wybraneWarzywoNazwa")
        //Nasza kwerenda
    int findQuantityByName(String wybraneWarzywoNazwa);

    @Query("UPDATE Warzywniak SET QUANTITY = :wybraneWarzywoNowaIlosc WHERE NAME= :wybraneWarzywoNazwa")
    void updateQuantityByName(String wybraneWarzywoNazwa, int wybraneWarzywoNowaIlosc);

    @Query("SELECT COUNT(*) FROM Warzywniak")
        //Ile jest rekordów w tabeli
    int size();

    @Query("SELECT _id FROM Warzywniak WHERE NAME = :wybraneWarzywoNazwa")
    public int getIDOfProduct(String wybraneWarzywoNazwa);


    //insert do tabeli z historią zmian
    @Insert(entity = StanPozycjiMagazynowej.class)
    public void insert(StanPozycjiMagazynowej stan);

    //pobiera wszystko ze złączonych tabel warzywniak i stanWarzywniaka i zwraca to w obiekcie  złączenia tabel
    @Transaction
    @Query("SELECT * FROM Warzywniak WHERE NAME= :wybraneWarzywoNazwa")
    public ZmianaPozycjiMagazynowej getZmianyPozycjiMagazynowej(String wybraneWarzywoNazwa);
}
