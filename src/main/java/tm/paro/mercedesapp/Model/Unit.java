package tm.paro.mercedesapp.Model;

import com.google.gson.annotations.SerializedName;

public enum Unit {
    @SerializedName("KILOMETER")
    KILOMETER,       // Odometer
    @SerializedName("PERCENTAGE")
    PERCENTAGE       // FuelLevel, StateOfCharge
}
