package farm.visitor;

import farm.model.Crop;
import farm.model.Farm;
import farm.model.Field;
import farm.model.Plot;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.Valve;

public interface Visitor<R> {
    R visitFarm(Farm farm);

    R visitField(Field field);

    R visitPlot(Plot plot);

    R visitCrop(Crop crop);

    R visitPump(Pump pump);

    R visitSensor(Sensor sensor);

    R visitValve(Valve valve);
}
