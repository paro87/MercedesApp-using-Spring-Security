package tm.paro.mercedesapp.Model;

import lombok.Data;

@Data
public class Tires {

    TirePressureStatus tirepressurefrontleft;
    TirePressureStatus tirepressurefrontright;
    TirePressureStatus tirepressurerearleft;
    TirePressureStatus tirepressurerearright;

    public Tires(){
        tirepressurefrontleft=new TirePressureStatus();
        tirepressurefrontright=new TirePressureStatus();
        tirepressurerearleft=new TirePressureStatus();
        tirepressurerearright=new TirePressureStatus();
    }
}
