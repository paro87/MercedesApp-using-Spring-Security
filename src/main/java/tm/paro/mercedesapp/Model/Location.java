package tm.paro.mercedesapp.Model;

import lombok.Data;

@Data
public class Location {
    LocationCoordinate latitude;
    LocationCoordinate longitude;
    LocationCoordinate heading;

    public Location(){
        latitude=new LocationCoordinate();
        longitude=new LocationCoordinate();
        heading=new LocationCoordinate();
    }
}
