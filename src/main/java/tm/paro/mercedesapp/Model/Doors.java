package tm.paro.mercedesapp.Model;

import lombok.Data;

@Data
public class Doors {
    private DoorOpenStatus doorstatusfrontleft;
    private DoorOpenStatus doorstatusfrontright;
    private DoorOpenStatus doorstatusrearleft;
    private DoorOpenStatus doorstatusrearright;
    private DoorOpenStatus doorlockstatusfrontleft;
    private DoorOpenStatus doorlockstatusfrontright;
    private DoorOpenStatus doorlockstatusrearleft;
    private DoorOpenStatus doorlockstatusrearright;
    private DoorOpenStatus doorlockstatusdecklid;
    private DoorOpenStatus doorlockstatusgas;
    private DoorOpenStatus doorlockstatusvehicle;
    private Command command;

    public Doors() {
        doorstatusfrontleft = new DoorOpenStatus();
        doorstatusfrontright = new DoorOpenStatus();
        doorstatusrearleft = new DoorOpenStatus();
        doorstatusrearright = new DoorOpenStatus();

        doorlockstatusfrontleft = new DoorOpenStatus();
        doorlockstatusfrontright = new DoorOpenStatus();
        doorlockstatusrearleft = new DoorOpenStatus();
        doorlockstatusrearright = new DoorOpenStatus();

        doorlockstatusdecklid = new DoorOpenStatus();
        doorlockstatusgas = new DoorOpenStatus();
        doorlockstatusvehicle = new DoorOpenStatus();


    }

}
