package tm.paro.mercedesapp.Model;

import lombok.Data;

@Data
public class Odometer {
    DistanceDriven odometer;
    DistanceDriven distancesincereset;
    DistanceDriven distancesincestart;

    public Odometer() {
        odometer=new DistanceDriven();
        distancesincereset=new DistanceDriven();
        distancesincestart=new DistanceDriven();
    }
}
