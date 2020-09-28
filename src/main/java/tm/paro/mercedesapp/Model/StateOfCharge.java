package tm.paro.mercedesapp.Model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class StateOfCharge {
    private Unit unit;
    private int value;
    @SerializedName("retrievalstatus")
    private RetrievalStatus retrievalStatus;
    private long timestamp;
}
