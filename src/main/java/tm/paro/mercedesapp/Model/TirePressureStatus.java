package tm.paro.mercedesapp.Model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TirePressureStatus {
    private String unit;
    private double value;
    @SerializedName("retrievalstatus")
    private RetrievalStatus retrievalStatus;
    private long timestamp;
}
