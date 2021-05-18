package pollub.ism.lab08;


import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ZmianaPozycjiMagazynowej {
    @Embedded
    public PozycjaMagazynowa pozycja;
    @Relation(
            parentColumn = "_id",
            entityColumn = "_idPozycji"
    )
    public List<StanPozycjiMagazynowej> listaStanow;
}
