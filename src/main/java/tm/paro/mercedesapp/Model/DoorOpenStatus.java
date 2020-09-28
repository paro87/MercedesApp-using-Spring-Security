package tm.paro.mercedesapp.Model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
class DoorOpenStatus {
    //private String value;

    private Value value;
    @SerializedName("retrievalstatus")
    private RetrievalStatus retrievalStatus;
    private long timestamp;
}
