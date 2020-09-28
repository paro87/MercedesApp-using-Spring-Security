package tm.paro.mercedesapp.Model;

import com.google.gson.annotations.SerializedName;

enum RetrievalStatus {
    @SerializedName("VALID")
    VALID,
    @SerializedName("INITIALIZED")
    INITIALIZED,
    @SerializedName("INVALID")
    INVALID,
    @SerializedName("NOT_SUPPORTED")
    NOT_SUPPORTED
}
